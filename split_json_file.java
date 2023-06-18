import java.util.Properties;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
	
	//Extract the Inbound File contents from Boomi input data stream
    def data = is.getText("UTF-8");
	
	//Declare JSON Slurper Object.
    def jsonSlurper = new JsonSlurper();
	
	//Parse the JSON from the data text, using jsonSlurper object.
    def inputJSON = jsonSlurper.parseText(data);
    
	//Define another json object from the data header.v
    def headerValues = inputJSON.result_histogram.header.v;
	
	//Define an Array of Strings to store the splitted JSON files at output.
    def outputJSONStrings = [];
    
	//Build another JSON file based on the data as index from header.v using Labda.
    headerValues.eachWithIndex { value, index ->
        def outputJSON = [:]; //Define a blank parent object.
        outputJSON.result_histogram = [:]; //Define child object
        outputJSON.result_histogram.header = [:]; //Define grand-child object
        outputJSON.result_histogram.header.v = [value]; //Add value to the great-grand-child object
		
		//Create another loop similar fashion and add values based on the index to the data object.
        outputJSON.result_histogram.data = inputJSON.result_histogram.data.collect { d -> 
            [t: d.t, v: [d.v[index]]];
        }
		//Publish the output json into the Array of Strings.
        outputJSONStrings << JsonOutput.toJson(outputJSON);
    }
    
    //Publish the output to Boomi data stream after splitting.
    outputJSONStrings.each { jsonString -> 
        is = new ByteArrayInputStream(jsonString.getBytes("UTF-8"));
        dataContext.storeStream(is, props);
    }

}
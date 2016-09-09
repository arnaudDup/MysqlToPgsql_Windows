package mysqlToPostgres;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import utils.ActionUtils;

import java.util.Properties;




public class PropertyLoader implements Constant {
	
	 	private static Logger logger = (Logger) LoggerFactory.getLogger(PropertyLoader.class);	
		static Properties properties;
		
		 
		   public static Properties load(String filename) throws IOException, FileNotFoundException{
		      properties = new Properties();
		      File fileProperties = new File(filename);
		      
		      try(FileInputStream input = new FileInputStream(fileProperties); ) {
		    	  properties.load(input);
		    	  return properties;
		      }
		   }
		   
		   public static void changeValues (String attribute, String value) throws FileNotFoundException, IOException {
			   if (properties == null) {
				   load(CONFIG_FILE);
			   }
			   properties.setProperty(attribute,value);
		   }
		   
		   public static void changeValues (Map<String,String> linkToValue) throws FileNotFoundException, IOException {
			   if (properties == null) {
				   load(CONFIG_FILE);
			   }
			   try(FileOutputStream out = new FileOutputStream(CONFIG_FILE);){
				   //Set new value on properties file for all value in hashMap.
				   for(Entry<String, String> entry : linkToValue.entrySet()) {
					    logger.debug("changement for key : "+entry.getKey()+" for the new value :"+entry.getValue());
					    properties.setProperty(entry.getKey(),entry.getValue());   
					}
				   properties.store(out, null);
			   }

		   }
		   
}

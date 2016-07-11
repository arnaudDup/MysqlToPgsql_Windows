package Transform.MY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;




public class PropertyLoader {

		   public static Properties load(String filename) throws IOException, FileNotFoundException{
		      Properties properties = new Properties();
		      File fileProperties = new File(filename);
		      FileInputStream input = new FileInputStream(fileProperties); 
		      try{
		    	  properties.load(input);
		    	  return properties;
		      }finally{input.close();}
		   }
}

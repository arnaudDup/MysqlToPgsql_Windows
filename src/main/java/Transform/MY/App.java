package Transform.MY;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException{
    	
    	Properties prop =  PropertyLoader.load("config.properties");
		ConnexionDB connexion = new ConnexionDB();
	
		// truncate the database and load the new data set.
    	if(prop.get("isforJunitTest").equals("true")){
    		try {
    			connexion.TruncateAllTableInMysqlDatabase();
    			connexion.loadDataINTOMysqlDatabase();
    		} catch (Exception e) {e.printStackTrace();}
    	}
    	
    	// dump the table in the CSV form.
		try {
			connexion.MakeCsvFromDataMysqlDatabase(true);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// truncate the postgres database.
		if(prop.get("needToTruncatePostgres").equals("true")){
			connexion.TruncateAllTableInPostgresDatabase();
		}
		
		// load the new data set in the postgres database.
		connexion.loadDataINTOPostgresdatabase();
    }
    	
}
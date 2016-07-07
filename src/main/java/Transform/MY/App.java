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
		connexion.MakeCsvFromDataMysqlDatabase(true);
		if(prop.get("needToTruncatePostgres").equals("true")){
			connexion.TruncateAllTableInPostgresDatabase();
		}
		connexion.loadDataINTOPostgresdatabase();
		
		// permet de truncate all tables in mysql database.
		/*
		try {
			connexion.TruncateAllTableInMysqlDatabase();
		} catch (SQLException e) {e.printStackTrace();}
		*/
    }
}
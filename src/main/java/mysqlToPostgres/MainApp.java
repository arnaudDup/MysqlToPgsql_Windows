package mysqlToPostgres;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import dao.ActionWithDatabase;
import utils.ActionUtils;


public class MainApp 
{
	private static Logger logger = (Logger) LoggerFactory.getLogger(MainApp.class);
	private static Properties prop;
	private static ActionWithDatabase manager;
	
    public static void main( String[] args ) throws IOException{
    	runApplication();
    }
    
    
    // ===========================================================
    // Methods
    // ===========================================================
    
    public static void saveDatabase(){
        try {
        	ActionWithDatabase manager = getManager();
        	Properties prop = getProperties();
            logger.info("saveDatabase(), Save curent database");
            
            if(prop.getProperty("savePostgresDatabase").equals("true")){
            	logger.info("saveDatabase(), Mysql Dump... ");
            	manager.saveDataFromMysqlDatabse();
            	logger.info("saveDatabase(),Success");
            }
            
            if(prop.getProperty("savePostgresDatabase").equals("true")){
            	logger.info("saveDatabase(), postgres Dump... ");
            	manager.saveDataFromMysqlDatabse();
            	logger.info("saveDatabase(),Success");
            }
            	
        } catch (Exception e) {
            logger.error("saveDatabase() KO: " + e, e);
            // we exit the programme before doing
            System.exit(1);
        }
    }
    
    public static void restoreDatabase(){
        try {
  
        	ActionWithDatabase manager = getManager();
        	Properties prop = getProperties();
            logger.info("restoreDatabase(), Save curent database");
            
            if(prop.getProperty("restoreMysqlDatabase").equals("true")){
            	logger.info("restoreDatabase(), Mysql restore... ");
            	manager.restoreMysqlDatabaseFromDump();
            }
            
            if(prop.getProperty("restorePostgresDatabase").equals("true")){
            	logger.info("restoreDatabase(), postgres Dump... ");
            	manager.restorePostgresDatabaseFromDump();
            }
            	
        } catch (Exception e) {
            logger.error("restoreDatabase() KO: " + e, e);
            // TODO generer une exception.
        }
    }
    
    // TODO improve handle exception
    public static void runApplication() throws FileNotFoundException, IOException  {
    	
    	ActionWithDatabase manager = getManager();
    	Properties prop = getProperties();
		saveDatabase();
		
		// TODO faire le travail de champs.
		
		restoreDatabase();
		// truncate the database and load the new data set.
    	if(prop.get("isforJunitTest").equals("true")){
    		try {
    			manager.TruncateAllTableInMysqlDatabase();
    			manager.loadDataINTOMysqlDatabase(false);
    		} catch (Exception e) {e.printStackTrace();}
    	}
    	
    	if(prop.getProperty("needToLoadIntoPostgres").equals("true")){
	    	// dump the table in the CSV form.
			try {
				manager.MakeCsvFromDataMysqlDatabase(true);
			} catch (Exception e) {
			}
			
			// truncate the postgres database.
			if(prop.get("needToTruncatePostgres").equals("true")){
				try {
					manager.TruncateAllTableInPostgresDatabase();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// load the new data set in the postgres database.
			manager.loadDataINTOPostgresdatabase(false);
    	}
    }
    
    // make the instance unique
    public static Properties getProperties() throws FileNotFoundException, IOException{
    	if(prop == null){
    		prop = PropertyLoader.load("config.properties");
    	}
    	return prop;
    } 
    
    // make the instance unique.
    public static ActionWithDatabase getManager() throws FileNotFoundException, IOException {
    	if(manager == null){
    		manager = new ActionWithDatabase();
    	}
    	return manager;
    }   
}
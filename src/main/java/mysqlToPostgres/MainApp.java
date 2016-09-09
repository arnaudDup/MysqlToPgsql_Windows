package mysqlToPostgres;

import java.util.Properties;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import dao.ActionWithDatabase;
import utils.ActionUtils;
import utils.NexcapException;


public class MainApp
{
	private static Logger logger = (Logger) LoggerFactory.getLogger(MainApp.class);
	private static Properties prop;
	private static ActionWithDatabase manager;
	
    public static void main( String[] args ) throws Exception{
    	int errorCode = 0;
    	Properties prop = getProperties();
        try {	
        	boolean shouldContinue = ActionUtils.ChangePropertiesSetting(args);
            if(shouldContinue){
            	runApplication();
            }
        } catch (Exception e) {
            logger.error("main KO: " + e, e);
            errorCode = 1;
        }finally{
        	// Make a pause for debugging an see log information.
        	logger.info("main(), exit code : "+errorCode);
        	System.exit(errorCode);
        }
    }
    
    
    // ===========================================================
    // Methods
    // ===========================================================
    
    public static void saveDatabase(){
        try {
        	ActionWithDatabase manager = getManager();
        	Properties prop = getProperties();
            logger.info("saveDatabase(), Save curent database");
            
            if(prop.getProperty("saveMysqlDatabase").equals("true")){
            	logger.info("saveDatabase(), Mysql Dump... ");
            	manager.saveDataFromMysqlDatabse();        	
            }
            
            if(prop.getProperty("savePostgresDatabase").equals("true")){
            	logger.info("saveDatabase(), postgres Dump... ");
            	manager.saveDataFromPostgresDatabase();
            }
            
            String response = (	prop.getProperty("saveMysqlDatabase").equals("true") ||
					prop.getProperty("savePostgresDatabase").equals("true"))?"Success":"Nothing to do";
            logger.info("saveDatabase(),"+ response);
        } catch (Exception e) {
            logger.error("saveDatabase() KO: " + e, e);
            // We stop the programm because the progamm couldn't save the curent state.
            System.exit(2);
        }
    }
    
    public static void restoreDatabase() throws Exception{
        try {
  
        	ActionWithDatabase manager = getManager();
        	Properties prop = getProperties();
            logger.info("restoreDatabase(), Restore curent database");
            
            if(prop.getProperty("restoreMysqlDatabase").equals("true")){
            	logger.info("restoreDatabase(), Mysql restore... ");
            	manager.restoreMysqlDatabaseFromDump();
            }
            
            if(prop.getProperty("restorePostgresDatabase").equals("true")){
            	logger.info("restoreDatabase(), postgres restore... ");
            	manager.restorePostgresDatabaseFromDump();
            }
            String response = (	prop.getProperty("restorePostgresDatabase").equals("true") ||
					prop.getProperty("restoreMysqlDatabase").equals("true"))?"Success":"Nothing to do";
            logger.info("restoreDatabase(),"+ response);	
        } catch (Exception e) {
            logger.error("restoreDatabase() KO: " + e, e);
            // we exit the program if the restauration doesn't work, this is just to warn in oder to restaure the database manually. 
            System.exit(2);
        }
    }
    
    public static void truncateDatabase() throws Exception{
        try {
        	  
        	ActionWithDatabase manager = getManager();
        	Properties prop = getProperties();
            logger.info("truncateDatabase(), truncate curent database");
            
            if(prop.getProperty("truncateMysqlDatabase").equals("true")){
            	logger.info("truncateDatabase(), Mysql truncate all table... ");
            	manager.TruncateAllTableInMysqlDatabase();
            }
            
            if(prop.getProperty("truncatePostgresDatabase").equals("true")){
            	logger.info("truncateDatabase(), postgres truncate all table... ");
            	manager.TruncateAllTableInPostgresDatabase();;
            }
            String response = (	prop.getProperty("truncatePostgresDatabase").equals("true") ||
            					prop.getProperty("truncateMysqlDatabase").equals("true"))?"Success":"Nothing to do";
            logger.info("truncateDatabase()," +response);	
        } catch (Exception e) {
            logger.error("truncateDatabase() KO: " + e, e);
            throw new Exception("truncateDatabase() KO:" + e, e); 
        }
    }
    
    public static void loadData() throws Exception{
    	
        try {
        	ActionWithDatabase manager = getManager();
        	Properties prop = getProperties();
            logger.info("loadData(), truncate curent database");
            
            if(prop.getProperty("loadIntoMysql").equals("true")){
            	logger.info("loadData(), Mysql load... ");
            	manager.loadIntoMysqlDatabase();
            }
            
            if(prop.getProperty("loadIntoPostgres").equals("true")){
            	logger.info("loadData(), postgres load... ");
            	manager.loadIntoPostgresDatabase();
            }
            String response = (	prop.getProperty("loadIntoPostgres").equals("true") ||
					prop.getProperty("loadIntoMysql").equals("true"))?"Success":"Nothing to do";
            logger.info("loadData(),"+ response);	
        } catch (Exception e) {
            logger.error("loadData() KO: " + e, e);
            throw new Exception("loadData() KO:" + e, e); 
        }
    }
    
    public static void makeCSVFileFromMysql() throws Exception{
    	try {
	       	if(prop.getProperty("makeCsvFileFromMysql").equals("true")){
			    	ActionWithDatabase manager = getManager();
			    	Properties prop = getProperties();
	       			logger.info("makeCSVFileFromMysql(), make csv compatible postgres");
					manager.MakeCsvFromDataMysqlDatabase(true);
	       		} 
            String response = (	prop.getProperty("makeCsvFileFromMysql").equals("true"))?"Success":"Nothing to do";
            logger.info("makeCsvFileFromMysql(),"+ response);	
    	}
       	catch (NexcapException e) {
	            logger.error("makeCSVFileFromMysql() KO: " + e, e);
	            // The query doesn't work properly, it's useless to continue.
	            System.exit(1);
			} catch (Exception e) {
				logger.error("makeCSVFileFromMysql() KO: " + e, e);
				throw new Exception("makeCSVFileFromMysql() KO:" + e, e); 
			}
    }
    
    public static void DropAndCreate() throws Exception{
    	try {
	       	if(prop.getProperty("dropAndCreate").equals("true")){
		    	ActionWithDatabase manager = getManager();
		    	Properties prop = getProperties();
		    	manager.DropAndCreate();
	       	} 
            String response = (	prop.getProperty("dropAndCreate").equals("true"))?"Success":"Nothing to do";
            logger.info("dropAndCreate(),"+ response);	
    	}
       	catch (NexcapException e) {
	            logger.error("DropAndCreate() KO: " + e, e);
	            System.exit(2);
		}
    }
    
    
    public static void updateSequence() throws Exception{
    	try {
	       	if(prop.getProperty("UpdateSequencePostgres").equals("true")){
		    	ActionWithDatabase manager = getManager();
		    	Properties prop = getProperties();
		    	manager.updateSequence();
	       	} 
            String response = (	prop.getProperty("UpdateSequencePostgres").equals("true"))?"Success":"Nothing to do";
            logger.info("UpdateSequencePostgres(),"+ response);	
    	}
       	catch (NexcapException e) {
	            logger.error("updateSequence() KO: " + e, e);
	            System.exit(2);
		}
    }
    
    
    public static void runApplication() throws Exception  {
		try{	
    		saveDatabase();
    		DropAndCreate();
			truncateDatabase();
			makeCSVFileFromMysql();
			loadData();
			restoreDatabase();
			updateSequence();
			
    	}catch (Exception e) {
            logger.error("runApplication() KO: " + e, e);
            throw new Exception("runApplication() KO:" + e, e); 
        }
    }
    
    // make a unique instance
    public static Properties getProperties() throws NexcapException{
    	try {
	    	if(prop == null){
	    		prop = PropertyLoader.load("config.properties");
	    	}
	    	return prop;
    	}catch(Exception e){
    		logger.error("getProperties() KO: " + e, e);
    		throw new NexcapException("getProperties() KO: Impossible to get properties file " + e, e);
    	}
    } 
    
    // make a unique instance
    public static ActionWithDatabase getManager() throws NexcapException {
    	try {
	    	if(manager == null){
	    		manager = new ActionWithDatabase();
	    	}
	    	return manager;
	    	
    	}catch(Exception e){
    		logger.error("getManager() KO: " + e, e);
    		throw new NexcapException("getManager() KO: Impossible to get properties file " + e, e);
    	}
    }  
}
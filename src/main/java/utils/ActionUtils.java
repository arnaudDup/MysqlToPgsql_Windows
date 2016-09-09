package utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import mysqlToPostgres.Constant;
import mysqlToPostgres.ConstantForProperties;
import mysqlToPostgres.PropertyLoader;

public class ActionUtils implements Constant,ConstantForProperties {
	
	public static final String SAVE = "save"; 
	public static final String DROP_CREATE = "drop_create"; 
	public static final String INSERT_DATA = "insertData"; 
	public static final String RESTORE_DATA  = "restoreData"; 
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(ActionUtils.class);	
	protected static File ErrorOutputMYSQL;
	protected static File ErrorOutputPOSTGRES;
	
	
	
	public static void cleanRepo(File repo, File csvRepo, File csvRepoSave) throws IOException{
		cleanRepo(repo,true);
		cleanRepo(csvRepo,true);
		cleanRepo(csvRepoSave,true);
	}
	
	public static void cleanRepo(File repo, boolean recreate)throws IOException {
		if(repo.exists()){
			logger.info("cleanRepo(), suppresion de "+repo.getAbsolutePath());
			FileUtils.deleteDirectory(repo);
		}
		if(recreate){
			logger.info("cleanRepo(), creation de "+repo.getAbsolutePath());
			repo.mkdirs();
		}
	}

	public static void checkFilePresentAndRemove(File tempFile) throws IOException{
		
		if(tempFile.exists()){
			logger.info("checkFilePresentAndRemove(), suppresion de "+tempFile.getAbsolutePath());
			tempFile.delete();
		}
		logger.info("checkFilePresentAndRemove(), creation de "+tempFile.getAbsolutePath());
		tempFile.createNewFile();
	}
	
	public static void removeErrorFile() throws NexcapException{
		File tempFile = null; 
		tempFile =  new File(RessourcesFolder,NAME_ERROR_POSTGRES);
		if(tempFile.delete()){
			logger.info("removeErrorFile() : Remove postgres error file");
		}
		
		tempFile =  new File(RessourcesFolder,NAME_ERROR_MYSQL);
		if(tempFile.delete()){
			logger.info("removeErrorFile() : Remove Mysql error file");
		}
	}
	public static File getFileError (int database) throws NexcapException{
		try{
			File tempFile = null;
			switch(database){
				case (POSTGRES) : 
					if(ErrorOutputPOSTGRES == null){
						ErrorOutputPOSTGRES = new File(RessourcesFolder,NAME_ERROR_POSTGRES);
						ErrorOutputPOSTGRES.createNewFile();
						logger.info("getFileError() : Create postgres error file");
					}
					tempFile =  ErrorOutputPOSTGRES;
				break;
				case (MYSQL) :
					if(ErrorOutputMYSQL == null){
						ErrorOutputMYSQL = new File(RessourcesFolder,NAME_ERROR_MYSQL);
						ErrorOutputMYSQL.createNewFile();
						logger.info("getFileError() : Create mysql error file");
					}
					tempFile =  ErrorOutputMYSQL;
				break;
			}
		return tempFile;
		}
		catch(Exception e){
		    logger.error("getFileError() KO: " + e, e);
		    throw new NexcapException("getFileError() KO: Impossible to open file " + e, e);
		}
		
		
	}
	
	/**
	 * arguments[0] = url for mysql connection
	 * arguments[1] = user for mysql connection
	 * arguments[2] = password  for mysql connection
	 * arguments[3] = url for postgres connection
	 * arguments[4] = user for postgres connection
	 * arguments[6] = schema  for postgres connection
	 * arguments[7] = folder csv Mysql
	 * arguments[8] = folder csv postgres
	 * arguments[9] = transfer data to postgres(boolean)

	 * @param arguments
	 * @return 
	 * @throws Exception
	 */
   public static boolean ChangePropertiesSetting(String [] arguments)throws Exception {
	   
	   Map<String, String> temp = new HashMap <String, String>();
	   if (arguments.length == 0){
		   // Nothing to do we keep the curent config.properties
		   logger.info("ChangePropertiesFile(),Do nothing no argument");
		   return true;
	   }
	   if (arguments.length == 1){
		   // Nothing to do we keep the curent config.properties
		   logger.info("ChangePropertiesFile(),change action in the main");
		   return ChangePropertiesActions(arguments);
	   }
	   else{
	   
		   if (arguments.length != NB_OF_ATTRIBUTE){
			   throw new Exception("the size of the String [] args doesn't correspond to the expected value.");
		   }
		   // build the hash Map to change properties dynamically;
		   Map<String, String> urlMysql = ActionUtils.decodeUrl(arguments[0]);
		   
		   /********************* Change the value for mysql connection **********************/
		   
		   temp.put(MY_SQL_HOST, urlMysql.get(HOST));
		   temp.put(MY_SQL_PORT, urlMysql.get(PORT));
		   temp.put(MY_SQL_DATABASE, urlMysql.get(DATABASE_NAME));
		   
		   temp.put(MY_SQL_USER,arguments[1] );
		   temp.put(MY_SQL_PASSWORD,arguments[2]);
		   
		   /********************* Change the value for mysql connection **********************/
		   
		   Map<String, String> urlPostgres = ActionUtils.decodeUrl(arguments[3]);
		   temp.put(POSTGRES_HOST, urlPostgres.get(HOST));
		   temp.put(POSTGRES_PORT, urlPostgres.get(PORT));
		   temp.put(POSTGRES_DATABASE, urlPostgres.get(DATABASE_NAME));
		   
		   temp.put(POSTGRES_USER,arguments[4] );
		   temp.put(POSTGRES_PASSWORD,arguments[5] );
		   temp.put(POSTGRES_SCHEMA,arguments[6] );
	
		   /********************* Change the value of specific folder **********************/
			
		   temp.put(CSV_EXPORT_MYSQL,arguments[7]);
		   temp.put(CSV_EXPORT_POSTGRES,arguments[8]);
		   temp.put(REPO_SAVE_POSTGRES,arguments[9]);
		   
		   /********************* Change programm behavior *********************************/

		   // change properties file.
		   try {
			PropertyLoader.changeValues(temp);
		   } catch (Exception e) {
			logger.error("getProperties() KO: " + e, e);
			// if the programm can't change properties value it's not necessary to continue.
			System.exit(1);
		   }
		   return false;
	   }
   }
   public static boolean ChangePropertiesActions(String [] arguments)throws Exception {
	   
	   Map<String, String> temp = new HashMap <String, String>();
	   
	   // insertion données.
	   if (arguments[0].equals(INSERT_DATA)){
		   temp.put(SAVE_POSTGRES,"false");
		   temp.put(LOAD_POSTGRES,"true");
		   temp.put(RESTORE_POSTGRES,"false");
		   temp.put(DROP_AND_CREATE,"false");
	   }
	   // restoration data.
	   else if (arguments[0].equals(RESTORE_DATA)){
		   temp.put(SAVE_POSTGRES,"false");
		   temp.put(LOAD_POSTGRES,"false");
		   temp.put(RESTORE_POSTGRES,"true");
		   temp.put(DROP_AND_CREATE,"false");
	   }
	   // allow to save data in the postgres database, drop the schema and create a new one.
	   else if (arguments[0].equals(SAVE)){
		   temp.put(SAVE_POSTGRES,"true");
		   temp.put(LOAD_POSTGRES,"false");
		   temp.put(RESTORE_POSTGRES,"false");
		   temp.put(DROP_AND_CREATE,"false");
	   }
	   // allow to drop and create a new schema.
	   else if (arguments[0].equals(DROP_CREATE)){
		   temp.put(SAVE_POSTGRES,"false");
		   temp.put(LOAD_POSTGRES,"false");
		   temp.put(RESTORE_POSTGRES,"false");
		   temp.put(DROP_AND_CREATE,"true");
	   }
	   
	   // change properties file.
	   try {
		PropertyLoader.changeValues(temp);
	   } catch (Exception e) {
		logger.error("getProperties() KO: " + e, e);
		// if the programm can't change properties value it's not necessary to continue.
		System.exit(1);
	   }
	   if(temp.size() == 0){
		   return false; 
	   }
	   return true;
   }
	
	public static Map<String,String> decodeUrl (String url) throws Exception{
		
		Map<String,String> mapUrl = new HashMap<String, String>();
		
		String pattern1 = "://(.*?):(.*?)\\/(.*?)\\?";
		Pattern p1 = Pattern.compile(pattern1);
		Matcher m1 = p1.matcher(url);

		if (m1.find()){
			mapUrl.put(HOST, m1.group(1));
			mapUrl.put(PORT, m1.group(2));
			mapUrl.put(DATABASE_NAME, m1.group(3));
		}
		else {
			
			 throw new Exception("Malformed Url");
		}
		return mapUrl ;
		
		
	}
	
}

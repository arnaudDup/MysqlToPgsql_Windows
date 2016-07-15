package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import mysqlToPostgres.CSVLoader;
import utils.ActionUtils;

public class ActionWithDatabase extends BaseDAO {
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(ActionWithDatabase.class);
	
	public ActionWithDatabase() throws FileNotFoundException, IOException{
		super();
	}
	
	/**
	 * Truncate all Table in postgres database
	 * @throws SQLException 
	 */
	public void TruncateAllTableInPostgresDatabase() throws SQLException{
		
		List<String> avoidingTable = Arrays.asList(this.prop.getProperty("tableUpdateAvoid").split("\\s*,\\s*"));
		SettingAutoCommit(connecPostgres,true);
		// for all table we truncate in cascasde the table  
		for (String nameTable : presentTableName){
			
			// Don't continue if the programm shouldn't modify the table.
			if(avoidingTable.contains(nameTable)){continue;}
			
			// clean data
			String query = REGEXTRUNCATE.replace(REGEX_TABLE, nameTable);	
			Statement stat =  connecPostgres.createStatement();
			System.out.println(query);
			stat.execute(query);

		}
	}

	/**
	 * Truncate all Table in Mysql database
	 * @throws SQLException
	 */
	public void TruncateAllTableInMysqlDatabase() throws SQLException{
	
		// disable constraints
		Statement statBefore =  connecMysql.createStatement();
		statBefore.execute("SET FOREIGN_KEY_CHECKS = 0;");
		for (String nameFile : presentTableName){
			
			String query = REGEXTRUNCATEMYSQL.replace(REGEX_TABLE, nameFile);	

				Statement stat =  connecMysql.createStatement();
				System.out.println(query);
				stat.execute(query);
		}
		
		// disable constraints
		Statement statAfter =  connecMysql.createStatement();
		statAfter.execute("SET FOREIGN_KEY_CHECKS = 1;");
	}

	/**
	 * Create csv from MySQl database.
	 * @param needToLoad
	 * @throws Exception 
	 */
	public void MakeCsvFromDataMysqlDatabase(boolean needToLoad) throws Exception{
		
		// execute the query in order to load data --> 
		if(needToLoad){
			String command = RessourcesFolder.getAbsolutePath()+"/mysqldump -u"+this.prop.getProperty("mySqlUser")
																+ " --host="+this.prop.getProperty("mySqlHost")+" --compatible=postgres"
																+ " --port="+this.prop.getProperty("mySqlPort")+" -p "+this.prop.getProperty("MysqlDatabase")
																+ " --password="+this.prop.getProperty("mySqlPassword")+" --fields-terminated-by=\""+END_OF_FIELD+"\" "
																+ " --lines-terminated-by=\""+END_OF_LINE+"\" --default-character-set=\"utf8\""
																+ " --tab=\""+this.prop.getProperty("mysqlFolderCSVEXPORT")+"\"";
			// process the request in a new process. 
			Runtime runtime = Runtime.getRuntime();
			try {
				System.out.println("Getting csv from Mysql database");
				System.out.println(command);
				Process process = runtime.exec(command);
				InputStream stdin = process.getInputStream();
	            InputStreamReader isr = new InputStreamReader(stdin,ENCODING);
	            BufferedReader br = new BufferedReader(isr);
	            String line = null;
	            while ( (line = br.readLine()) != null)System.out.println(line); 
	            int state = process.waitFor();
	            System.out.println((state == 0)?"Success":"Fail");
	            if(state != 0){
					System.exit(1);
				}
			} catch (IOException e) {e.printStackTrace();} 
			  catch (InterruptedException e) {e.printStackTrace();}
		}
		
		FileWriter fw = null;
		for (String curentNameFile : repo.list()){
			if(!curentNameFile.contains(".txt")){
				continue;
			}
			
			File curentDataFile = new File(repo,curentNameFile);
			System.out.println("data processing"+curentDataFile.getAbsolutePath());
			
			try {
				File csvFile = new File(csvRepo,curentNameFile);
				if(csvFile.exists()){csvFile.delete();}
				fw = new FileWriter(csvFile, true);
			} catch (IOException e1){e1.printStackTrace();}
	
			try {
				String str = FileUtils.readFileToString(curentDataFile,ENCODING);
				
				// transform null value for the csv format. (by default in postgres null value is an unquoted empty string in CSV format)
				str = str.replace("\\N","")
						
						// keep the "\n" char on the data set.
						.replace("\n","\\n").replace("\r\\n","\r\n")
						
						// replace the boolean value.
						.replace("","1").replace("\u0000","0");
				
				fw.append(str);	 
		    	fw.flush();
			} catch (IOException e) {e.printStackTrace();}
			
		new File(csvRepo,curentNameFile).setReadable(true,false);
		}
		if(fw != null){
			try {
				fw.close();
			} catch (IOException e) {e.printStackTrace();}
		}
		
		// suppres temporary file.
		ActionUtils.cleanRepo(repo,false);
	}
	
	/**
	 * Load Data into Mysql Database.
	 */
	public void loadDataINTOMysqlDatabase(boolean idOldData){
		
		String command = RessourcesFolder.getAbsolutePath()	+"/mysql -u"+prop.getProperty("mySqlUser")
															+ " --host="+prop.getProperty("mySqlHost")
															+ " --port="+prop.getProperty("mySqlPort")
															+ " -p "+prop.getProperty("MysqlDatabase")
															+ " --password="+prop.getProperty("mySqlPassword");
        ProcessBuilder pb = new ProcessBuilder("cmd.exe","/C",command);
        Process p;
		try {
			System.out.println("Load data into Mysql Database");
			System.out.println(command);
			File sqlFile = (idOldData)? new File(this.prop.getProperty("mysqlDumpToSave")) :new File(this.prop.getProperty("mysqlDumpPath"));
			p = pb.redirectErrorStream(true).redirectInput(sqlFile).start();
			System.out.println((p.waitFor() == 0)?"Success":"Fail"); 
		} catch (IOException e) {e.printStackTrace();} 
		  catch (InterruptedException e) {e.printStackTrace();}
	}
	
	private void SettingAutoCommit(Connection con,boolean isAutoCommit){
		try {
			con.setAutoCommit(isAutoCommit);
		} catch (SQLException e) {e.printStackTrace();}
	}
	
	private void commitOperation (Connection con) throws SQLException{
		try {
			con.commit();
		} catch (SQLException e) {
			con.rollback();
			e.printStackTrace();
		}
	}
	
	private void beginTransaction (Connection con) throws SQLException{
		String query = "BEGIN TRANSACTION; ";
		Statement stat = con.createStatement();
		stat.execute(query);
	}

	private void disableConstraint(Connection con) throws SQLException{
		String query = "SET CONSTRAINTS ALL DEFERRED; ";
		Statement stat = con.createStatement();
		stat.execute(query);
	}
	
	/**
	 * Load data into postgres.
	 */
	public void loadDataINTOPostgresdatabase(boolean isOldData){
		
		File repoCSV  = (isOldData)?repoSavepostgres:csvRepo;
		List<String> avoidingTable = Arrays.asList(this.prop.getProperty("tableUpdateAvoid").split("\\s*,\\s*"));
		// Open the transaction
		SettingAutoCommit(connecPostgres,false);
		try {
			beginTransaction(connecPostgres);
			disableConstraint(connecPostgres);
		} catch (SQLException e1) {e1.printStackTrace();}

        CSVLoader loader = new CSVLoader(connecPostgres,prop.getProperty("postgresShema"),ENCODING,END_OF_FIELD,QUOTE);
        
        // populate the databse.
        for(String curentFileName : repoCSV.list()){
        	
        	// Don't continue if the programm shouldn't modify the table.
        	if(avoidingTable.contains(curentFileName.replace(".txt", ""))){continue;}
        	
        	// populate the database.
	        try{
	        	loader.loadCSV(repoCSV.getAbsolutePath() +"\\"+ curentFileName, curentFileName.replace(".txt", ""));
	        }catch(Exception e){
	        	e.printStackTrace();
	        	System.out.println("Impossible de la mise a jour  :  "+curentFileName);
	        	continue;
	        }
        }  
		try {
			commitOperation(connecPostgres);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		SettingAutoCommit(connecPostgres,true);
	}
	
	/**
	 * Make a dump of Mysql Database.
	 */
	public void saveDataFromMysqlDatabse(){
		
		String command = RessourcesFolder.getAbsolutePath()	+"/mysqldump -u"+this.prop.getProperty("mySqlUser")
															+" --host="+this.prop.getProperty("mySqlHost")
															+" --port="+this.prop.getProperty("mySqlPort")
															+" -p "+this.prop.getProperty("MysqlDatabase")
															+" --password="+this.prop.getProperty("mySqlPassword")
															+" --no-create-info --skip-triggers --no-create-db"
															+" --default-character-set=\"utf8\"";
		
        ProcessBuilder pb = new ProcessBuilder("cmd.exe","/C",command);
        Process p;
		try {
			logger.debug(command);
			File sqlFile = new File(this.prop.getProperty("mysqlDumpToSave"));
			p = pb.redirectErrorStream(false).redirectOutput(sqlFile).start();
			int state = p.waitFor();
			logger.debug((state == 0)?"Success":"Fail"); 
			
			// If the backed up fail we don't continue
			if(state != 0){
				System.exit(1);
			}
			
		} catch (IOException e) {e.printStackTrace();} 
		  catch (InterruptedException e) {e.printStackTrace();}
		logger.info("ActionWithDatabase,saveDataFromMysqlDatabse(),success");
	}
		
	public void saveDataFromPostgresDatabase() throws SQLException, IOException{
		
        CSVLoader loader = new CSVLoader(connecPostgres,prop.getProperty("postgresShema"),ENCODING,END_OF_FIELD,QUOTE);
        // make a csv file for all table the databse.
        for(String curentTableName : presentTableName){
        	File tempFile = new File(repoSavepostgres,curentTableName+".txt");
        	if (tempFile.exists()){
	        	if (tempFile.delete()){
	        		tempFile.createNewFile();
	        	}
	        	else{
	        		throw new IOException("Impossible to delete the file.");
	        	}
        	}
        	else{
        		tempFile.createNewFile();
        	}
        	
	        try{
	        	loader.MakeCSV(tempFile.getAbsolutePath(), curentTableName);
	        }catch(Exception e){
	        	e.printStackTrace();
	        	System.out.println("Impossible de la mise a jour  :  "+curentTableName);
	        	continue;
	        }
        }  
	
	}
	
	public void restoreMysqlDatabaseFromDump() throws SQLException{	
		// suppress all data in the database.
		TruncateAllTableInMysqlDatabase();
		// we repopulate the database.
		loadDataINTOPostgresdatabase(true);
	}
	public void restorePostgresDatabaseFromDump() throws SQLException{
		// suppress all data in the database.
		TruncateAllTableInPostgresDatabase();
		// we repopulate the database.
		loadDataINTOPostgresdatabase(true);
	}
	
}


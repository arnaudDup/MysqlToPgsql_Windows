package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import mysqlToPostgres.CSVLoader;
import utils.ActionUtils;
import utils.NexcapException;

public class ActionWithDatabase extends BaseDAO {
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(ActionWithDatabase.class);
	
	public ActionWithDatabase() throws FileNotFoundException, IOException{
		super();
	}
	
	public void saveDataFromMysqlDatabse() throws NexcapException{
		try {
			ActionUtils.checkFilePresentAndRemove(sqlDumpFile);
			String command = RessourcesFolder.getAbsolutePath()	+"\\mysqldump -u"+this.prop.getProperty("mySqlUser")
																+" --host="+this.prop.getProperty("mySqlHost")
																+" --port="+this.prop.getProperty("mySqlPort")
																+" -p "+this.prop.getProperty("MysqlDatabase")
																+" --password="+this.prop.getProperty("mySqlPassword")
																+"  --skip-triggers "
																+" --default-character-set=\"utf8\"";
			
	        ProcessBuilder pb = new ProcessBuilder("cmd.exe","/C",command);
	        Process p;
	        
			logger.debug("saveDataFromMysqlDatabse():"+command);
			logger.debug("saveDataFromMysqlDatabse(): make a dump using "+sqlDumpFile.getAbsolutePath());
			p = pb.redirectErrorStream(false).redirectOutput(sqlDumpFile).redirectError(ActionUtils.getFileError(MYSQL)).start();
			int state = p.waitFor(); 
			
			// If the backed up fail we don't continue
			if(state != 0){
				logger.error("saveDataFromMysqlDatabse() KO: ");
				throw new NexcapException("saveDataFromMysqlDatabse() KO: impossible to save database return query code:"+state); 
			}
		} catch (Exception e) {
		    logger.error("saveDataFromMysqlDatabse() KO: " + e, e);
		    throw new NexcapException("saveDataFromMysqlDatabse() KO: Impossible to save database " + e, e); 
		}
	}

	public void saveDataFromPostgresDatabase() throws NexcapException{
		try{
			ActionUtils.cleanRepo(repoSavepostgres,true);
	        CSVLoader loader = new CSVLoader(connecPostgres,prop.getProperty("postgresShema"),ENCODING,END_OF_FIELD,QUOTE);
	        for(String curentTableName : presentTableNamePostgres){
	        	File tempFile = new File(repoSavepostgres,curentTableName+".txt");
	        	if (tempFile.exists()){
		        	if (tempFile.delete()){
		        		tempFile.createNewFile();
		        	}
	        	}
	        	else{
	        		tempFile.createNewFile();
	        	}
	        	logger.debug("saveDataFromPostgresDatabase(), make csv of "+ curentTableName);
	        	loader.MakeCSV(tempFile.getAbsolutePath(), curentTableName);
	        }
		}catch (Exception e) {
			    logger.error("saveDataFromPostgresDatabase() KO: " + e, e);
			    throw new NexcapException("saveDataFromPostgresDatabase() KO: Impossible to save database " + e, e); 
		}  
	}

	public void TruncateAllTableInPostgresDatabase() throws NexcapException{
		try{
			reloadListNameTablePgsql();
			List<String> avoidingTable = Arrays.asList(this.prop.getProperty("tableUpdateAvoid").split("\\s*,\\s*"));
			SettingAutoCommit(connecPostgres,true);
			// for all table we truncate in cascasde the table  
			for (String currentNameTable : presentTableNamePostgres){
				
				// Don't continue if the programm shouldn't modify the table.
				if(avoidingTable.contains(currentNameTable)){continue;}
				
				String query = REGEXTRUNCATE.replace(REGEX_TABLE, currentNameTable);	
				Statement stat =  connecPostgres.createStatement();
				logger.debug("TruncateAllTableInPostgresDatabase(), truncate table "+currentNameTable+ " query : "+ query);
				stat.execute(query);
	
			}
		}catch (Exception e) {
		    logger.error("TruncateAllTableInPostgresDatabase() KO: " + e, e);
		    throw new NexcapException("TruncateAllTableInPostgresDatabase() KO: Impossible to truncate database " + e, e); 
		}
	}

	/**
	 * Truncate all Table in Mysql database
	 * @throws NexcapException 
	 * @throws SQLException
	 */
	public void TruncateAllTableInMysqlDatabase() throws NexcapException{
		try{
			reloadListNameTableMysql();
			// disable constraints
			Statement statBefore =  connecMysql.createStatement();
			statBefore.execute("SET FOREIGN_KEY_CHECKS = 0;");
			for (String currentNameTable : presentTableName){
				
				String query = REGEXTRUNCATEMYSQL.replace(REGEX_TABLE, currentNameTable);	
				Statement stat =  connecMysql.createStatement();
				logger.debug("TruncateAllTableInPostgresDatabase(), truncate table "+currentNameTable+ " query : "+ query);
				stat.execute(query);
			}
			
			// disable constraints
			Statement statAfter =  connecMysql.createStatement();
			statAfter.execute("SET FOREIGN_KEY_CHECKS = 1;");
			
		}catch (Exception e) {
		    logger.error("TruncateAllTableInMysqlDatabase() KO: " + e, e);
		    throw new NexcapException("TruncateAllTableInMysqlDatabase() KO: Impossible to truncate database " + e, e); 
		}
	}
	
	/**
	 * Now use loadDataIntopostgresByInsertRequest() may be less efficient but works on remote database. 
	 * Create csv from MySQl database.
	 * @param needToLoad
	 * @throws Exception 
	 */
	@Deprecated
	public void MakeCsvFromDataMysqlDatabase(boolean needToLoad) throws NexcapException,Exception {
		
		try {
			ActionUtils.cleanRepo(repoCsvMysql,true);
			ActionUtils.cleanRepo(repoCsvPostgres,true);
			reloadListNameTableMysql();
			if(needToLoad){

				String command = RessourcesFolder.getAbsolutePath()+"\\mysqldump -u"+this.prop.getProperty("mySqlUser")
																	+ " --host="+this.prop.getProperty("mySqlHost")+" --compatible=postgres"
																	+ " --port="+this.prop.getProperty("mySqlPort")+" -p "+this.prop.getProperty("MysqlDatabase")
																	+ " --password="+this.prop.getProperty("mySqlPassword")+" --fields-terminated-by=\""+END_OF_FIELD+"\" "
																	+ " --lines-terminated-by=\""+END_OF_LINE+"\" --default-character-set=\"utf8\""
																	+ " --tab=\""+this.prop.getProperty("mysqlFolderCSVEXPORT")+"\"";
				logger.info("MakeCsvFromDataMysqlDatabase(): Make csv file of Mysql Database");
				Runtime runtime = Runtime.getRuntime();
				logger.debug("MakeCsvFromDataMysqlDatabase(): making csv "+command);
				Process process = runtime.exec(command);
				
				// Cleaning the Output stream of the console.
				InputStream stdin = process.getInputStream();
	            InputStreamReader isr = new InputStreamReader(stdin,ENCODING);
	            BufferedReader br = new BufferedReader(isr);
	            String line = null;
	            while ( (line = br.readLine()) != null); 
	            int state = process.waitFor();
	            logger.debug((state == 0)?"Success Command":"Fail command");
	            if(state != 0){
					logger.error("MakeCsvFromDataMysqlDatabase() KO: Impossible to make csv from Mysql database ");
					throw new NexcapException("saveDataFromMysqlDatabse() KO: impossible to save database return query code:"+state); 
				}
			}	
			
			// cleaning repository 
			ActionUtils.cleanRepo(repoCsvPostgres,true);
			
			FileWriter fw = null;
			logger.info("MakeCsvFromDataMysqlDatabase(): Modify csv file to be compatible with postgres");
			for (String curentNameFile : presentTableName){
				
				File curentDataFile = new File(repoCsvMysql,curentNameFile+EXTENSION);
				logger.debug("MakeCsvFromDataMysqlDatabase dealing with : "+curentNameFile);
			
				File csvFile = new File(repoCsvPostgres,curentNameFile);
				fw = new FileWriter(csvFile, true);
		
					String str = FileUtils.readFileToString(curentDataFile,ENCODING);
					
					// transform null value for the csv format. (by default in postgres null value is an unquoted empty string in CSV format)
					str = str.replace("\\N","")
							
							// keep the "\n" char on the data set.
							.replace("\n","\\n").replace("\r\\n","\r\n")
							
							// replace the boolean value.
							.replace("","1").replace("\u0000","0");
					
					fw.append(str);	 
			    	fw.flush();	
				new File(repoCsvPostgres,curentNameFile).setReadable(true,false);
			}
			if(fw != null){fw.close();}
		}catch(Exception e){
		    logger.error("MakeCsvFromDataMysqlDatabase() KO: " + e, e);
		    throw new Exception("MakeCsvFromDataMysqlDatabase() KO: Impossible to truncate database " + e, e); 
		}
		
	}

	public void loadDataINTOMysqlDatabase(boolean idOldData) throws NexcapException{
		try {
			File sqlFile = (idOldData)? sqlDumpFile : sqlJunitDumpFile;
			logger.info("loadDataINTOMysqlDatabase() : load data in Mysql from "+ sqlFile);
			
			String command = RessourcesFolder.getAbsolutePath()	+"\\mysql -u"+prop.getProperty("mySqlUser")
																+ " --host="+prop.getProperty("mySqlHost")
																+ " --port="+prop.getProperty("mySqlPort")
																+ " -p "+prop.getProperty("MysqlDatabase")
																+ " --password="+prop.getProperty("mySqlPassword");
	        ProcessBuilder pb = new ProcessBuilder("cmd.exe","/C",command);
			logger.debug("loadDataINTOMysqlDatabase() : "+ command);
			Process p = pb.redirectErrorStream(true).redirectInput(sqlFile).redirectError(ActionUtils.getFileError(MYSQL)).start();
			int state = 0;
			logger.debug(((state = p.waitFor()) == 0)?"Success Command":"Fail command"); 
            if(state != 0){
				logger.error("MakeCsvFromDataMysqlDatabase() KO: Impossible to load data Corrupt SQL file");
				throw new NexcapException("loadDataINTOMysqlDatabase() KO: impossible to load database return query code:"+state); 
			}
		} catch (Exception e) {
		    logger.error("loadDataINTOMysqlDatabase() KO: " + e, e);
		    throw new NexcapException("loadDataINTOMysqlDatabase() KO: Impossible to load data into Mysql database " + e, e); 
		} 
	}
	
	public void loadDataIntopostgresByInsertRequest() throws NexcapException{
		logger.info("loadDataIntopostgresByInsertRequest() : load data in postgres from "+dataSetPostgres.getAbsolutePath());
		LineIterator it = null;
		Statement stmt = null;
		try {
			it = FileUtils.lineIterator(dataSetPostgres, "UTF-8");
			// Open the transaction and disable constraints.
			SettingAutoCommit(connecPostgres,false);
			beginTransaction(connecPostgres);
			disableConstraint(connecPostgres);
			
			stmt = connecPostgres.createStatement();
			// for each line we execute a create query.
		    while (it.hasNext()) {
		        String line = it.nextLine();
		        logger.debug("loadDataIntopostgresByInsertRequest() : dealing with : "+line);
		        stmt.executeUpdate(line);
		    }
		} catch(Exception e){
		    logger.error("loadDataIntopostgresByInsertRequest() KO: " + e, e);
		    throw new NexcapException("loadDataIntopostgresByInsertRequest() KO: Impossible d'importer les données." + e, e); 
		}finally {
			if(it != null) LineIterator.closeQuietly(it);
		    if (stmt != null){
		    	try {
		    		stmt.close();
		    	}
		    	catch (SQLException e) {
		    		logger.error("loadIntoPostgres() KO: " + e, e);
		    		throw new NexcapException("loadDataIntopostgresByInsertRequest() KO: Impossible de fermer le statement" + e, e); 	
		    	}
		    } 
		}
		// commit opération.
		commitOperation(connecPostgres);
		SettingAutoCommit(connecPostgres,true);
	}
	public void change_Sequence_Value () throws NexcapException{
		try {
			logger.info("change_Sequence_Value(),get liste table with sequence.");
			List<String> listTable = new ArrayList<String>();
			Statement stat = null;
			stat =  connecPostgres.createStatement();

			// get all sequence name.
			stat.execute(SEQUENCE_REQUEST.replace(REGGEX_SCHEMA,prop.getProperty("postgresShema")));
			ResultSet  rs = stat.getResultSet();
			while (rs.next()) {
				listTable.add(rs.getString(1));
			}
			for(String nameConstraint : listTable ){
				String tableName = nameConstraint.substring(0,nameConstraint.indexOf("_id_seq"));
				// replace the value of the sequence by the maximum id.
				String query = SEQUENCE_REGEX_SETVALUE.replace(REGEX_TABLE, tableName)
														
								.replace(REGEX_SEQUENCE_NAME,nameConstraint)
								
								.replace(REGGEX_SCHEMA,prop.getProperty("postgresShema"));
						
				logger.debug("change_Sequence_Value(), changement de l'id dans la sequence" + query);
				stat.execute(query);
			}
			
		} catch (SQLException e) {
			   logger.error("change_Sequence_Value() KO: " + e, e);
			   throw new NexcapException("change_Sequence_Value() KO: Impossible de changer la sequence id" + e, e); 
		}
	}
	public void transformRequestForPostgres() throws NexcapException {
		
		logger.info("transformRequestForPostgres() : dealing with Mysql dump "+dataSetMysql.getAbsolutePath());
		
		LineIterator it = null;
		FileWriter fw = null;
		try {
			
			it = FileUtils.lineIterator(dataSetMysql, "UTF-8");
			String command = null;
			
			fw = new FileWriter(dataSetPostgres, true);
		    while (it.hasNext()) {
		        String line = it.nextLine();
		        	
		           // get all line beginning by insert into.
			       if(line.contains("INSERT INTO")){
			    	   
			    	   // replace by the schema.
						line = line.replace("INSERT INTO `","INSERT INTO "+ this.prop.getProperty("postgresShema") +".").replace("`"," ");
						
						// get the command in order to reduce the string length.
			    	   	int indexTemp = line.indexOf("(");
			    	   	command = line.substring(0,indexTemp-1);
			    	   	
								// replace the boolean values.
						line =	line.replace("''","true").replace("'\\0'","false")
								// make line command.
								.replace("),(", ");\r\n"+command+"(")
								// escape special character in postgres is simple quote.
								.replace("\\'","''")
								// replace special quote for jackson.
								.replace("\\\"","\"");

						// flush the string on the file.
						//logger.info("transformRequestForPostgres() : dealing with : "+line);
						fw.append(line);
						fw.append("\r\n");
					    fw.flush();	
					}
		    }
		}catch(Exception e){
		    logger.error("loadIntoPostgres() KO: " + e, e);
		    throw new NexcapException("loadIntoPostgres() KO: Impossible de construire le fichier correspondant." + e, e); 
		}
		finally {
			if(it != null) LineIterator.closeQuietly(it);
		    if(fw != null){
			    try {
					fw.close();
				} 
			    catch (IOException e) {
					   logger.error("loadIntoPostgres() KO: " + e, e);
					   throw new NexcapException("loadIntoPostgres() KO: Impossible de fermer le fichier contenant les requétes." + e, e); 
				}
		    } 
		}
	}
	
	
	public void loadDataINTOPostgresDatabase(boolean isOldData) throws NexcapException{
		try {
			
			File repoCSV  = (isOldData)?repoSavepostgres:repoCsvPostgres;
			logger.info("loadDataINTOPostgresDatabase() : load data in Mysql from "+ repoCSV);
			List<String> avoidingTable = Arrays.asList(this.prop.getProperty("tableUpdateAvoid").split("\\s*,\\s*"));
			
			// Open the transaction and disable constraints.
			SettingAutoCommit(connecPostgres,false);
			beginTransaction(connecPostgres);
			disableConstraint(connecPostgres);
		
		    CSVLoader loader = new CSVLoader(connecPostgres,prop.getProperty("postgresShema"),ENCODING,END_OF_FIELD,QUOTE);
		    		
		    // populate the databse.
		    for(String curentFileName : repoCSV.list()){
		    	// Don't continue if the programm shouldn't modify the table.
		    	if(avoidingTable.contains(curentFileName.replace(".txt", ""))){continue;}
		    	// populate the database.
		        loader.loadCSV(repoCSV.getAbsolutePath() +"\\"+ curentFileName, curentFileName.replace(".txt", ""));
		    }  
		    
			// commit opération.
			commitOperation(connecPostgres);
			SettingAutoCommit(connecPostgres,true);
			
		}catch(Exception e){
			 logger.error("loadDataINTOPostgresDatabase() KO: " + e, e);
			 throw new NexcapException("loadDataINTOPostgresDatabase() KO: Impossible to load data into Mysql database " + e, e); 
		} 
	}
	
	private void SettingAutoCommit(Connection con,boolean isAutoCommit){
		logger.info("commitOperation(), setting autocommit " + isAutoCommit); 
		try {
			con.setAutoCommit(isAutoCommit);
		} catch (SQLException e) {e.printStackTrace();}
	}
	
	private void commitOperation (Connection con) throws NexcapException {
		logger.info("commitOperation(), commit operation"); 
		try {
			con.commit();
		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				logger.error("commitOperation() KO: " + e, e);
				throw new NexcapException("commitOperation() KO: Impossible to rolback " + e, e);
			}
			logger.error("commitOperation() KO: " + e, e);
			throw new NexcapException("commitOperation() KO: Impossible to Commit " + e, e);
		}
	}
	
	private void beginTransaction (Connection con) throws NexcapException{
		try {
			logger.info("beginTransaction(), open a transaction");
			String query = "BEGIN TRANSACTION; ";
			Statement stat;
			stat = con.createStatement();
			stat.execute(query);
		} catch (SQLException e) {
			logger.error("beginTransaction() KO: " + e, e);
			throw new NexcapException("beginTransaction() KO: Impossible to begin transaction " + e, e);
		}
	}
	
	public void DropPostgresTable() throws NexcapException{
		try {
			logger.info("DropPostgresTable() : drop schema"+prop.getProperty("postgresShema"));
			String query = REGGEX_DROP_TABLE.replace(REGGEX_SCHEMA, prop.getProperty("postgresShema"));
			logger.debug(query);
			Statement stat = connecPostgres.createStatement();
			stat.execute(query);
		} catch (SQLException e) {
			logger.error("DropPostgresTable() KO: " + e, e);
			throw new NexcapException("DropPostgresTable() KO: Impossible to drop schema " + e, e);
		}
	}
	
	public void CreatePostgresTable() throws NexcapException{
		try {
			logger.info("CreatePostgresTable() : create schema"+prop.getProperty("postgresShema"));
			String query = REGGEX_CREATE_TABLE.replace(REGGEX_SCHEMA, prop.getProperty("postgresShema")).replace(REGGEX_USER, prop.getProperty("postgresUser"));
			logger.debug(query);
			Statement stat = connecPostgres.createStatement();
			stat.execute(query);
		} catch (SQLException e) {
			logger.error("CreatePostgresTable() KO: " + e, e);
			throw new NexcapException("CreatePostgresTable() KO: Impossible to create schema " + e, e);
		}
	}

	private void disableConstraint(Connection con) throws NexcapException{
		try {
			logger.info("disableConstraint(), disbale constraint"); 
			String query = "SET CONSTRAINTS ALL DEFERRED; ";
			Statement stat = con.createStatement();
			stat.execute(query);
		} catch (SQLException e) {
			logger.error("disableConstraint() KO: " + e, e);
			throw new NexcapException("disableConstraint() KO: Impossible to disable constraint " + e, e);
		}
	}
	
	public void restoreMysqlDatabaseFromDump() throws NexcapException {	
		// suppress all data in the database.
		TruncateAllTableInMysqlDatabase();
		// we repopulate the database.
		loadDataINTOMysqlDatabase(true);
	}
	public void restorePostgresDatabaseFromDump()throws NexcapException{
		// suppress all data in the database.
		TruncateAllTableInPostgresDatabase();
		// we repopulate the database.
		loadDataINTOPostgresDatabase(true);
		// change the sequence value.
		change_Sequence_Value (); 
	}
	
	public void loadIntoPostgresDatabase() throws NexcapException{
		// suppress all data in the database.
		TruncateAllTableInPostgresDatabase();
		// we create the file for inserting data. 
		transformRequestForPostgres();
		// We insert data from the file.
		loadDataIntopostgresByInsertRequest();
		// change the sequence value.
		change_Sequence_Value (); 
		
	}
	
	public void loadIntoMysqlDatabase() throws NexcapException{
		// suppress all data in the database.
		TruncateAllTableInMysqlDatabase();
		// we repopulate the database.
		loadDataINTOMysqlDatabase(false);
	}
	
	public void loadIntoPostgresDatabaseByCsv() throws NexcapException{
		// suppress all data in the database.
		TruncateAllTableInPostgresDatabase();
		// we repopulate the database.
		loadDataINTOPostgresDatabase(false);
	}

	public void DropAndCreate() throws NexcapException {
		DropPostgresTable();
		CreatePostgresTable();
	}

	public void updateSequence() throws NexcapException {
		// change the sequence value.
		change_Sequence_Value (); 
	}
		
}


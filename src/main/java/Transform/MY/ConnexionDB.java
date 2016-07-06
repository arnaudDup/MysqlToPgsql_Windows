package Transform.MY;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.nio.file.Paths;
import java.security.PermissionCollection;
import java.io.FilePermission;

import org.apache.commons.io.FileUtils;

public class ConnexionDB {
	
	public static final String END_OF_FIELD = "!!!,";
	public static final String END_OF_LINE = "**\\r\\n";
	public static final String EXTENSION = ".txt"; 
	public static final String REGEXTRUNCATE = "TRUNCATE nexcapup.{table} CASCADE;"; 
	public static final String REGEX_TABLE = "{table}";

	Properties prop;
	private Connection connecPostgres;
	private Connection connecMysql;
	private File repo;
	private File csvRepo;
	
	public ConnexionDB() throws IOException
	 {
		// load properties; 
		prop = PropertyLoader.load("config.properties");
		repo = new File(this.prop.getProperty("mysqlFolderCSVEXPORT"));
		csvRepo = new File(this.prop.getProperty("postgresFolderCSVEXPORT"));
		// Connect to the MySQl database.
	    try{
	    		System.out.println("Connexion to Mysql Database");
	            Class.forName(this.prop.getProperty("mysqlDriver"));
	            connecMysql=DriverManager.getConnection(this.prop.getProperty("mySqlUrl"),
                								this.prop.getProperty("mySqlUser"),
                								this.prop.getProperty("mySqlPassword"));
	            System.out.println("Success");
	        }catch(Exception e){
	            System.out.println("Error in connection"+e);
	        }

		// Connnect to the postgres database.
        try {
        	System.out.println("Connexion to Postgres database");
        	Class.forName(this.prop.getProperty("postgresDriver"));
			connecPostgres =DriverManager.getConnection(this.prop.getProperty("postgresUrl"),
														this.prop.getProperty("postgresUser"),
														this.prop.getProperty("postgresPassword"));
			System.out.println("Succes");
			
		} catch (SQLException e) {e.printStackTrace();}
          catch (ClassNotFoundException e) {e.printStackTrace();}
        
        // Cleaning repository.
	    cleanRepo();
	}
	
	private void cleanRepo() throws IOException{
		if(!repo.exists()){
			FileUtils.deleteDirectory(repo);
		}
		repo.mkdirs();
		
		if(!csvRepo.exists()){
			FileUtils.deleteDirectory(csvRepo);
		}
		csvRepo.mkdirs();
	}
	
	/**
	 * allow to truncate table before inserting data into postgres database.
	 */
	public void TruncateAllTableInPostgresDatabase(){
		List<String> avoidingTable = Arrays.asList(this.prop.getProperty("TableUpdateAvoid").split("\\s*,\\s*"));
		SettingAutoCommit(connecPostgres,true);
		// for all table we truncate in cascasde the table  
		for (String nameFile : csvRepo.list()){
			nameFile = nameFile.replace(EXTENSION,"");
			
			// Don't continue if the programm shouldn't modify the table.
			if(avoidingTable.contains(nameFile)){continue;}
			
			// clean data
			String query = REGEXTRUNCATE.replace(REGEX_TABLE, nameFile);	
			try {
				Statement stat =  connecPostgres.createStatement();
				System.out.println(query);
				stat.execute(query);
			} catch (SQLException e) {e.printStackTrace();}
		}
	}
	

	/**
	 * Create csv from MySQl database.
	 * @param needToLoad
	 */
	public void MakeCsvFromDataMysqlDatabase(boolean needToLoad){
		
		// execute the query in order to load data --> debug
		if(needToLoad){
			String command = "mysqldump -u root --host=localhost --skip-set-charset --compatible=postgres  --port=3306 -p nexcapup  --password=454gf360 --fields-terminated-by=\""+END_OF_FIELD+"\" --lines-terminated-by=\""+END_OF_LINE+"\" --default-character-set=utf8   --tab=\"C:/ProgramData/MySQL/MySQL Server 5.6/Uploads\"";
			Runtime runtime = Runtime.getRuntime();
			try {
				System.out.println(command);
				Process process = runtime.exec(command);
				process.waitFor();
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
				String str = FileUtils.readFileToString(curentDataFile,"UTF-8");
				
				str = str.replace("\\N","").replace("\"","").replace("\r\n","")
						 .replace("\n","").replace("**", "\r\n").replace("","1")
						 .replace("\\0","0").replace(",","").replace("!!!", ",");
				
				fw.append(str);	 
		    	fw.flush();
			} catch (IOException e) {e.printStackTrace();}
			
		new File(csvRepo,curentNameFile).setReadable(true,false);
		} 
		try {
			fw.close();
		} catch (IOException e) {e.printStackTrace();}
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
	

	
	public void loadDataINTOPostgresdatabase(){
		List<String> avoidingTable = Arrays.asList(this.prop.getProperty("TableUpdateAvoid").split("\\s*,\\s*"));
		// Open the transaction
		SettingAutoCommit(connecPostgres,false);
		try {
			beginTransaction(connecPostgres);
			disableConstraint(connecPostgres);
		} catch (SQLException e1) {e1.printStackTrace();}

        CSVLoader loader = new CSVLoader(connecPostgres,"nexcapup","utf-8", ",");
        
        // populate the databse.
        for(String curentFileName : csvRepo.list()){
        	
        	// Don't continue if the programm shouldn't modify the table.
        	if(avoidingTable.contains(curentFileName.replace(".txt", ""))){continue;}
        	
        	// populate the database.
	        try{
	        	loader.loadCSV(csvRepo.getAbsolutePath() +"/"+ curentFileName, curentFileName.replace(".txt", ""));
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
}

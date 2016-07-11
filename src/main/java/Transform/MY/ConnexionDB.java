package Transform.MY;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	
	public static final String ENCODING = "UTF-8";
	public static final String END_OF_FIELD = "!!!,";
	public static final String END_OF_LINE = "**\\r\\n";
	public static final String EXTENSION = ".txt"; 
	public static final String REGEXTRUNCATEMYSQL = "TRUNCATE  {table};"; 
	public static final String REGEXLISTTABLE = "SHOW full  TABLES From {database} where  Table_Type != 'VIEW';";
	public static final String REGEX_URL_MYSQL = "jdbc:mysql://{host}:{port}/{database}?characterEncoding=UTF-8";
	public static final String REGEX_URL_PGSQL = "jdbc:postgresql://{host}:{port}/{database}?characterEncoding=UTF-8";
	public static final String REGEX_TABLE = "{table}";
	public static final String REGEX_DATABASE = "{database}";
	public static final String REGEX_HOST = "{host}";
	public static final String REGEX_PORT = "{port}";

	Properties prop;
	private Connection connecPostgres;
	private Connection connecMysql;
	private File repo;
	private File csvRepo;
	private String REGEXTRUNCATE = "TRUNCATE {database}.{table} CASCADE;"; 
	
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
	            connecMysql=DriverManager.getConnection(REGEX_URL_MYSQL.replace(REGEX_HOST, prop.getProperty("mySqlHost"))
	         		   													.replace(REGEX_PORT, prop.getProperty("mySqlPort"))
	         		   													.replace(REGEX_DATABASE,prop.getProperty("MysqlDatabase")),
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
			connecPostgres =DriverManager.getConnection(REGEX_URL_PGSQL.replace(REGEX_HOST, prop.getProperty("postgresHost"))
					   													.replace(REGEX_PORT, prop.getProperty("postgresPort"))
					   													.replace(REGEX_DATABASE,prop.getProperty("postgresShema")),
														this.prop.getProperty("postgresUser"),
														this.prop.getProperty("postgresPassword"));
			System.out.println("Succes");
			
		} catch (SQLException e) {e.printStackTrace();}
          catch (ClassNotFoundException e) {e.printStackTrace();}
        
        REGEXTRUNCATE =  REGEXTRUNCATE.replace(REGEX_DATABASE,this.prop.getProperty("postgresShema"));
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
		List<String> avoidingTable = Arrays.asList(this.prop.getProperty("tableUpdateAvoid").split("\\s*,\\s*"));
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
	
	
	private  List<String> getTableNameMySql() throws SQLException {

		List<String> listTable = new ArrayList<String>();
		Statement stat = null;
		try {
			stat =  connecMysql.createStatement();
			stat.execute(REGEXLISTTABLE.replace(REGEX_DATABASE, prop.getProperty("MysqlDatabase")));
		} catch (SQLException e) {e.printStackTrace();}
		
		ResultSet  rs= stat.getResultSet();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		
		// get all tables names.
		while (rs.next()) {
		    	listTable.add(rs.getString(1));
		}
		return listTable;
	}
	
	
	public void TruncateAllTableInMysqlDatabase() throws SQLException{

		List<String> listTable = getTableNameMySql();
		
		// disable constraints
		Statement statBefore =  connecMysql.createStatement();
		statBefore.execute("SET FOREIGN_KEY_CHECKS = 0;");
		for (String nameFile : listTable){
			
			String query = REGEXTRUNCATEMYSQL.replace(REGEX_TABLE, nameFile);	
			try {
				Statement stat =  connecMysql.createStatement();
				System.out.println(query);
				stat.execute(query);
			} catch (SQLException e) {e.printStackTrace();}
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
		
		File RessourcesFolder = new File("resources");
		// execute the query in order to load data --> debug
		if(needToLoad){
			String command = RessourcesFolder.getAbsolutePath()+"/mysqldump -u"+this.prop.getProperty("mySqlUser")
																+ " --host="+this.prop.getProperty("mySqlHost")+" --compatible=postgres"
																+ " --port="+this.prop.getProperty("mySqlPort")+" -p "+this.prop.getProperty("MysqlDatabase")
																+ " --password="+this.prop.getProperty("mySqlPassword")+" --fields-terminated-by=\""+END_OF_FIELD+"\" "
																+ " --lines-terminated-by=\""+END_OF_LINE+"\" --default-character-set=\"utf8\""
																+ " --tab=\""+this.prop.getProperty("mysqlFolderCSVEXPORT")+"\"";
			Runtime runtime = Runtime.getRuntime();
			try {
				
				System.out.println(command);
				Process process = runtime.exec(command);
				InputStream stdin = process.getInputStream();
	            InputStreamReader isr = new InputStreamReader(stdin,ENCODING);
	            BufferedReader br = new BufferedReader(isr);
	            String line = null;
	            while ( (line = br.readLine()) != null)System.out.println(line); 
	            int exitVal = process.waitFor(); 
				System.out.println(exitVal);
				
				// we generate an Exception if the command fail.
				if (exitVal != 0){
					throw new Exception("Impossible to import data"); 
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
				
				str = str.replace("\\N","").replace("\"","").replace("\r\n","")
						 .replace("\n","").replace("**", "\r\n").replace("","1").replace("\u0000","0")
						 .replace("\\0","0").replace(",","").replace("!!!", ",");
				
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
	}
	
	public void loadDataINTOMysqlDatabase(){
		
		File RessourcesFolder = new File("resources");
		List<String> params = new ArrayList<String>();
		String command = RessourcesFolder.getAbsolutePath()+"/mysql -u"+prop.getProperty("mySqlUser")
						+ " --host="+prop.getProperty("mySqlHost")
						+ " --port="+prop.getProperty("mySqlPort")
						+ " -p "+prop.getProperty("MysqlDatabase")
						+ " --password="+prop.getProperty("mySqlPassword");
        ProcessBuilder pb = new ProcessBuilder("cmd.exe","/C",command);
        Process p;
		try {
			System.out.println(command);
			File sqlFile = new File(this.prop.getProperty("mysqlDumpPath"));
			p = pb.redirectErrorStream(true).redirectInput(sqlFile).start();
			int state = p.waitFor();
			System.out.println(state);
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
	

	
	public void loadDataINTOPostgresdatabase(){
		List<String> avoidingTable = Arrays.asList(this.prop.getProperty("tableUpdateAvoid").split("\\s*,\\s*"));
		// Open the transaction
		SettingAutoCommit(connecPostgres,false);
		try {
			beginTransaction(connecPostgres);
			disableConstraint(connecPostgres);
		} catch (SQLException e1) {e1.printStackTrace();}

        CSVLoader loader = new CSVLoader(connecPostgres,prop.getProperty("postgresShema"),ENCODING, ",");
        
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

package mysqlToPostgres;

public interface ConstantForProperties {
	
	/******************* Mysql conf *************************************/
	
	public static final String MY_SQL_USER = "mySqlUser";
	public static final String MY_SQL_PASSWORD = "mySqlPassword";
	public static final String MY_SQL_HOST = "mySqlHost";
	public static final String MY_SQL_PORT = "mySqlPort";
	public static final String MY_SQL_DATABASE = "MysqlDatabase";
	
	/******************* Postgres conf *************************************/
	
	public static final String POSTGRES_USER = "postgresUser";
	public static final String POSTGRES_PASSWORD = "postgresPassword";
	public static final String POSTGRES_HOST = "postgresHost";
	public static final String POSTGRES_PORT = "postgresPort";
	public static final String POSTGRES_DATABASE = "postgresDatabase";
	public static final String POSTGRES_SCHEMA = "postgresShema";
	
	/******************* Specific Folder *************************************/
	
	public static final String CSV_EXPORT_MYSQL = "mysqlFolderCSVEXPORT";
	public static final String CSV_EXPORT_POSTGRES = "postgresFolderCSVEXPORT";
	public static final String REPO_SAVE_POSTGRES = "makeCsvFileFromMysql";
	
	/************************* Configuration *********************************/
	
	public static final String SAVE_POSTGRES = "savePostgresDatabase";
	public static final String LOAD_POSTGRES = "loadIntoPostgres";
	public static final String RESTORE_POSTGRES = "restorePostgresDatabase";
	public static final String MAKE_CSV_FILE = "makeCsvFileFromMysql";
	
	
	
	public static final String DROP_AND_CREATE = "dropAndCreate";
	
	
	
	
	
	public static final int NB_OF_ATTRIBUTE = 10;
		 
	
}

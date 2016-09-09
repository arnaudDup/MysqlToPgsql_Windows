package mysqlToPostgres;

import java.io.File;

public interface Constant {
	
	public static final int POSTGRES = 0;
	public static final int MYSQL = 1;
	public static final String ENCODING = "UTF-8";
	public static final String END_OF_FIELD = "~";
	public static final String END_OF_LINE = "\\r\\n";
	public static final String QUOTE = "*";
	public static final String EXTENSION = ".txt"; 
	public static final String REGEXTRUNCATEMYSQL = "TRUNCATE  {table};"; 
	public static final String REGEXLISTTABLEPOSTGRES = "SELECT * FROM information_schema.tables where table_type = 'BASE TABLE' and table_schema = '{database}';";
	public static final String REGEXLISTTABLE = "SHOW full  TABLES From {database} where  Table_Type != 'VIEW';";
	public static final String REGEX_URL_MYSQL = "jdbc:mysql://{host}:{port}/{database}?characterEncoding=UTF-8";
	public static final String REGEX_URL_PGSQL = "jdbc:postgresql://{host}:{port}/{database}?characterEncoding=UTF-8";
	public static final String SEQUENCE_REQUEST = "SELECT sequence_name FROM information_schema.sequences where sequence_schema = '{schema}' ;";
	public static final String SEQUENCE_REGEX_SETVALUE = "SELECT setval('{schema}.{NameSequence}',(SELECT coalesce(MAX(id)+1,1) FROM {schema}.{table}),false);";
	public static final String REGEX_TABLE = "{table}";
	public static final String REGEX_DATABASE = "{database}";
	public static final String REGEX_HOST = "{host}";
	public static final String REGEX_PORT = "{port}";
	public static final String REGEX_SEQUENCE_NAME = "{NameSequence}";
	public static final String NAME_ERROR_POSTGRES = "redirectErrorPostgres.txt";
	public static final String NAME_ERROR_MYSQL = "redirectErrorMysql.txt";
	public static final File  RessourcesFolder =  new File("resources");
	public static final String  CONFIG_FILE =  "config.properties";
	public static final String REGGEX_DROP_TABLE = "DROP SCHEMA {schema} CASCADE;";
	public static final String REGGEX_CREATE_TABLE = "CREATE SCHEMA {schema} AUTHORIZATION {user};";
	public static final String REGGEX_SCHEMA = "{schema}";
	public static final String REGGEX_USER = "{user}";
	
	/****************************** Constante for Regex communication ****************************/
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String DATABASE_NAME = "database_name";
	

}

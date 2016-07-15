package mysqlToPostgres;

import java.io.File;

public interface Constant {
	
	public static final int POSTGRES = 0;
	public static final int Mysql = 1;
	public static final String ENCODING = "UTF-8";
	public static final String END_OF_FIELD = "~";
	public static final String END_OF_LINE = "\\r\\n";
	public static final String QUOTE = "*";
	public static final String EXTENSION = ".txt"; 
	public static final String REGEXTRUNCATEMYSQL = "TRUNCATE  {table};"; 
	public static final String REGEXLISTTABLE = "SHOW full  TABLES From {database} where  Table_Type != 'VIEW';";
	public static final String REGEX_URL_MYSQL = "jdbc:mysql://{host}:{port}/{database}?characterEncoding=UTF-8";
	public static final String REGEX_URL_PGSQL = "jdbc:postgresql://{host}:{port}/{database}?characterEncoding=UTF-8";
	public static final String REGEX_TABLE = "{table}";
	public static final String REGEX_DATABASE = "{database}";
	public static final String REGEX_HOST = "{host}";
	public static final String REGEX_PORT = "{port}";
	public static final File  RessourcesFolder =  new File("resources");

}

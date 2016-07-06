package Transform.MY;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.opencsv.CSVReader;

public class CSVLoader {
	
	private String SQL_COPY = "COPY ${schema}.${table} FROM '${filename}' CSV ENCODING '${encoding_name}' DELIMITER '${separator}';";	
	private static final String TABLE_REGEX = "${table}";
	private static final String Filename_REGEX = "${filename}";
	private static final String ENCODING_NAME = "${encoding_name}";
	private static final String SEPARATOR = "${separator}";
	private static final String SCHEMA = "${schema}";

	private Connection connection;

	public CSVLoader (Connection aConnection,String aSchema ,String anEncoding_Name, String aSeparator) {
		// TODO prendre le temps de de faire un truc generique qui prend encoding
		this.connection = aConnection;
		// build the query.
		SQL_COPY = SQL_COPY.replace(ENCODING_NAME, anEncoding_Name); 
		SQL_COPY = SQL_COPY.replace(SEPARATOR, aSeparator);
		SQL_COPY = SQL_COPY.replace(SCHEMA, aSchema);
	}
	
	
	public boolean loadCSV(String csvFile, String tableName) throws Exception {
		boolean isCorrect = true; 
		String query = SQL_COPY;
		
		// build the specific request.
		query = query.replace(TABLE_REGEX, tableName); 
		query = query.replace(Filename_REGEX, csvFile); 
		
		
		Statement stat =  connection.createStatement();
		System.out.println(query);
		stat.execute(query);
		return isCorrect; 
	}
}
	
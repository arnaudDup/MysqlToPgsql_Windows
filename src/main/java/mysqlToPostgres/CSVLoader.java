package mysqlToPostgres;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import utils.NexcapException;

public class CSVLoader {
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(CSVLoader.class);
	private String SQL_COPY_SAVE = "COPY ${schema}.${table} TO STDOUT CSV ENCODING '${encoding_name}' DELIMITER '${separator}' QUOTE '${quote}';";	
	private String SQL_COPY = "COPY ${schema}.${table} FROM STDIN CSV ENCODING '${encoding_name}' DELIMITER '${separator}' QUOTE '${quote}';";	
	private static final String TABLE_REGEX = "${table}";
	private static final String Filename_REGEX = "${filename}";
	private static final String ENCODING_NAME = "${encoding_name}";
	private static final String SEPARATOR = "${separator}";
	private static final String SCHEMA = "${schema}";
	private static final String QUOTE = "${quote}";

	CopyManager copyManager;
	private Connection connection;

	public CSVLoader (Connection aConnection,String aSchema ,String anEncoding_Name, String aSeparator, String aQuote) throws SQLException {
		this.connection = aConnection;
		
		// allow to execute copy on client side.
		copyManager = new CopyManager((BaseConnection)aConnection);
		// build the query.
		SQL_COPY = SQL_COPY.replace(ENCODING_NAME, anEncoding_Name); 
		SQL_COPY = SQL_COPY.replace(SEPARATOR, aSeparator);
		SQL_COPY = SQL_COPY.replace(SCHEMA, aSchema);
		SQL_COPY = SQL_COPY.replace(QUOTE, aQuote);
		
		SQL_COPY_SAVE = SQL_COPY_SAVE.replace(ENCODING_NAME, anEncoding_Name); 
		SQL_COPY_SAVE = SQL_COPY_SAVE.replace(SEPARATOR, aSeparator);
		SQL_COPY_SAVE = SQL_COPY_SAVE.replace(SCHEMA, aSchema);
		SQL_COPY_SAVE = SQL_COPY_SAVE.replace(QUOTE, aQuote);
	}
	
	
	public boolean loadCSV(String csvFile, String tableName) throws NexcapException, Exception {
		connection.setAutoCommit(false);
		InputStreamReader inFile = null;
		try {
			boolean isCorrect = true; 
			String query = SQL_COPY;
			query = query.replace(TABLE_REGEX, tableName); 
			logger.debug(query);
			inFile = new InputStreamReader(new FileInputStream(csvFile), "UTF-8");
			copyManager.copyIn(query,new InputStreamReader(new FileInputStream(csvFile), "UTF-8"));
			return isCorrect; 
			
		} catch (Exception e) {
		    logger.error("loadCSV() KO: " + e, e);
		    throw new NexcapException("loadCSV() KO: " + e, e);
		}finally{
            if (inFile != null) {
            	inFile.close();
            }
		}
	}
	
	public boolean MakeCSV(String csvFile, String tableName) throws NexcapException, IOException{
		FileOutputStream outfile = null;
		try {
			boolean isCorrect = true; 
			String query = SQL_COPY_SAVE;
			query = query.replace(TABLE_REGEX, tableName); 
			logger.debug(query);
			outfile = new FileOutputStream(csvFile);
			copyManager.copyOut(query,outfile);
			return isCorrect; 
		} catch (Exception e) {
		    logger.error("MakeCSV() KO: " + e, e);
		    throw new NexcapException("MakeCSV() KO: " + e, e);
		}finally{
            if (outfile != null) {
            	outfile.close();
            }
		}
	}
}
	
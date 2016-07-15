package dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import mysqlToPostgres.ConnexionDB;
import mysqlToPostgres.Constant;
import mysqlToPostgres.PropertyLoader;
import utils.ActionUtils;

public abstract class BaseDAO implements Constant {

	private static Logger logger = (Logger) LoggerFactory.getLogger(BaseDAO.class);
	
	protected Properties prop;
	protected Connection connecPostgres;
	protected Connection connecMysql;
	protected File repo;
	protected File csvRepo;
	protected String REGEXTRUNCATE = "TRUNCATE {database}.{table} CASCADE;"; 
	protected List<String> presentTableName;
	File repoSavepostgres;
	
	public BaseDAO() throws FileNotFoundException, IOException{
		
		/* Instanciate File Utils  */
		prop = PropertyLoader.load("config.properties");
		repo = new File(this.prop.getProperty("mysqlFolderCSVEXPORT"));
		csvRepo = new File(this.prop.getProperty("postgresFolderCSVEXPORT"));
		REGEXTRUNCATE =  REGEXTRUNCATE.replace(REGEX_DATABASE,this.prop.getProperty("postgresShema"));
		repoSavepostgres = new File(prop.getProperty("postgresToSaveRepo"));
		
		// initiate the connexion.
		try {
			connecMysql = ConnexionDB.ConnexionMySQL(prop);
			connecPostgres = ConnexionDB.ConnexionPostgres(prop);
			ActionUtils.cleanRepo(repo, csvRepo,repoSavepostgres);
			presentTableName = getPresentTableName();
		} catch (ClassNotFoundException | SQLException e) {
			logger.error("BaseDAO() KO: " + e, e);
			// exit the programm because we can't continue.
			System.exit(1);
		}
		
	}
	
	/**
	 * Get the list of all table prensent in the Mysql database.
	 * @return
	 * @throws SQLException
	 */
	private  List<String> getPresentTableName() throws SQLException {

		List<String> listTable = new ArrayList<String>();
		Statement stat = null;
		try {
			stat =  connecMysql.createStatement();
			stat.execute(REGEXLISTTABLE.replace(REGEX_DATABASE, prop.getProperty("MysqlDatabase")));
		} catch (SQLException e) {e.printStackTrace();}
		
		ResultSet  rs= stat.getResultSet();
		// get all tables names.
		while (rs.next()) {
		    	listTable.add(rs.getString(1));
		}
		return listTable;
	}
}

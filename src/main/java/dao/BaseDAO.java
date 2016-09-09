package dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import utils.NexcapException;

public abstract class BaseDAO implements Constant {

	private static Logger logger = (Logger) LoggerFactory.getLogger(BaseDAO.class);
	
	protected Properties prop;
	protected Connection connecPostgres;
	protected Connection connecMysql;
	
	protected File repoCsvMysql;
	protected File repoCsvPostgres;
	protected File sqlDumpFile;
	protected File repoSavepostgres;
	protected File sqlJunitDumpFile;
	
	protected File dataSetMysql;
	protected File dataSetPostgres;
	
	protected String REGEXTRUNCATE = "TRUNCATE {database}.{table} CASCADE;"; 
	protected List<String> presentTableName;
	protected List<String> presentTableNamePostgres;
	
	public BaseDAO() throws FileNotFoundException, IOException{
		
		/* Instanciate Utils */
		prop = PropertyLoader.load("config.properties");
		REGEXTRUNCATE =  REGEXTRUNCATE.replace(REGEX_DATABASE,this.prop.getProperty("postgresShema"));
		repoCsvMysql = new File(this.prop.getProperty("mysqlFolderCSVEXPORT"));
		repoCsvPostgres = new File(this.prop.getProperty("postgresFolderCSVEXPORT"));
		repoSavepostgres = new File(prop.getProperty("postgresToSaveRepo"));
		sqlDumpFile = new File(this.prop.getProperty("mysqlDumpToSave"));
		sqlJunitDumpFile = new File(this.prop.getProperty("mysqlDumpPath"));
		dataSetPostgres= new File(RessourcesFolder,"Dump_Junit_Posgres.sql");
		dataSetMysql= new File(RessourcesFolder,"Dump_Junit.sql");
		
		// initiate the connexion.
		try {
			logger.info("BaseDAO(), initiate BaseDAO object...");
			connecMysql = ConnexionDB.ConnexionMySQL(prop);
			connecPostgres = ConnexionDB.ConnexionPostgres(prop);
			ActionUtils.removeErrorFile();
			ActionUtils.checkFilePresentAndRemove(dataSetPostgres);
			presentTableName = getPresentTableName();
			presentTableNamePostgres = getPresentTableNamePostgres();
			logger.info("BaseDAO(), success");
		} catch (ClassNotFoundException | SQLException e) {
			logger.error("BaseDAO() KO: " + e, e);
			// exit the programm because we can't continue.
			System.exit(1);
		}catch(NexcapException e){
			logger.error("BaseDAO() KO: " + e, e);
		}
		
	}
	
	/**
	 * Get the list of all table prensent in the Mysql database.
	 * @return
	 * @throws SQLException
	 */
	private  List<String> getPresentTableName() throws SQLException {
		List<String> listTable = new ArrayList<String>();
		if (connecMysql != null){
			logger.info("getPresentTableName(), get table list of MysqlDatabase.");
			
			Statement stat = null;
			stat =  connecMysql.createStatement();
			stat.execute(REGEXLISTTABLE.replace(REGEX_DATABASE, prop.getProperty("MysqlDatabase")));
			ResultSet  rs= stat.getResultSet();
			while (rs.next()) {
			    	listTable.add(rs.getString(1));
			}
			logger.info("Success");
			
		}
		return listTable;
	}
	
	protected void reloadListNameTableMysql() throws SQLException{
		presentTableName = getPresentTableName();
	}
	
	protected void reloadListNameTablePgsql() throws SQLException{
		presentTableNamePostgres = getPresentTableNamePostgres();
	}
	
	private  List<String> getPresentTableNamePostgres() throws SQLException {
		
		logger.info("getPresentTableNamePostgres(), get table list of Postgres.");
		List<String> listTable = new ArrayList<String>();
		Statement stat = null;
		stat =  connecPostgres.createStatement();
		stat.execute(REGEXLISTTABLEPOSTGRES.replace(REGEX_DATABASE, prop.getProperty("postgresShema")));
		ResultSet  rs= stat.getResultSet();
		while (rs.next()) {
		    	listTable.add(rs.getString(3));
		}
		logger.info("Success");
		return listTable;
	}
}

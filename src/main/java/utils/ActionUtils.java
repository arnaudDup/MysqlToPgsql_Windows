package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import mysqlToPostgres.Constant;

public class ActionUtils implements Constant {
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(ActionUtils.class);
	
	public static FileWriter fw ;
	public static final String[] nameTempFile = {"nameTempFileForPostgres","nameTempFileForMysql"};
	

	public static void cleanRepo(File repo, File csvRepo, File csvRepoSave) throws IOException{
		cleanRepo(repo,true);
		cleanRepo(csvRepo,true);
		cleanRepo(csvRepoSave,true);
	}
	
	public static void cleanRepo(File repo, boolean recreate)throws IOException {
		if(repo.exists()){
			logger.info("cleanRepo, suppresion de "+repo.getAbsolutePath());
			FileUtils.deleteDirectory(repo);
		}
		if(recreate){
			repo.mkdirs();
		}
	}

	public static void checkFilePresentAndRemove(File testFile) throws IOException{
		
		if(testFile.exists()){
			testFile.delete();
		}
		testFile.createNewFile();
	}
	
	public static  void deleteFile(int databseSelect) throws IOException{
		File tempFile = new File(RessourcesFolder,nameTempFile[databseSelect]);
		fw.close();
		if(tempFile.delete()){
			logger.debug(tempFile.getName() + " is deleted!");
		}else{
			logger.debug("Delete operation is failed.");
		}
	   
	}
	
	public static File createFileContainString(String StringToAdd,int databseSelect ) throws IOException{
		File tempFile = new File(RessourcesFolder,nameTempFile[databseSelect]);
		tempFile.createNewFile();
		
		fw = new FileWriter(tempFile, true);
		BufferedWriter output = new BufferedWriter(fw);
		output.write(StringToAdd);
		output.write(System.getProperty("line.separator"));
		output.flush();
		output.close();
		return tempFile;	
	}
}

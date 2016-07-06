package Transform.MY;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException{
		ConnexionDB connexion = new ConnexionDB();
		connexion.MakeCsvFromDataMysqlDatabase(true);
		connexion.TruncateAllTableInPostgresDatabase ();
		connexion.loadDataINTOPostgresdatabase();
    }
}
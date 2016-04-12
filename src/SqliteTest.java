import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.sql.*;
import java.time.chrono.MinguoChronology;
import java.util.Arrays;

/**
 * Created by luigi on 12/04/2016.
 */
public class SqliteTest {
    public static void main( String args[] )
    {
        //Connection secondaryDb;
        //Statement statement;
        //ResultSet resultSet;

        String basePath = "C:\\resources\\";

        //file list
        System.out.println("db list");
        File dir = new File(basePath);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".db");
            }
        });
        System.out.println(Arrays.toString(files));

        try {
            /* copy everything in the first db */
            Class.forName("org.sqlite.JDBC");
            Connection mainDb = DriverManager.getConnection("jdbc:sqlite:" + files[0]);

            for(int i = 1 ; i < files.length ; i++) {
                System.out.println("db: " + files[i]);

                //secondaryDb = DriverManager.getConnection("jdbc:sqlite:" + files[i]);
                mainDb.prepareStatement("ATTACH DATABASE \"" + files[i] + "\" AS fromDB").execute();

                mainDb.prepareStatement("INSERT INTO test(Field2, Field3)" +
                        "SELECT Field2, Field3 FROM fromDB.test").execute();

                mainDb.prepareStatement("DETACH DATABASE fromDB").execute();
                /* print the content of a DB
                statement = secondaryDb.createStatement();
                resultSet = statement.executeQuery("SELECT * FROM test");

                while (resultSet.next()) {
                    System.out.println(resultSet.getString("Field1") + "\t" +
                            resultSet.getString("Field2") + "\t" +
                            resultSet.getString("Field3") + "\t");
                }
                resultSet.close();
                statement.close();
                secondaryDb.close();*/
            }

            mainDb.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}

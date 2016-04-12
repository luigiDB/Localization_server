import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by Luigi on 12/04/2016.
 */
public class DatabaseHelper {

    private String basePath;

    // Contacts table name
    private static final String TABLE_MEASURES = "campioni";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_ID_MEASURE = "id_measure";
    private static final String KEY_EDIFICIO = "edificio";
    private static final String KEY_PIANO = "piano";
    private static final String KEY_AULA = "aula";
    private static final String KEY_BSSID = "bssid";
    private static final String KEY_SSID = "ssid";
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_RSSI = "rssi";

    public DatabaseHelper(String path) {
        basePath = path;
    }

    public int mergeDb(){
        System.out.println("MergeDb");
        File[] files = getDbList();

        if(files.length == 1) {
            return 1;
        }

        try {
            Connection mainDb = DriverManager.getConnection("jdbc:sqlite:" + files[0]);

            for (int i = 1; i < files.length; i++) {
                System.out.println("db: " + files[i]);

                mainDb.prepareStatement("ATTACH DATABASE \"" + files[i] + "\" AS fromDB").execute();

                String query = "INSERT INTO " + TABLE_MEASURES +
                        "(" + KEY_ID_MEASURE + ", " + KEY_EDIFICIO + ", " + KEY_PIANO + ", " + KEY_AULA + ", " + KEY_BSSID + ", " + KEY_SSID + ", " +  KEY_FREQUENCY + ", " + KEY_RSSI + ")" +
                        "SELECT " + KEY_ID_MEASURE + ", " + KEY_EDIFICIO + ", " + KEY_PIANO + ", " + KEY_AULA + ", " + KEY_BSSID + ", " + KEY_SSID + ", " +  KEY_FREQUENCY + ", " + KEY_RSSI + " FROM fromDB." + TABLE_MEASURES;

                mainDb.prepareStatement(query).execute();

                mainDb.prepareStatement("DETACH DATABASE fromDB").execute();

                if(!files[i].delete()) {
                    return 0;
                }
            }

            mainDb.close();
        } catch (Exception e) {
            System.err.println( "ERROR: " + e.getClass().getName() + ": " + e.getMessage() );
            return -1;
        }
        return 1;
    }

    public File[] getDbList() {
        System.out.println("getDbList");
        File dir = new File(basePath);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".db");
            }
        });
        return files;
    }

    public ArrayList<String> getBuildings() {
        System.out.println("getBuildings");
        File fileDb = getDbList()[0];
        ArrayList<String> res = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + fileDb);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT distinct(" + KEY_EDIFICIO +") as ret\n" +
                    "FROM " + TABLE_MEASURES);
            while(resultSet.next()) {
                res.add(resultSet.getString("ret"));
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println( "ERROR: " + e.getClass().getName() + ": " + e.getMessage() );
        }

        return res;
    }

    public ArrayList<String> getBssid(String building) {
        System.out.println("getBssid");
        File fileDb = getDbList()[0];
        ArrayList<String> res = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + fileDb);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT distinct(" + KEY_BSSID + ") as ret\n" +
                    "FROM " + TABLE_MEASURES + "\n" +
                    "WHERE " + KEY_EDIFICIO + " = \"" + building + "\"");
            while(resultSet.next()) {
                res.add(resultSet.getString("ret"));
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println( "ERROR: " + e.getClass().getName() + ": " + e.getMessage() );
        }

        return res;
    }

    public int getNumberOfBuildings() {
        System.out.println("getNumberOfBuildings");
        File fileDb = getDbList()[0];
        int res = 0;
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + fileDb);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT count(distinct(" + KEY_EDIFICIO +")) as ret\n" +
                    "FROM " + TABLE_MEASURES);
            while(resultSet.next()) {
                res = resultSet.getInt("ret");
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println( "ERROR: " + e.getClass().getName() + ": " + e.getMessage() );
        }

        return res;
    }

    public ArrayList<String> getRoomList(String building) {
        System.out.println("getRoomList");
        File fileDb = getDbList()[0];
        ArrayList<String> res = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + fileDb);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT distinct(" + KEY_EDIFICIO + ") as building, " + KEY_PIANO + ", " + KEY_AULA + "\n" +
                    "FROM " + TABLE_MEASURES + "\n" +
                    "WHERE " + KEY_EDIFICIO + " = '" + building + "'");
            while(resultSet.next()) {
                res.add(resultSet.getString("building") + "_" + resultSet.getString(KEY_PIANO) + "_" + resultSet.getString(KEY_AULA));
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println( "ERROR: " + e.getClass().getName() + ": " + e.getMessage() );
        }

        return res;
    }
}

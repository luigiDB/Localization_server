import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Luigi on 12/04/2016.
 */

//TODO: dato che tutte le funzioni chimano la getDbList()[0] potrei anche fare in modo che avvenga in automatico la merge nel caso in cui getDbList().length>1
//TODO: fare in modo che se si passa un oggetto Statement alla funzione non lo rialloca ogni volta

/**
 * this class handle the database connection and query
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

    /**
     * constructor
     * @param path path of the dbs
     */
    public DatabaseHelper(String path) {
        if(path == null) {
            System.err.println("DatabaseHelper: bad parameters");
            return;
        }
        basePath = path;
    }


    /**
     * merge all file *.db in the folder in only one file
     * @return 1: only one file remains, 0: some error in deleting a file, -1: some exception
     */
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


    /**
     * return the list of the db in the folder
     * @return list of File
     */
    public File[] getDbList() {
        File dir = new File(basePath);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".db");
            }
        });
        return files;
    }


    /**
     * return list of the buildings in the db
     * @return null: some error in db connection, !null: an ArrayList<String> containing buildings
     */
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
            return null;
        }

        return res;
    }


    /**
     * return list of the bssid in the given building
     * @param building name of the building (Must be exact)
     * @return null: some error in db connection, !null: an ArrayList<String> containing bssid
     */
    public ArrayList<String> getBssid(String building) {
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
            return null;
        }

        return res;
    }


    /**
     * return the number of buildings in the db
     * @return -1: some error in db connection, !-1: number of buildings
     */
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
            return -1;
        }

        return res;
    }


    /**
     * return list of the rooms in the given building
     * each room is a string containing building, floor and name of the room separated by "_"
     * @param building name of the building (Must be exact)
     * @return null: some error in db connection, !null: an ArrayList<String> containing rooms => building_floor_room
     */
    public ArrayList<String> getRoomList(String building) {
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
            return null;
        }

        return res;
    }


    /**
     * return list of the experiments in the given building, room and floor
     * @param building name of the building (Must be exact)
     * @param floor name of the floor (Must be exact)
     * @param room name of the room (Must be exact)
     * @return null: some error in db connection, !null: an ArrayList<String> containing experiments
     */
    public ArrayList<String> getExperiments(String building, String floor, String room) {
        File fileDb = getDbList()[0];
        ArrayList<String> res = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + fileDb);
            Statement statement = connection.createStatement();
            String query = "SELECT distinct(" + KEY_ID_MEASURE +") as ret\n" +
                    "FROM " + TABLE_MEASURES + "\n" +
                    "WHERE " + KEY_EDIFICIO + " = \"" + building + "\" " +
                    "AND " + KEY_PIANO + " = \"" + floor + "\" " +
                    "AND " + KEY_AULA + " = \"" + room + "\"";
            //System.out.println("QUERY: " + query);
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                res.add(resultSet.getString("ret"));
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println( "ERROR: " + e.getClass().getName() + ": " + e.getMessage() );
            return null;
        }

        return res;
    }


    /**
     * return list of the measurements in the given experiment
     * @param experiment name of the experiment (Must be exact)
     * @return null: some error in db connection, !null: an LinkedHashMap<bssid, rssi> containing measures
     */
    public LinkedHashMap<String, String> getMeasurments(String experiment) {
        //System.out.println("getMeasurments");
        File fileDb = getDbList()[0];
        LinkedHashMap<String, String> res = new LinkedHashMap<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + fileDb);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT " + KEY_BSSID + ", " + KEY_RSSI + "\n" +
                    "FROM " + TABLE_MEASURES + "\n" +
                    "WHERE " + KEY_ID_MEASURE + " = \"" + experiment + "\"");
            while(resultSet.next()) {
                //res.add(resultSet.getString("ret"));
                res.put(resultSet.getString(KEY_BSSID), resultSet.getString(KEY_RSSI));
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println( "ERROR: " + e.getClass().getName() + ": " + e.getMessage() );
            return null;
        }

        return res;
    }
}

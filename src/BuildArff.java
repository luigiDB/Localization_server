import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by Luigi on 12/04/2016.
 */

/**
 * This class handle the data to compile an arff file.
 * Data are write in memory by a FileHelper object.
 */
public class BuildArff {

    private DatabaseHelper databaseHelper = null;
    private String destinationPath = null;
    private String fileName = null;
    private BuildingsInformations buildingsInformations;

    /**
     * constructor
     * @param dh DatabaseHelper object
     * @param name Arff file name
     * @param bi BuildingsInformations object
     * @param dest destination path
     */
    public BuildArff(DatabaseHelper dh, String name, BuildingsInformations bi, String dest) {
        if(dh==null || name==null || bi==null || dest==null) {
            System.err.println("BuildArff: bad parameters");
            return;
        }
        databaseHelper = dh;
        fileName = name;
        destinationPath = dest;
        buildingsInformations = bi;
    }


    /**
     * constructor
     * @param dh DatabaseHelper object
     * @param name Arff file name
     * @param bi BuildingsInformations object
     */
    public BuildArff(DatabaseHelper dh, String name, BuildingsInformations bi) {
        if(dh==null || name==null || bi==null) {
            System.err.println("BuildArff: bad parameters");
            return;
        }
        databaseHelper = dh;
        fileName = name;
        buildingsInformations = bi;
    }


    /**
     * export all the needed arff file one for each building
     * @return true: no error, false. otherwise
     */
    public boolean exportArffFiles() {
        System.out.println("exportArffFiles");

        //be sure that there is only one db
        if(databaseHelper.mergeDb() != 1) {
            return false;
        }

        //get buildings
        ArrayList<String> buildings = databaseHelper.getBuildings();
        System.out.println(buildings.toString());
        System.out.println("\n");
        boolean res = true;
        for(String building: buildings) {
            res = exportArffFile(building);
            if(!res) {
                return false;
            }
        }
        return res;
    }


    /**
     * create the arff file for the specified building
     * @param building building name
     * @return true: no error, false: some error
     */
    private boolean exportArffFile(String building) {
        System.out.println("EXPORT: " + building);
        //get bssid list for building
        ArrayList<String> bssidList =  databaseHelper.getBssid(building);
        //load in the buildingInformations
        if(!buildingsInformations.addBuildingWithInfo(building, bssidList)) {
            return false;
        }

        //get room list for building
        ArrayList<String> roomList = databaseHelper.getRoomList(building);
        //roomList may contains spaces that need to filled with -
        System.out.println("roomlist 1: " + roomList.toString());
        replaceString(roomList, " ", "-");
        System.out.println("roomlist 2: " + roomList.toString());

        FileHelper parser;
        if(destinationPath == null) {
            parser = new FileHelper(fileName + "_" + building.replace(" ", "-") + ".arff");
        } else {
            parser = new FileHelper(fileName + "_" + building.replace(" ", "-") + ".arff", destinationPath);
        }
        //TODO: room names in roomlist are filled with "-" since weka doesn't accept spaces but in the next step of this function i need to do the opposite so remember to move the replacing function to the computeAttributes function
        computeAttributes(parser, bssidList, roomList);


        //Access room [Building]
        for(String room: roomList) {
            String[] roomPosition = room.split("_");        //building_floor_room
            ArrayList<String> experimentsId = databaseHelper.getExperiments(roomPosition[0].replaceAll("-", " "), roomPosition[1], roomPosition[2]);    //building, floor, room name

            //Access sample (collection of measurements) [Building [Room]]
            for(String experimentId: experimentsId) {
                //String[MAC1_SSID, MAC2_SSID, MAC3_SSID, ..., class] for each experiment
                LinkedHashMap<String, String> measures = databaseHelper.getMeasurments(experimentId);

                //Access measurement [Building [Room [Sample]]]
                String[] train = computeMeasurementArray(measures, bssidList, room);
                if(train == null) {
                    return false;
                }

                //parse the array in the arff file
                parser.writeDataRow(train);
            }
        }

        parser.closeFile();
        return true;
    }


    /**
     * Compute a train sample for weka with all sensed value for each bssid in the building and the relative class
     * @param measures LinkedHashMap of bssid and rssi
     * @param bssidList list of all possible bssid
     * @param room class
     * @return null: bssid not found in bssidlist (logical error state that should not exist), !null: train string for weka
     */
    public static String[] computeMeasurementArray(LinkedHashMap<String, String> measures, ArrayList<String> bssidList, String room) {
        // allocate an array lenght able to contain all bssid and one class
        String[] train =  new String[bssidList.size() + 1];
        Arrays.fill(train, "0");

        //fill the bssid cells
        for(String bssid: measures.keySet()){
            String rssi = measures.get(bssid);

            //insert measure in the array
            int index = getIndex(bssidList, bssid);
            if(index < 0) {
                return null;
            }
            if(!rssi.equals("")) {  //maintain zero in the array in case of null value in the array
                train[index] = rssi;
            }
        }

        //fill the class cell
        if(room != null) {
            train[train.length - 1] = room;
        }

        return train;
    }


    /**
     * Compute first part of the arff file: attribute and class definition
     * @param parser FileHelper
     * @param bssidList ArrayList<String> of bssid
     * @param roomList ArrayList<String> of rooms
     */
    private void computeAttributes(FileHelper parser, ArrayList<String> bssidList, ArrayList<String> roomList) {
        LinkedHashMap<String, String> attributeList = new LinkedHashMap<>();
        for(String bssid: bssidList) {
            attributeList.put(bssid, "real");
        }
        attributeList.put("class", computeClassValues(roomList));

        //send to parser
        parser.setAttributes(attributeList);
    }


    /**
     * given an ArrayList<String> return a string containing the elements of the ArrayList
     * separated by , and enclosured by {}
     * @param roomList ArrayList<String>
     * @return String = {a, b, c}
     */
    private String computeClassValues(ArrayList<String> roomList) {
        String nominalValue = "{";
        for(int i = 0 ; i < roomList.size() ; i++) {
            nominalValue += roomList.get(i);
            if(i != (roomList.size()-1)) {
                nominalValue += ",";
            }
        }
        nominalValue += "}";
        System.out.println("\n\nNOMINAL VALUE: " + nominalValue);
        return nominalValue;
    }


    /**
     * Given a list and an element return the index of that element in the list
     * @param list ArrayList<String>
     * @param elem String representing the elem
     * @return -1: elem not found in the list, >=0: index of the element in list
     */
    private static int getIndex(ArrayList<String> list, String elem) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(elem)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * replace all originChar in an ArrayList<String> with newChar
     * @param list list of strings where a string must be changed
     * @param originChar character that must be found
     * @param newChar character that must be replaced
     */
    private void replaceString(ArrayList<String> list, String originChar, String newChar) {
        /*for(String elem: list) {
            elem = elem.replaceAll(originChar, newChar);
        }*/
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).replaceAll(originChar, newChar));
        }
    }
}

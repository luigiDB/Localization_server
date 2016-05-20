import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by Luigi on 12/04/2016.
 */
public class BuildArff {

    private DatabaseHelper databaseHelper = null;
    private String destinationPath = null;
    private String fileName = null;
    private BuildingsInformations buildingsInformations;

    public BuildArff(DatabaseHelper dh, String name, BuildingsInformations bi, String dest) {
        databaseHelper = dh;
        fileName = name;
        destinationPath = dest;
        buildingsInformations = bi;
    }

    public BuildArff(DatabaseHelper dh, String name, BuildingsInformations bi) {
        databaseHelper = dh;
        fileName = name;
        buildingsInformations = bi;
    }

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

    private boolean exportArffFile(String building) {
        System.out.println("EXPORT: " + building);
        //get bssid list for building
        ArrayList<String> bssidList =  databaseHelper.getBssid(building);
        //load in the buildingInformations
        if(!buildingsInformations.addBuildingWithInfo(building.replaceAll(" ", "&"), bssidList)) {
            return false;
        }

        //get room list for building
        ArrayList<String> roomList = databaseHelper.getRoomList(building);
        //roomList may contains spaces that need to filled with -
        System.out.println("roomlist 1: " + roomList.toString());
        replaceString(roomList, " ", "-");
        System.out.println("roomlist 2: " + roomList.toString());
//        if(databaseHelper.mergeDb() != 1) {
//            return false;
//        }

        ARFFParser parser;
        if(destinationPath == null) {
            parser = new ARFFParser(fileName + "_" + building + ".arff");
        } else {
            parser = new ARFFParser(fileName + "_" + building + ".arff", destinationPath);
        }
        computeAttributes(parser, bssidList, roomList);



        //Access room [Building]
        for(String room: roomList) {
            String[] roomPosition = room.split("_");
            ArrayList<String> experimentsId = databaseHelper.getExperiments(roomPosition[0].replaceAll("-", " "), roomPosition[1], roomPosition[2]);

            //Access sample (collection of measurements) [Building [Room]]
            for(String experimentId: experimentsId) {
                //String[MAC1_SSID, MAC2_SSID, MAC3_SSID, ..., class] for each experiment
                LinkedHashMap<String, String> measures;
                measures = databaseHelper.getMeasurments(experimentId);

                //Access measurement [Building [Room [Ssmple]]]
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

    public static String[] computeMeasurementArray(LinkedHashMap<String, String> measures, ArrayList<String> bssidList, String room) {
        String[] train =  new String[bssidList.size() + 1];
        Arrays.fill(train, "0");
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

        if(room != null) {
            train[train.length - 1] = room;
        }

        return train;
    }

    private void computeAttributes(ARFFParser parser, ArrayList<String> bssidList, ArrayList<String> roomList) {
        LinkedHashMap<String, String> attributeList = new LinkedHashMap<>();
        for(String bssid: bssidList) {
            attributeList.put(bssid, "real");
        }
        attributeList.put("class", computeClassValues(roomList));

        //send to parser
        parser.setAttributes(attributeList);
    }

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

    private static int getIndex(ArrayList<String> list, String elem) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(elem)) {
                return i;
            }
        }
        return -1;
    }

    private void replaceString(ArrayList<String> list, String originChar, String newChar) {
        /*for(String elem: list) {
            elem = elem.replaceAll(originChar, newChar);
        }*/
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).replaceAll(originChar, newChar));
        }
    }
}

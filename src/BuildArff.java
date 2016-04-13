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

    public BuildArff(DatabaseHelper dh, String name, String dest) {
        databaseHelper = dh;
        fileName = name;
        destinationPath = dest;
    }

    public BuildArff(DatabaseHelper dh, String name) {
        databaseHelper = dh;
        fileName = name;
    }

    public boolean exportArffFiles() {
        ArrayList<String> buildings = databaseHelper.getBuildings();
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
        ArrayList<String> bssidList =  databaseHelper.getBssid(building);
        ArrayList<String> roomList = databaseHelper.getRoomList(building);

        if(databaseHelper.mergeDb() != 1) {
            return false;
        }

        ARFFParser parser;
        if(destinationPath == null) {
            parser = new ARFFParser(fileName + "_" + building + ".arff");
        } else {
            parser = new ARFFParser(fileName + "_" + building + ".arff", destinationPath);
        }


        computeAttributes(parser, bssidList, roomList);


        //String[MAC1_SSID, MAC2_SSID, MAC3_SSID, ..., class] for each experiment
        String[] train =  new String[bssidList.size() + 1];
        LinkedHashMap<String, String> measures = null;

        //Access room [Building]
        for(String room: roomList) {
            String[] roomPosition = room.split("_");
            ArrayList<String> experimentsId = databaseHelper.getExperiments(roomPosition[0], roomPosition[1], roomPosition[2]);

            //Access sample (collection of measurements) [Building [Room]]
            for(String experimentId: experimentsId) {
                Arrays.fill(train, "0");
                train[train.length - 1] = room;

                measures = databaseHelper.getMeasurments(experimentId);

                //Access measurement [Building [Room [Ssmple]]]
                for(String bssid: measures.keySet()){
                    String rssi = measures.get(bssid);

                    //insert measure in the array
                    int index = getIndex(bssidList, bssid);
                    if(index < 0) {
                        return false;
                    }
                    if(!rssi.equals("")) {  //maintain zero in the array in case of null value in the array
                        train[index] = rssi;
                    }
                }
                //parse the array in the arff file
                parser.writeDataRow(train);
            }
        }


        parser.closeFile();
        return true;
    }

    private void computeAttributes(ARFFParser parser, ArrayList<String> bssidList, ArrayList<String> roomList) {
        LinkedHashMap<String, String> attributeList = new LinkedHashMap<>();
        for(String bssid: bssidList) {
            attributeList.put(bssid, "string");
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

    private int getIndex(ArrayList<String> list, String elem) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(elem)) {
                return i;
            }
        }
        return -1;
    }
}

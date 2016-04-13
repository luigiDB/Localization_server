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

        ARFFParser parser;
        if(destinationPath == null) {
            parser = new ARFFParser(fileName);
        } else {
            parser = new ARFFParser(fileName, destinationPath);
        }

        //////////////////////////////////////
        //compute attributes in a LinkedHashMap
        LinkedHashMap<String, String> attributeList = new LinkedHashMap<>();
        for(String bssid: bssidList) {
            attributeList.put(bssid, "string");
        }
        attributeList.put("class", computeClassValues(roomList));

        //send to parser
        parser.setAttributes(attributeList);
        /////////////////////////////////////

        //String[MAC1_SSID, MAC2_SSID, MAC3_SSID, ..., class] for each experiment
        String[] train =  new String[roomList.size() + 1];
        LinkedHashMap<String, String> measures = null;

        ArrayList<String> experimentsId = databaseHelper.getExperiments(building);
        for(String experimentId: experimentsId) {
            Arrays.fill(train, "0");

            measures = databaseHelper.getMeasurments(experimentId);
            for(String bssid: measures.keySet()){
                String ssid = measures.get(bssid);

                int index = getIndex(bssidList, bssid);
                train[index] = ssid;
            }
            parser.writeDataRow(train);
        }

        parser.closeFile();
        return true;
    }

    private String computeClassValues(ArrayList<String> roomList) {
        String nominalValue = "{";
        for(int i = 0 ; i < roomList.size() ; i++) {
            nominalValue.concat(roomList.get(i).toString());
            if(i != (roomList.size()-1)) {
                nominalValue.concat(",");
            }
        }
        nominalValue.concat("}");
        return nominalValue;
    }

    private int getIndex(ArrayList<String> list, String elem) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(elem)) {
                return i;
                break;
            }
        }
    }
}

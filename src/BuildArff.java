import java.util.ArrayList;
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

        //compute attributes in a LinkedHashMap
        LinkedHashMap<String, String> attributeList = new LinkedHashMap<>();
        for(String bssid: bssidList) {
            attributeList.put(bssid, "string");
        }
        attributeList.put("class", computeClassValues(roomList));

        //send to parser
        parser.setAttributes(attributeList);

        //compute String[] for each experiment




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
}

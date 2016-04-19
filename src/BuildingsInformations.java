import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by luigi on 19/04/2016.
 */
public class BuildingsInformations {
    LinkedHashMap<String, ArrayList<String>> buildings;

    public BuildingsInformations() {
        buildings = new LinkedHashMap<>();
    }

    public boolean addBuildingWithInfo(String building, ArrayList<String> bssidList) {
        boolean check = checkIfBuildingExist(building);
        if(check) {     //cannot enter the building that already exist
            return false;
        }
        buildings.put(building, bssidList);
        return true;
    }


    public String getBuilding(String bssid) {
        for(String b: buildings.keySet()){
            ArrayList<String> bssidList= buildings.get(b);
            for(String bssidTemp: bssidList) {
                if(bssidTemp.equals(bssid)) {
                    return b;
                }
            }
        }
        return null;
    }


    public ArrayList<String> getBssidList(String building) {
        return buildings.get(building);
    }


    private boolean checkIfBuildingExist(String building) {
        for(String b: buildings.keySet()){
            if(b.equals(building)) {
                return true;
            }
        }
        return false;
    }
}

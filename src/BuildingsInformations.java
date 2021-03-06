import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by luigi on 19/04/2016.
 */

/**
 * This class handle the know building information in local
 * without relying on the db
 */
public class BuildingsInformations {

    // building, bssidList
    private LinkedHashMap<String, ArrayList<String>> buildings;


    /**
     * constructor
     * instantiate necessary objects
     */
    public BuildingsInformations() {
        buildings = new LinkedHashMap<>();
    }


    /**
     * return private LinkedHashMap of building as ArrayList
     * @return ArrayList of buildings
     */
    public ArrayList<String> getBuildingList() {
        ArrayList<String> temp = new ArrayList<>();
        for(String b: buildings.keySet()) {
            temp.add(b);
        }
        return temp;
    }


    /**
     * add a building to the class only if its new
     * @param building building that i want to insert
     * @param bssidList bssid list of the building
     * @return true: new building added, false: building already known
     */
    public boolean addBuildingWithInfo(String building, ArrayList<String> bssidList) {
        boolean check = checkIfBuildingExist(building);
        if(check) {     //cannot enter the building that already exist
            return false;
        }
        buildings.put(building, bssidList);
        return true;
    }


    //TODO: la funzione ritorna solo la prima occorrenza; andrebbe quindi modificata per riportare tutti gli edifici corrispondenti per esempio ritornando String[]
    /**
     * return building associated with a bssid
     * @param bssid bssid of the query
     * @return null: bssid not present, !null: the name of the associated buidlings
     */
    public String[] getBuildings(String bssid) {
        ArrayList<String> possibleBuildings = new ArrayList<>();

        for(String b: buildings.keySet()){
            ArrayList<String> bssidList= buildings.get(b);
            for(String bssidTemp: bssidList) {
                if(bssidTemp.equals(bssid)) {
                    possibleBuildings.add(b);
                }
            }
        }
        return possibleBuildings.toArray(new String[possibleBuildings.size()]);
    }


    /**
     * return the list of bssid associated to a building
     * @param building building of the query
     * @return ArrayList<String> of the bssid
     */
    public ArrayList<String> getBssidList(String building) {
        return buildings.get(building);
    }


    /**
     * check if the passed building is already known
     * @param building building name used for the query
     * @return true: building is already known, false: building is non known
     */
    private boolean checkIfBuildingExist(String building) {
        return buildings.containsKey(building);
        /*for(String b: buildings.keySet()){
            if(b.equals(building)) {
                return true;
            }
        }
        return false;*/
    }


    /**
     * Return a LinkedHashMap with key all the possible buildings and an Integer set to 0
     * @return LinkedHashMap
     */
    public LinkedHashMap<String, Integer> getCountStructure() {
        LinkedHashMap<String, Integer> res = new LinkedHashMap<>();
        for(String building: buildings.keySet()) {
            res.put(building, 0);
        }

        return res;
    }
}

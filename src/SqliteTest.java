import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static java.lang.Thread.sleep;

/**
 * Created by luigi on 12/04/2016.
 */
public class SqliteTest {

    //buildingIndexes is needed to know in which clsList index is stored each building classifier
    private static LinkedHashMap<String, Integer> buildingIndexes;
    private static ArrayList<ClassifierService> clsList;
    private static BuildingsInformations bi;
    private static final int NUM_SAMPLES = 5;
    private static String ip;


    /**
     * Given a sample compute the most probable building and classify
     * @param sample LinkedHashMap<String, String> of the sampled data
     * @return classifier result aka. room
     */
    public static String ClassificationProcess(LinkedHashMap<String, String> sample){

        //find the most probable building for the sample
        LinkedHashMap<String, Integer> counters = bi.getCountStructure();
        for(String bssid: sample.keySet()) {
            //String rssi = sample.get(bssid);
            String possibleBuildings[] = bi.getBuildings(bssid);
            System.out.println("possibleBuildings for " + bssid + ": " + Arrays.toString(possibleBuildings));
            for(String building: possibleBuildings) {
                counters.put(building, counters.get(building)+1);
            }
        }
        //find the majority building
        String building = mostProbableBuilding(counters);
        System.out.println("SELECTED BUILDING: " + building);

        // compute the input for weka classifier
        ArrayList<String> bssid = bi.getBssidList(building);
        String[] trainArray = BuildArff.computeMeasurementArray(sample, bssid, null);
        if(trainArray == null) {
            return "Not found";
        }
        System.out.println("Visual check of created sample:");

        for(String str : trainArray)
            System.out.print(str + " ");
        System.out.println();


        //select of the classifier and classification
        int index = buildingIndexes.get(building);
        String result = clsList.get(index).classify(trainArray);
        return result;
    }


    /**
     * Given a LinkedHashMap<String, Integer> return the string with the higher Integer
     * @param list input LinkedHashMap
     * @return winner string
     */
    private static String mostProbableBuilding(LinkedHashMap<String, Integer> list) {
        String winner = "";
        int max = 0;
        for(String elem: list.keySet()) {
            int temp = list.get(elem);
            if(temp > max) {
                winner = elem;
            }
        }

        return winner;
    }


    /**
     * Given an array of string return the most recurrent value.
     * This is used to obtain much higher confidence of the result.
     * @param input Array of string
     * @return most recurrent string
     */
    private static String findMajority(String[] input) {
        int[] occurrences = new int[input.length];
        int indexMax = 0;
        int maxValue = 0;
        double confidenceLevel = 0;
        for(int i = 0; i < input.length; i++){
            for(int j = i + 1; j < input.length; j++){
                if(input[i].equals(input[j]))
                    occurrences[i]++;
            }
        }
        for(int i = 0; i < input.length; i++){
            if(occurrences[i] > maxValue){
                maxValue = occurrences[i];
                indexMax = i;
            }
        }
        confidenceLevel = ((double)maxValue / (double)input.length) * 100;
        return input[indexMax] + "\t" +  confidenceLevel + "%";
    }


    /**
     * return global ip of the current machine
     * @return ip as a string
     * @throws IOException in case of web service malfunction
     */
    private static boolean getIp(){
        URL whatismyip = null;
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

            ip = in.readLine(); //you get the IP as a String
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("Error in web service connection.");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in web service response parsing.");
            return false;
        }

        return true;
    }


    public static void main( String args[] )
    {
        //update ip on no-ip
        NoIP n = new NoIP("spada.elfica@gmail.com","nzor4csv4");
        n.submitHostname("ciaoasdfghjkl.ddns.net");

        /*------------------------------TEST CLASSE DatabaseHelper and BuildingsInfo.*/
        //merge and generate arff files
        String basePath = "C:\\resources\\";
        String baseName = "base";

        DatabaseHelper dh = new DatabaseHelper(basePath);

        //export arff files
        bi = new BuildingsInformations();
        BuildArff ba = new BuildArff(dh, baseName, bi, basePath);
        ba.exportArffFiles();

        /*
        //testing the classifier
        LinkedHashMap<String, String> sample;       //need to initialized
        //find the building by searching for the first bssid in sample
        String building = bi.getBuilding(sample.entrySet().iterator().next());
        ArrayList<String> bssid = bi.getBssidList(building);
        String[] trainArray = BuildArff.computeMeasurementArray(sample, bssid, null);

        System.out.println("Visual check of created sample: \n" + trainArray.toString());
        */


        /*-----------------------------------TEST CLASSE ClassifierService
        ClassifierService cls = new ClassifierService();
        cls.buildClassifier(basePath + "baruffa_polo_c.arff");
        String test = "0,0,-80,0,0,-92,-60,0,0,0,-79,0,0,0,0,0,0,-87,0,-81,0,0,0,0,0,0,0,0,-84,-69,0,0,0,-87,0,-84,-84,-84,0,0,-84,-78,0,-78,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
        test += ",0";     //aggiungo artificialmente uno zero dove dovrebbe esserci invece la classe "polo_c_2_2", ma che non c'è perchè è da classificare
        String[] parsed = test.split(",");
        System.out.println(cls.classify(parsed));
        */


        /*-----------------------------------TEST CLASSE ServerHelper con lato client*/

        //TODO: this must be changed and need to pickup the correct arff file based on building
        /*
        ClassifierService cls = new ClassifierService();
        cls.buildClassifier(basePath + "baruffa_casa-giulio.arff");
        */
        //list method
        clsList = new ArrayList<>();
        buildingIndexes = new LinkedHashMap<>();
        ClassifierService temp;
        int counter = 0;
        for(String building: bi.getBuildingList()) {
            System.out.println("create classifier for: " + building);
            temp = new ClassifierService();
            temp.buildClassifier(basePath + baseName + "_" + building.replaceAll(" ", "-") + ".arff");

            //add classifier
            clsList.add(temp);

            //add building index
            buildingIndexes.put(building, counter);
            counter += 1;
        }

        ServerHelper socket = new ServerHelper(8888);
        System.out.println("Server is listening");
        String[] results = new String[NUM_SAMPLES];
        while(socket.acceptNewClient()){
            System.out.println("New client is arrived!");
            for(int i = 0; i < NUM_SAMPLES; i++) {
                LinkedHashMap<String, String> sample = socket.readClientRecord();
                //sample <bssid, rssi>
                System.out.println(sample.toString());


                results[i] = ClassificationProcess(sample);
                System.out.println(results[i]);
            }
            String result = findMajority(results);
            socket.writeSingleLine(result);
            System.out.println("Server response: " + result);
            socket.closeClient();
        }

    }


}

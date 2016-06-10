import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    String basePath;
    private static Lock lock;


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
        lock.lock();
        String result;
        try {
            int index = buildingIndexes.get(building);
            result = clsList.get(index).classify(trainArray);
        } finally {
            lock.unlock();
        }
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
            occurrences[i] = 1;
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
        return input[indexMax] + " " +  confidenceLevel + "%";
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


    private static void createClassifier(String basePath, String baseName, BuildArff ba) {
        ClassifierService temp;
        int counter = 0;

        //
        if(!ba.deleteArffFiles()) {
            System.out.println("Impossible to delete some arff file");
            return;
        }
        ba.exportArffFiles();

        lock.lock();
        try {
            //blank previous structures
            clsList.clear();
            buildingIndexes.clear();

            //
            for (String building : bi.getBuildingList()) {
                System.out.println("create classifier for: " + building);
                temp = new ClassifierService();
                temp.buildClassifier(basePath + baseName + "_" + building.replaceAll(" ", "-") + ".arff");

                //add classifier
                clsList.add(temp);

                //add building index
                buildingIndexes.put(building, counter);
                counter += 1;
            }
        } finally {
            lock.unlock();
        }
    }


    public static void main( String args[] )
    {
        //create lock
        lock = new ReentrantLock();

        String basePath = "C:\\resources\\";
        String baseName = "base";

        DatabaseHelper dh = new DatabaseHelper(basePath);

        lock.lock();
        try {
            clsList = new ArrayList<>();
            buildingIndexes = new LinkedHashMap<>();
        } finally {
            lock.unlock();
        }

        //update ip on no-ip
        NoIP n = new NoIP("spada.elfica@gmail.com","nzor4csv4");
        n.submitHostname("ciaoasdfghjkl.ddns.net");

        //export arff files
        bi = new BuildingsInformations();
        BuildArff ba = new BuildArff(dh, baseName, bi, basePath);


        /*if(!ba.deleteArffFiles()) {
            System.out.println("Impossible to delete some arff file");
            return;
        }
        ba.exportArffFiles();
        */
        //build classifier
        createClassifier(basePath, baseName, ba);

        //file server
        System.out.println("Start file server");
        try {
            DbServerThread test = new DbServerThread(8000, basePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("File server - Impossible to start file server");
        }

        //contribution server
        System.out.println("Start contribution server");
        ServerHelper socket = new ServerHelper(8080);
        System.out.println("Contribution Server is listening");
        String[] results = new String[NUM_SAMPLES];
        while(socket.acceptNewClient()){
            System.out.println("Contribution Server - New client is arrived!");
            for(int i = 0; i < NUM_SAMPLES; i++) {
                LinkedHashMap<String, String> sample = socket.readClientRecord();
                //sample <bssid, rssi>
                System.out.println(sample.toString());


                results[i] = ClassificationProcess(sample);
                System.out.println(results[i]);
            }
            String result = findMajority(results);
            socket.writeSingleLine(result);
            System.out.println("Contribution Server - Server response: " + result);
            socket.closeClient();


            //if db files are more than one retrain
            if(dh.getDbList().length > 1) {
                /*if(!ba.deleteArffFiles()) {
                    System.out.println("Impossible to delete some arff file");
                    return;
                }
                ba.exportArffFiles();

                //blank previous structures
                clsList.clear();
                buildingIndexes.clear();*/

                //recreate classifier
                createClassifier(basePath, baseName, ba);
            }
        }

    }


}

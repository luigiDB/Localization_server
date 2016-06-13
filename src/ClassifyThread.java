import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Created by Giulio on 12/06/2016.
 */
public class ClassifyThread implements Runnable{
    //buildingIndexes is needed to know in which clsList index is stored each building classifier
    private LinkedHashMap<String, Integer> buildingIndexes;
    private ArrayList<ClassifierService> clsList;
    private BuildingsInformations bi;
    private int NUM_SAMPLES;
    private String basePath;
    private int port;
    private ServerHelper socket;
    private DatabaseHelper dh;

    public ClassifyThread(int NUM_SAMPLES, String basePath, ServerHelper socket) {
        this.NUM_SAMPLES = NUM_SAMPLES;
        this.basePath = basePath;
        this.socket = socket;
    }

    /**
     * Given a sample compute the most probable building and classify
     * @param sample LinkedHashMap<String, String> of the sampled data
     * @return classifier result aka. room
     */
    public String ClassificationProcess(LinkedHashMap<String, String> sample){

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


    @Override
    public void run() {

        //merge and generate arff files
        String baseName = "base";
        dh = new DatabaseHelper(basePath);
        //socket = new ServerHelper(port);

        //export arff files
        bi = new BuildingsInformations();
        BuildArff ba = new BuildArff(dh, baseName, bi, basePath);
        ba.exportArffFiles();

        //list method
        clsList = new ArrayList<>();
        buildingIndexes = new LinkedHashMap<>();
        ClassifierService temp;
        int counter = 0;
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

        System.out.println("Server is listening");
        String[] results = new String[NUM_SAMPLES];
        while (socket.acceptNewClient()) {
            System.out.println("New client is arrived!");
            for (int i = 0; i < NUM_SAMPLES; i++) {
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
        socket.closeAll();
    }
}

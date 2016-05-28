import java.util.ArrayList;
import java.util.LinkedHashMap;
/**
 * Created by luigi on 12/04/2016.
 */
public class SqliteTest {

    private static LinkedHashMap<String, Integer> buildingIndexes;
    private static ArrayList<ClassifierService> clsList;
    private static BuildingsInformations bi;
    private static int NUM_SAMPLES = 5;

    public static String ClassificationProcess(LinkedHashMap<String, String> sample){
        //find the building by searching for the first bssid in sample
        String building = bi.getBuilding(sample.keySet().iterator().next());
        System.out.println("Selected building: " + building);
        ArrayList<String> bssid = bi.getBssidList(building);
        String[] trainArray = BuildArff.computeMeasurementArray(sample, bssid, null);

        System.out.println("Visual check of created sample:");
        assert trainArray != null;
        for(String str : trainArray)
            System.out.print(str + " ");


        //This need to the checked
        int index = buildingIndexes.get(building);
        String result = clsList.get(index).classify(trainArray);
        return result;
    }

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

    public static void main( String args[] )
    {
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

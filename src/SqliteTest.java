import java.util.ArrayList;
import java.util.LinkedHashMap;
/**
 * Created by luigi on 12/04/2016.
 */
public class SqliteTest {
    public static void main( String args[] )
    {
        /*------------------------------TEST CLASSE DatabaseHelper and BuildingsInfo.*/
        //merge and generate arff files
        String basePath = "C:\\resources\\";
        String baseName = "baruffa";

        DatabaseHelper dh = new DatabaseHelper(basePath);

        //export arff files
        BuildingsInformations bi = new BuildingsInformations();
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
        ClassifierService cls = new ClassifierService();
        cls.buildClassifier(basePath + "baruffa_casa-silvia.arff");

        //list method
        ArrayList<ClassifierService> clsList = new ArrayList<>();
        LinkedHashMap<String, Integer> buildingIndexes = new LinkedHashMap<>();
        ClassifierService temp;
        int counter = 0;
        for(String building: bi.getBuildingList()) {
            //create classifier
            temp = new ClassifierService();
            temp.buildClassifier(basePath + baseName + "_" + building + ".arff");

            //add classifier
            clsList.add(temp);

            //add building index
            buildingIndexes.put(building, counter);
            counter += 1;
        }

        ServerHelper socket = new ServerHelper(8888);
        System.out.println("Server is listening");
        while(socket.acceptNewClient()){
            System.out.println("New client is arrived!");
            LinkedHashMap<String, String> sample = socket.readClientRecord();
            System.out.println(sample.toString());


            //find the building by searching for the first bssid in sample
            String building = bi.getBuilding(sample.keySet().iterator().next());
            ArrayList<String> bssid = bi.getBssidList(building);
            String[] trainArray = BuildArff.computeMeasurementArray(sample, bssid, null);

            System.out.println("Visual check of created sample:");
            assert trainArray != null;
            for(String str : trainArray)
                System.out.print(str + " ");


            //This need to the checked
            int index = buildingIndexes.get(building);
            String result = clsList.get(index).classify(trainArray);

            //original version
            //String result = cls.classify(trainArray);

            socket.writeSingleLine(result);
            System.out.println("Server response: " + result);
            socket.closeClient();
        }

    }
}

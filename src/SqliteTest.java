import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by luigi on 12/04/2016.
 */
public class SqliteTest {
    public static void main( String args[] )
    {

        String basePath = "C:\\resources\\";
        /*
        DatabaseHelper dh = new DatabaseHelper(basePath);

        BuildArff ba = new BuildArff(dh, "baruffa", basePath);
        ba.exportArffFiles();
        */
        //TEST CLASSE ClassifierService
        ClassifierService cls = new ClassifierService();
        cls.buildClassifier(basePath + "baruffa_polo_c.arff");
        String test = "0,0,-80,0,0,-92,-60,0,0,0,-79,0,0,0,0,0,0,-87,0,-81,0,0,0,0,0,0,0,0,-84,-69,0,0,0,-87,0,-84,-84,-84,0,0,-84,-78,0,-78,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
        test += ",0";     //aggiungo artificialmente uno zero dove dovrebbe esserci invece la classe "polo_c_2_2", ma che non c'è perchè è da classificare
        String[] parsed = test.split(",");
        System.out.println(cls.classify(parsed));
    }
}

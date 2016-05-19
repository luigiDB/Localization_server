import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.*;

import java.util.Random;

/**
 * Created by Giulio on 17/04/2016.
 */
public class ClassifierService {
    Classifier cls;
    String[] classes;
    Instances data;
    public ClassifierService() {
        cls = null;
        classes = null;
        data = null;
    }

    public boolean buildClassifier(String filePath){
        if(filePath == null || filePath.isEmpty())
            return false;
        try {
            //Read the arff file
            DataSource source = new DataSource(filePath);
            data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
            //Build the classifier
            //cls = new IBk();
            cls = new RandomForest();
            cls.buildClassifier(data);
            //Save class values into a string array
            classes = new String[data.classAttribute().numValues()];
            for(int i = 0; i < data.classAttribute().numValues(); i++){
                String classValue = data.classAttribute().value(i);
                classes[i] = classValue;
            }

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String classify(String[] sample){
        if(sample == null || sample.length != data.numAttributes())
            return null;
        try {
            double[] parsedSample = new double[sample.length];
            for(int i = 0; i < sample.length; i++)
                parsedSample[i] = Double.parseDouble(sample[i]);
            Instance toClassify =  data.firstInstance().copy(parsedSample);

            int classIndex = (int)cls.classifyInstance(toClassify);
            return classes[classIndex];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}



/**
 * Created by luigi on 12/04/2016.
 */
public class SqliteTest {
    public static void main( String args[] )
    {
        String basePath = "C:\\resources\\";

        DatabaseHelper dh = new DatabaseHelper(basePath);

        BuildArff ba = new BuildArff(dh, "baruffa", basePath);
        ba.exportArffFiles();
    }
}

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Created by luigi on 12/04/2016.
 */
public class SqliteTest {
    private static final int NUM_SAMPLES = 3;
    private static String ip;
    private static String basePath = "C:\\resources\\";
    private static int portContribution = 8000;
    private static int portClassifiction = 8080;


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
        //Start thread to collect contributions from users
        try {
            DbServerThread test = new DbServerThread(portContribution, basePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //update ip on no-ip
        NoIP n = new NoIP("spada.elfica@gmail.com","nzor4csv4");
        n.submitHostname("ciaoasdfghjkl.ddns.net");

        while(true) {
            ServerHelper server = new ServerHelper(portClassifiction);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future f = executor.submit(new ClassifyThread(NUM_SAMPLES, basePath, server));

            try {
                sleep(1000 * 20);


                server.closeAll();
                f.cancel(true);
                sleep(1000 * 10);
                /*
                if (!BuildArff.deleteArffFiles(basePath)) {
                    System.out.println("Error in deleting the arff files");
                    break;
                }
                */
            }catch (InterruptedException e) {
                    e.printStackTrace();
            }
        }
    }

}

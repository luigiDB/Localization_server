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



    public static void main( String args[] )
    {
        //Start thread to collect contributions from users
        try {
            DbServerThread test = new DbServerThread(portContribution, basePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Create the class (and the thread) to upload periodically the public IP address
        NoIP dnsService = new NoIP("spada.elfica@gmail.com","nzor4csv4");
        //The public IP address will be bound to a given URL address
        dnsService.submitHostname("ciaoasdfghjkl.ddns.net");

        while(true) {
            //Create a server socket for the classification thread
            ServerHelper server = new ServerHelper(portClassifiction);
            //Use an ExecutorService with one single thread to run the Classification Thread
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future f = executor.submit(new ClassifyThread(NUM_SAMPLES, basePath, server));

            try {
                //After 10 minutes it closes the server socket. This is the signal to stop for the ClassifyThread
                sleep(1000 * 60 * 5);
                //Once the server has been closed, the thread will come out from the infinite loop
                server.closeAll();
                //Be sure that the thread will be terminated
                f.cancel(false);
                //It waits 1 minute before re-starting the ClassifyThread
                sleep(1000 * 60);
            }catch (InterruptedException e) {
                    e.printStackTrace();
            }
        }
    }

}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;

/**
 * Created by Giulio on 17/04/2016.
 */
public class ServerHelper {
    private int portNumber;
    private ServerSocket server;
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    public ServerHelper(int portNumber){
        if(portNumber <= 0)
            return;
        this.portNumber = portNumber;
        client = null;
        try {
            server = new ServerSocket(portNumber);
            server.setReuseAddress(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean acceptNewClient(){
        try {
            client = server.accept();
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if(client != null && out != null && in != null)
            return true;
        return false;
    }
    public String readSingleLine(){
        String ret;
        try {
            ret = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }
    public boolean writeSingleLine(String line){
        if(line == null)
            return false;
        try {
            out.println(line);
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //Assuming that the data format sent by the android application is like "Mac1=pow1,Mac2=pow2,Mac3=pow3"
    public LinkedHashMap<String,String> readClientRecord(){
        LinkedHashMap<String,String> ret;
        String record;
        String[] firstStage;
        String[] secondStage;

        record =  readSingleLine();
        if(record == null || record.isEmpty())
            return null;
        firstStage = record.split(",");
        ret = new LinkedHashMap<>();
        for(String str : firstStage){
            secondStage = str.split("=");
            ret.put(secondStage[0],secondStage[1]);
        }
        return ret;
    }

    public void closeClient(){
        try {
            in.close();
            out.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

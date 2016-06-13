import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;

/**
 * Created by Giulio on 17/04/2016.
 */
public class ServerHelper {
    private ServerSocket server;
    private Socket client;
    private BufferedReader inLine;
    private PrintWriter out;

    public ServerHelper(int portNumber){
        if(portNumber <= 0)
            return;
        this.client = null;
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
            inLine = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }
        if(client != null && out != null && inLine != null)
            return true;
        return false;
    }
    public String readSingleLine(){
        if(inLine == null)
            return null;
        String ret;
        try {
            ret = inLine.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }


    public boolean writeSingleLine(String line){
        if(line == null || out == null)
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
            if(inLine != null) {
                inLine.close();
                inLine = null;
            }
            if(out != null) {
                out.flush();
                out.close();
                out = null;
            }
            if(client != null) {
                client.close();
                client = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeAll(){
        this.closeClient();
        try {
            if(server != null) {
                server.close();
                server = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

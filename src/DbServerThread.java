import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Giulio on 19/05/2016.
 */
public class DbServerThread extends Thread{
    private int Port;
    private String basePath;
    private ServerSocket socket;

    public DbServerThread(int port, String base) throws IOException {
        if(port <= 0 || base == null)
            return;
        this.Port = port;
        this.basePath = base;
        //Create the server socket object
        this.socket = new ServerSocket(Port);
        socket.setReuseAddress(true);
        start();
    }


    public void run(){
        System.out.println("Server is listening");
        try {
            while(true) {

                Socket actual = socket.accept();
                System.out.println("New contribution is arrived!");


                String strFilePath = basePath + "file.db";
                try {
                    FileOutputStream fos = new FileOutputStream(strFilePath);
                    //String strContent = "Write File using Java ";


                    DataInputStream dIn = new DataInputStream(actual.getInputStream());

                    int length = dIn.readInt();                    // read length of incoming message
                    if(length>0) {
                        byte[] message = new byte[length];
                        dIn.readFully(message, 0, message.length); // read the message
                        fos.write(message);
                        fos.close();
                    }


                }
                catch(FileNotFoundException ex)   {
                    System.out.println("FileNotFoundException : " + ex);
                }
                catch(IOException ioe)  {
                    System.out.println("IOException : " + ioe);
                }

                actual.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

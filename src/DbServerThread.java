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
    private FileHelper fh;
    private final int DIM_BUF = 1024;

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

    /**
     * This is a basic thread that receives contribution databases from clients
     * and saves them inside a given folder
     */
    public void run(){
        Socket actual;
        byte[] message = new byte[DIM_BUF];
        int length = 0;
        int received = 0;
        System.out.println("File server - Server is listening");
        try {
            while(true) {
                actual = socket.accept();
                System.out.println("File server - New contribution is arrived!");

                fh = new FileHelper("New_Contribution.db", basePath);
                DataInputStream dIn = new DataInputStream(actual.getInputStream());
                //Read the file size
                length = dIn.readInt();
                System.out.println(length);
                while(length > 0) {
                    received = dIn.read(message);
                    fh.saveFile(message, received);
                    length -= received;
                }

                actual.close();
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                fh.closeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

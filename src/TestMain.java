import java.io.*;
import java.net.Socket;

/**
 * Created by Giulio on 23/05/2016.
 */
public class TestMain {

    public final static int SOCKET_PORT = 13267;  // you may change this
    public final static String FILE_TO_SEND = "C:\\Users\\Giulio\\Desktop\\BackupDB_Giulio_casa.db";  // you may change this

    public static void main(String args[]) throws IOException {

        DbServerThread test = new DbServerThread(8000, "C:\\resources\\");

/*
        Socket sock = null;
        try {
            sock = new Socket("127.0.0.1", 8000);
            try {
                System.out.println("Accepted connection : " + sock);


                File file = new File(FILE_TO_SEND);

                byte[] b = new byte[(int) file.length()];
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    fileInputStream.read(b);

                    DataOutputStream dOut = new DataOutputStream(sock.getOutputStream());

                    dOut.writeInt(b.length); // write length of the message
                    dOut.write(b);           // write the message

                    dOut.close();


                } catch (FileNotFoundException e) {
                    System.out.println("File Not Found.");
                    e.printStackTrace();
                } catch (IOException e1) {
                    System.out.println("Error Reading The File.");
                    e1.printStackTrace();
                }
            } finally {

            }
        } finally {
            if (sock != null) sock.close();
        }
*/
    }
}


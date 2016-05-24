import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Giulio on 23/05/2016.
 */
public class TestMain {

    public final static int SOCKET_PORT = 13267;  // you may change this
    public final static String FILE_TO_SEND = "C:\\Users\\Giulio\\Desktop\\BackupDB_Giulio_casa.db";  // you may change this

    public static void main(String args[]) throws IOException {

        DbServerThread test = new DbServerThread(8000, "C:\\resources\\");

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        Socket sock = null;
        try {
            sock = new Socket("127.0.0.1", 8000);
            try {
                System.out.println("Accepted connection : " + sock);
                // send file
                File myFile = new File (FILE_TO_SEND);
                byte [] mybytearray  = new byte [(int)myFile.length()];
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(mybytearray,0,mybytearray.length);
                os = sock.getOutputStream();
                System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
                os.write(new String(myFile.getName() + "\n").getBytes());
                os.write(new String(Integer.toString(mybytearray.length) + "\n").getBytes());
                for(byte b : mybytearray)
                    System.out.print((char)b);
                System.out.println("");
                os.write(mybytearray,0,mybytearray.length);
                os.flush();
                System.out.println("Done.");
            }
            finally {
                if (bis != null) bis.close();
                if (os != null) os.close();
                if (sock!=null) sock.close();
            }


        }
        finally {
            if (sock != null) sock.close();
        }
    }
}


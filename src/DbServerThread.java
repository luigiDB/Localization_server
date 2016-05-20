import java.io.File;

/**
 * Created by Giulio on 19/05/2016.
 */
public class DbServerThread extends Thread{
    private int Port;
    private String basePath;
    private ServerHelper socket;

    public DbServerThread(int port, String base){
        if(port <= 0 || base == null)
            return;
        this.Port = port;
        this.basePath = base;
        //Create the server socket object
        this.socket = new ServerHelper(Port);
        start();
    }

    public void run(){
        System.out.println("Server is listening");
        while(socket.acceptNewClient()) {
            System.out.println("New contribution is arrived!");
            //retrieve the name
            String fileName = socket.readSingleLine();
            //create the file handler object
            FileWriter fileHandler = new FileWriter(fileName, basePath);
            //now read the file dimension
            int dimFile = Integer.parseInt(socket.readSingleLine());
            char[] temp;
            //now read the file from the socket and save in the file
            while(dimFile > 0){
                temp = socket.readSingleLine().toCharArray();
                fileHandler.saveFile(temp);
                dimFile -= temp.length;
            }
            fileHandler.closeFile();
            socket.closeClient();
        }
    }
}

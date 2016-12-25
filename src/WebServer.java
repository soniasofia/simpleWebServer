import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by codecadet on 09/11/16.
 */
public class WebServer {

    private int portNumber = 2000;
    private ServerSocket serverSocket;

    /**
     * Wait for a connection and processes one Request at a time.
     */
    public void start () {

        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Waiting for connection...");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server start error");
        }

        ExecutorService pool = Executors.newFixedThreadPool(4);

        while (true) {

            Socket clientSocket = null;

            try {
                clientSocket = serverSocket.accept();
                System.out.println("Client accepted.");
                pool.submit(new Processor(clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


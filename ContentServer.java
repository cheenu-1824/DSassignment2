import java.net.*;
import java.io.*;

public class ContentServer {
    public static void main(String[] params) {
        int port = 9000;
        
        if (params.length == 1) {
            port = Integer.parseInt(params[0]);
        } 

        System.out.println("Starting content server on port: " + port);

        try (ServerSocket contentServerSocket = new ServerSocket(port)) {
            
            while (true) {

                try {

                    Socket aggregationClientSocket = contentServerSocket.accept();

                } catch (IOException e) {
                    System.out.println("Content server socket was closed...");
                }
            }

        } catch (IOException e) {
            System.out.println("Content server could not listen on port: " + port);
            System.exit(-1);
        }
    }
}

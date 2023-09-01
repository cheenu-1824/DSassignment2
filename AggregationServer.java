import java.net.*;
import java.io.*;

public class AggregationServer {
    public static void main(String[] params) {
        int port = 9999;

        System.out.println("Starting aggregation server on port: " + port);

        try (ServerSocket aggregationServerSocket = new ServerSocket(9999)) {
            
            while (true) {

                try {

                    Socket clientSocket = aggregationServerSocket.accept();

                } catch (IOException e) {
                    System.out.println("Aggregation server socket was closed...");
                }
            }

        } catch (IOException e) {
            System.out.println("Aggregation server could not listen on port: " + port);
            System.exit(-1);
        }
    }
}
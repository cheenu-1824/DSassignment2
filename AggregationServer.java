import java.net.*;
import java.io.*;

public class AggregationServer {

    public static void handlePutReq(BufferedWriter bufferedWriter, String msg) {

        System.out.println("PUT: " + msg);
        
        try {
    
            bufferedWriter.write("PUT request received!");
            bufferedWriter.newLine();
            bufferedWriter.flush();
    
        } catch (IOException e) {
            System.out.println("Error: Failed to send PUT response...");
        }
    
    }


    public static void main(String[] args) {

        Socket socket = null;
        ServerSocket serverSocket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        String serverAddress = "localhost";
        int port = 4567;

        System.out.println("Starting aggregation server on port: " + port);

        try {
            
            serverSocket = new ServerSocket(port);

            while (true) {

                try {

                    socket = serverSocket.accept();

                    inputStreamReader = new InputStreamReader(socket.getInputStream());
                    outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                    bufferedReader = new BufferedReader(inputStreamReader);
                    bufferedWriter = new BufferedWriter(outputStreamWriter);

                    while (true) {

                        String msg = bufferedReader.readLine();

                        // Make this into a function which uses switch statement
                        if (msg.contains("PUT")){
                            handlePutReq(bufferedWriter, msg);
                        } else {
                            System.out.println("Client: " + msg);

                            bufferedWriter.write("MSG received!");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();

                        }






                        if (msg.equalsIgnoreCase("BYE")){
                            break;
                        }
                    }

                    socket.close();
                    inputStreamReader.close();
                    outputStreamWriter.close();
                    bufferedReader.close();
                    bufferedWriter.close();

                } catch (IOException e) {
                    System.out.println("Aggregation server socket was closed...");
                }
            }

        } catch (IOException e) {
            System.out.println("Aggregation server could not listen on port: " + port);
        }
    }
}
import java.net.*;
import java.io.*;

public class AggregationServer {

    public static String buildMsg(BufferedReader bufferedReader) {
        StringBuilder content = new StringBuilder();
        try {
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.out.println("Error: Failed to build request into a string...");
        }

        return content.toString();
    }

    public static void handleReq(BufferedReader bufferedReader, BufferedWriter bufferedWriter, String msg) {
        
        
        try {
            switch (msg.substring(0, 3)) {
                case "PUT":
                    msg = buildMsg(bufferedReader);
                    handlePutReq(bufferedWriter, msg);
                    break;
                case "GET":
                    break;
                default:
                    System.out.println("Client: " + msg);
    
                    bufferedWriter.write("MSG received!");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    break;
            }
        } catch (IOException e) {
            System.out.println("Error: Failed to handle req properly...");
        }

    }

    public static void handlePutReq(BufferedWriter bufferedWriter, String msg) {

        System.out.println("PUT request:\n" + msg);
        
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

                        // Handle requests "GET", "PUT"
                        handleReq(bufferedReader, bufferedWriter, msg);


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
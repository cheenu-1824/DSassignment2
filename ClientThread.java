import java.net.*;
import java.io.*;

public class ClientThread implements Runnable{

    private Socket socket;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {

        int maxClients = 5;

        try {

            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            while (true) {

                String msg = bufferedReader.readLine();

                if (msg.equalsIgnoreCase("BYE")){
                    bufferedWriter.write("BYE!");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    break;
                }

                // Handle requests "GET", "PUT"
                AggregationServer.handleReq(bufferedReader, bufferedWriter, msg);

            }

            for (int i = 0; i < maxClients; i++) {
                if (AggregationServer.threads[i] == this) {
                    AggregationServer.threads[i] = null;
                    break;
                }
            }

            socket.close();
            inputStreamReader.close();
            outputStreamWriter.close();
            bufferedReader.close();
            bufferedWriter.close();

        } catch (IOException e) {
            System.out.println("Error: Failed to start threaded client socket...");
        }
    }

}
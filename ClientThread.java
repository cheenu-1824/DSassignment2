import java.net.*;
import java.io.*;

import lib.*;

/**
 * The ClientThread class represents a thread that handles a request from a single client.
 * It listens for messages from the client, handles them, and responds.
 */
public class ClientThread implements Runnable{

    private Socket socket = null;

    /**
     * Constructs a ClientThread instance.
     *
     * @param socket The client socket associated with the thread instance.
     */
    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    /**
     * Runs the thread's handling process for handling client communication.
     * It listens for incoming messages, processes them, and responds.
     */
    public void run() {

        int maxClients = 15;

        try {

            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            while (true) {

                String msg = bufferedReader.readLine();

                if (msg.equalsIgnoreCase("BYE")) {
                    Http.write(bufferedWriter, "BYE!\r\n");
                    break;
                }

                if (true) { 
                    AggregationServer.handleReq(bufferedReader, bufferedWriter, msg);
                }

            }

            synchronized (AggregationServer.threads) {
                for (int i = 0; i < maxClients; i++) {
                    if (AggregationServer.threads[i] == this) {
                        AggregationServer.threads[i] = null;
                        break;
                    }
                }
            }
            
            Tool.closeSocket(socket);
            Tool.closeInputStreamReader(inputStreamReader);
            Tool.closeOutputStreamWriter(outputStreamWriter);
            Tool.closeBufferedReader(bufferedReader);
            Tool.closeBufferedWriter(bufferedWriter);

        } catch (IOException e) {
            System.out.println("Error: Failed to start threaded client socket...");
        } 
    }
}
import java.net.*;
import java.io.*;

import lib.*;

public class ClientThread implements Runnable{

    private Socket socket = null;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {

        int maxClients = 1;

        try {

            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            while (true) {

                String msg = bufferedReader.readLine();
                System.out.println("MSG:  " + msg);

                if (msg.equalsIgnoreCase("BYE")) {
                    Http.write(bufferedWriter, "BYE!\r\n");
                    break;
                }

                if (true) { // for some reason its not receive by msg
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
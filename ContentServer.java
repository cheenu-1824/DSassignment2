import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ContentServer {

    public static String[] parseURL(String url) {
        String[] splitURL = url.split(":");
    
        if (splitURL.length == 2) {
            return splitURL;
        } else {
            throw new IllegalArgumentException("Invalid URL format: " + url + ". Correct usage: <domain>:<port>");
        }
    }

    public static String readFile(String filename) {

        StringBuilder stringBuilder = new StringBuilder();

        try {

            String line;
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

        } catch (IOException e) {

            System.out.println("Error: " + filename + " could not be found in the file system");
            System.exit(1);

        }
        return stringBuilder.toString();
    }


    public static void main(String[] args) {
        
        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        Scanner scanner = null;
        String serverAddress = "localhost";
        int port = 9999;
        String filename = "content/test.txt";

        if (args.length != 2){
            System.out.println("Incorrect parameters, input should be as follows: make contentServer <domain:port> <filename>");
            System.exit(1);
        }

        String[] splitURL = parseURL(args[0]);
        serverAddress = splitURL[0];
        port = Integer.parseInt(splitURL[1]);
        filename = args[1];


        System.out.println(serverAddress);
        System.out.println(port);
        System.out.println(filename);


        try {

            // Read file from file system
            String content = readFile(filename);
            System.out.println(content);

            // Establish connection to aggregation server
            socket = new Socket(serverAddress, port);


            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            scanner = new Scanner(System.in);

            while (true) {
                
                String msg = scanner.nextLine();
                bufferedWriter.write(msg);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                System.out.println("Server: " + bufferedReader.readLine());

                if (msg.equalsIgnoreCase("BYE")){
                    break;
                }
            }

        } catch (IOException e){
            System.out.println("Connection to aggregation server was lost...");
            e.printStackTrace();

        } finally {
            
            try {

                if (socket != null) {
                    socket.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }

            } catch (IOException e){
                System.out.println("Error occured when closing objects...");
                e.printStackTrace();

            }

        }
    }
}

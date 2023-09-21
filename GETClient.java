import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;

public class GETClient {

    public static String[] parseURL(String url) {
        String[] splitURL = url.split(":");
    
        if (splitURL.length == 2) {
            return splitURL;
        } else {
            throw new IllegalArgumentException("Invalid URL format: " + url + ". Correct usage: <domain>:<port>");
        }
    }

    public static void getReq(BufferedWriter bufferedWriter) {

        String getMessage = "GET /api/resource HTTP/1.1\r\n"
                    + "Host: " + "host" + "\r\n\r\n";

        System.out.println(getMessage);

        try {

            bufferedWriter.write(getMessage);
            //bufferedWriter.newLine();
            bufferedWriter.flush();

        } catch (IOException e ){
            System.out.println("Error: Failed to send GET request the the server...");
        } 

    }

    public static String buildMsg(BufferedReader bufferedReader) {
        StringBuilder content = new StringBuilder();
        try {
            boolean isContent = false;
            while (true) {
                String line = bufferedReader.readLine();
                if (line.isEmpty()) {
                    if (isContent == true){
                        break;
                    } else {
                        isContent = true;
                    }
                }
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.out.println("Error: Failed to build request into a string...");
        }
        return content.toString().trim();
    }

    public static String getBody(String msg) {
        int contentIndex = msg.indexOf("{");
        if (contentIndex != -1) {
            String requestBody = msg.substring(contentIndex);
            return requestBody;
        } else {
            System.out.println("Error: No content found in the body of the request...");
            return null;
        }
    }

    public static List<String> splitJson(String msg) {
        String[] weatherData = msg.split("\\n");
        List<String> json = new ArrayList<>();
    
        for (String weather : weatherData) {
            weather = weather.trim();
            if (!weather.isEmpty()) {
                json.add(weather);
            }
        }
    
        return json;
    } 

    public static void handleReq(BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        
       // try {
            String msg = buildMsg(bufferedReader);
            System.out.println(msg);

            String body = getBody(msg);

            Gson gson = new Gson();

            List<String> json = splitJson(getBody(msg));
            List<WeatherObject> weatherData = new ArrayList<>();
            
            for (String entry : json) {
                weatherData.add(gson.fromJson(entry, WeatherObject.class));
            }
       // } catch (IOException e) {
          //  System.out.println("Error: Failed to handle req properly...");
       // }

    } 

    public static void main(String[] args) {

        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        Scanner scanner = null;
        String serverAddress = "localhost";
        int port = 4567;

        if (args.length != 1){
            System.out.println("Incorrect parameters, input should be as follows: java GETClient <domain:port>");
            System.exit(1);
        }

        // Parse URL in server address and port
        String[] splitURL = parseURL(args[0]);
        serverAddress = splitURL[0];

        try {

            socket = new Socket(serverAddress, port);


            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            scanner = new Scanner(System.in);

            getReq(bufferedWriter);
            System.out.println("Server: " + bufferedReader.readLine());

            handleReq(bufferedReader, bufferedWriter);

            while (true) {
                
                String msg = "BYE";
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


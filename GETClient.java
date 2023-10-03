import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;

import lib.*;

public class GETClient {

    private static Socket socket = null;
    private static InputStreamReader inputStreamReader = null;
    private static OutputStreamWriter outputStreamWriter = null;
    private static BufferedReader bufferedReader = null;
    private static BufferedWriter bufferedWriter = null;

    public static void setReaderWriter(Socket socket) throws IOException {
        
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        bufferedWriter = new BufferedWriter(outputStreamWriter);

    }

    public static String[] parseURL(String url) {
        String[] splitURL = url.split(":");
    
        if (splitURL.length == 2) {
            return splitURL;
        } else {
            throw new IllegalArgumentException("Invalid URL format: " + url + ". Correct usage: <domain>:<port>");
        }
    }

    public static void getReq(BufferedWriter bufferedWriter) {

        String getMessage = "GET /filesystem/weather.json HTTP/1.1\r\n" //find correct dir
                    + "Host: " + "host" + "\r\n\r\n";
        try {

            Http.write(bufferedWriter, getMessage);

        } catch (IOException e ){
            System.out.println("Error: Failed to send GET request the the server...");
        } 

    }

    public static void postReq(BufferedWriter bufferedWriter) {

        String content = "Retrying connection\r\n";
        int contentLength = content.length();

        String postMessage = Http.HttpRequest("POST", null, false, "text/plain", contentLength, content);

        System.out.println(postMessage);

        try {

            Http.write(bufferedWriter, postMessage);

        } catch (IOException e ){
            System.out.println("Error: Failed to send POST request to the server...");
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
            System.out.println("Error: Failed to recieve request from aggregation server...");
        }
        return content.toString().trim();
    }

    public static String getBody(String msg) {

        if (msg.length() == 0) {
            System.out.println("Error: No content found in the request, please request again...");
            return null;
        } else if (msg.charAt(msg.length() - 1) == '0') {
            System.out.println("Error: No content found in the body of the request, please request again...");
            return null;
        }

        //int contentIndex = msg.indexOf("{");
        int contentIndex = msg.indexOf("\n\n");
        if (contentIndex != -1) {
            String requestBody = msg.substring(contentIndex);
            return requestBody;
        } else {
            System.out.println("Error: Content has been malformed...");
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

    public static List<WeatherObject> handleReq(BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        
        String msg = buildMsg(bufferedReader);
        String body = getBody(msg);

        System.out.println("Server: GET req received!\n" + msg + "\n");
        

        if (body == null) {
            return null;
        }

        Gson gson = new Gson();

        List<String> json = splitJson(getBody(msg));
        List<WeatherObject> weatherData = new ArrayList<>();
        
        for (String entry : json) {
            weatherData.add(gson.fromJson(entry, WeatherObject.class));
        }

       return weatherData;

    } 

    public static Socket retryConnection(String serverAddress, int port) {
        int tries = 0;
        String response = "";

        while (tries < 4) {
            tries += 1;
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                System.err.println("Error: Failed to retry connection to the server...");
            }

            try {

                Socket socket = new Socket(serverAddress, port);

                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
    
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                
                postReq(bufferedWriter);

                while ((response = bufferedReader.readLine()) != null) {
                    System.out.println("Server: " + response);
                    if (!response.equals("Server too busy. Please try again later...")) {
                        response = bufferedReader.readLine();
                        System.out.println("Server: " + response);
                        return socket;
                    }
                }
                System.err.println("Error: Server is still busy, retrying...");
                Tool.closeSocket(socket);
            } catch (IOException e) {
                System.err.println("Error: Failed to retry connection to the server...");
            }

        }
        return null;
    }

    public static void displayWeather(List<WeatherObject> weatherData) {

        System.out.println("<=====WEATHER FEED=====>\n");

        for (WeatherObject weather : weatherData) {
            System.out.println("ID: " + weather.getId());
            System.out.println("Name: " + weather.getName());
            System.out.println("State: " + weather.getState());
            System.out.println("Time Zone: " + weather.getTime_zone());
            System.out.println("Latitude: " + weather.getLat());
            System.out.println("Longitude: " + weather.getLon());
            System.out.println("Local Date Time: " + weather.getLocal_date_time());
            System.out.println("Local Date Time Full: " + weather.getLocal_date_time_full());
            System.out.println("Air Temperature: " + weather.getAir_temp());
            System.out.println("Apparent Temperature: " + weather.getApparent_t());
            System.out.println("Cloud: " + weather.getCloud());
            System.out.println("Dew Point: " + weather.getDewpt());
            System.out.println("Pressure: " + weather.getPress());
            System.out.println("Relative Humidity: " + weather.getRel_hum());
            System.out.println("Wind Direction: " + weather.getWind_dir());
            System.out.println("Wind Speed (km/h): " + weather.getWind_spd_kmh());
            System.out.println("Wind Speed (kt): " + weather.getWind_spd_kt());
            System.out.println();
            System.out.println("<======================>");
            System.out.println();
        }
    }
    public static void main(String[] args) {

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

            setReaderWriter(socket);

            getReq(bufferedWriter);
            boolean sentReq = true;

            String response = bufferedReader.readLine();
            System.out.println("Server: " + response);

            if (!response.equals("HTTP/1.1 200 OK")) {
                sentReq = false;

                Tool.closeSocket(socket);

                socket = retryConnection(serverAddress, port);
                if (socket != null) {
                    setReaderWriter(socket);
                } else {
                    System.err.println("Error: Failed to establish a connection to the server, please reconnect to restore weather data to the server");
                }
            }

            if (sentReq == false && socket != null) {
                getReq(bufferedWriter);

                response = bufferedReader.readLine();
                System.out.println("Server: " + response);
            }

            List<WeatherObject> weatherData = handleReq(bufferedReader, bufferedWriter);

            if (weatherData == null) {

                String msg = "BYE\r\n";
                Http.write(bufferedWriter, msg);
                bufferedWriter.write(msg);
                bufferedWriter.newLine();
                bufferedWriter.flush();
    
                Tool.closeSocket(socket);
                Tool.closeInputStreamReader(inputStreamReader);
                Tool.closeOutputStreamWriter(outputStreamWriter);
                Tool.closeBufferedReader(bufferedReader);
                Tool.closeBufferedWriter(bufferedWriter);

                System.exit(1);
            }

            displayWeather(weatherData);

            // Function for testing 
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Failed to wait");
            }

            String msg = "BYE";
            bufferedWriter.write(msg);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            System.out.println("Server: " + bufferedReader.readLine());

            if (msg.equalsIgnoreCase("BYE")){
                System.out.println("Request was handled sucessfully...");
            } else {
                System.out.println("Failed to handle request sucessfully...");
            }

        } catch (IOException e){
            System.out.println("Connection to aggregation server was lost...");
            e.printStackTrace();

        } finally {
            
            try {

                Tool.closeSocket(socket);
                Tool.closeInputStreamReader(inputStreamReader);
                Tool.closeOutputStreamWriter(outputStreamWriter);
                Tool.closeBufferedReader(bufferedReader);
                Tool.closeBufferedWriter(bufferedWriter);

            } catch (IOException e){
                System.out.println("Error occured when closing objects...");
                e.printStackTrace();
            }
        }
    }
}


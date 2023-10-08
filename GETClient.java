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

    public static void getReq(BufferedWriter bufferedWriter) {

        String getMessage = "GET /filesystem/weather.json HTTP/1.1\r\n" //find correct dir
                    + "Host: " + "localhost" + "\r\n\r\n";
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
        String body = Http.getBody(msg);

        System.out.println(msg);
        

        if (body == null) {
            return null;
        }

        Gson gson = new Gson();

        List<String> json = splitJson(Http.getBody(msg));
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
                    System.out.println(response);
                    if (!response.equals("Server too busy. Please try again later...")) {
                        response = bufferedReader.readLine();
                        System.out.println(response);
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

        System.out.println("\n\n<=====WEATHER FEED=====>\n");

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
        String[] splitURL = Tool.parseURL(args[0]);
        serverAddress = splitURL[0];

        try {

            socket = new Socket(serverAddress, port);

            setReaderWriter(socket);

            Http.getRequest(bufferedWriter);
            boolean sentReq = true;

            String response = bufferedReader.readLine();
            System.out.println(response);

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
                Http.getRequest(bufferedWriter);

                response = bufferedReader.readLine();
                System.out.println(response);
            }

            List<WeatherObject> weatherData = handleReq(bufferedReader, bufferedWriter);

            if (weatherData == null) {

                String msg = "BYE\r\n";

                Http.write(bufferedWriter, msg);

    
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

            String msg = "BYE\r\n";
            Http.write(bufferedWriter, msg);
            String finalResponse = bufferedReader.readLine();

            if (finalResponse.equals("BYE!")){
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


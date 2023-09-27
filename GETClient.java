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

        String getMessage = "GET /filesystem/weather.json HTTP/1.1\r\n" //find correct dir
                    + "Host: " + "host" + "\r\n\r\n";

        try {

            bufferedWriter.write(getMessage);
            bufferedWriter.flush();

        } catch (IOException e ){
            System.out.println("Error: Failed to send GET request the the server...");
        } 

    }

    public static void postReq(BufferedWriter bufferedWriter) {

        String content = "Retrying connection\r\n";
        int contentLength = content.length();
        String putMessage = "POST / HTTP/1.1\r\n"
                    + "User-Agent: ATOMClient/1/0\r\n"
                    + "Content-Type: text/plain\r\n" // I NEED TO WORK THIS OUT
                    + "Content-Length: " + contentLength + "\r\n\r\n"
                    + content;

        System.out.println(putMessage);

        try {

            bufferedWriter.write(putMessage);
            bufferedWriter.newLine();
            bufferedWriter.flush();

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

        System.out.println("HER: " + msg);

        if (msg.charAt(msg.length() - 1) == '0') {
            System.out.println("Error: No content found in the body of the request, please request again...");
            return null;
        }

        int contentIndex = msg.indexOf("{");
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
                
                getReq(bufferedWriter);

                while ((response = bufferedReader.readLine()) != null) {
                    System.out.println("Server: " + response);
                    if (!response.equals("Server too busy. Please try again later...")) {
                        return socket;
                    }
                }
                System.err.println("Error: Server is still busy, retrying...");
                socket.close();
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

        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
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

           //String response = bufferedReader.readLine();

            //Retry if server is busy
           // System.out.println(response);
           //if (response.equals("Server too busy. Please try again later...")) {
             //  socket = retryConnection(serverAddress, port);
           //}

            getReq(bufferedWriter);
            boolean sentReq = true;

            String response = bufferedReader.readLine();
            System.out.println("Server: " + response);

            if (!response.equals("HTTP/1.1 200 OK")) {
                sentReq = false;
                socket = retryConnection(serverAddress, port);
                System.out.println("Servdsdser: ");

                inputStreamReader = new InputStreamReader(socket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                bufferedWriter = new BufferedWriter(outputStreamWriter);
                System.out.println("Servdsdser: ");

                response = bufferedReader.readLine();
                System.out.println("Server: " + response);

            }

            List<WeatherObject> weatherData = handleReq(bufferedReader, bufferedWriter);

            if (weatherData == null) {

                String msg = "BYE";
                bufferedWriter.write(msg);
                bufferedWriter.newLine();
                bufferedWriter.flush();
    
                System.out.println("Server: " + bufferedReader.readLine());

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
                System.exit(1);
            }

            displayWeather(weatherData);
            try {
                Thread.sleep(50);
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


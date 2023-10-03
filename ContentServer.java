import java.net.*;
import java.security.cert.TrustAnchor;
import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import lib.*;


public class ContentServer {

    private static int stationId = -1;
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

    public static WeatherObject buildWeatherObject(String input) {

        String[] lines = input.split("\n");
        WeatherObject weather = new WeatherObject();

        for (String line : lines) {

            String[] objectParts = line.split(":");

            String parameter = objectParts[0].trim();
            String value = objectParts[1].trim();

            switch (parameter) {
                case "id":
                    weather.setId(value);
                    break;
                case "name":
                    weather.setName(value);
                    break;
                case "state":
                    weather.setState(value);
                    break;
                case "time_zone":
                    weather.setTime_zone(value);
                    break;
                case "lat":
                    weather.setLat(Float.parseFloat(value));
                    break;
                case "lon":
                    weather.setLon(Float.parseFloat(value));
                    break;
                case "local_date_time":
                    weather.setLocal_date_time(value);
                    break;
                case "local_date_time_full":
                    weather.setLocal_date_time_full(value);
                    break;
                case "air_temp":
                    weather.setAir_temp(Float.parseFloat(value));
                    break;
                case "apparent_t":
                    weather.setApparent_t(Float.parseFloat(value));
                    break;
                case "cloud":
                    weather.setCloud(value);
                    break;
                case "dewpt":
                    weather.setDewpt(Float.parseFloat(value));
                    break;
                case "press":
                    weather.setPress(Float.parseFloat(value));
                    break;
                case "rel_hum":
                    weather.setRel_hum(Integer.parseInt(value));
                    break;
                case "wind_dir":
                    weather.setWind_dir(value);
                    break;
                case "wind_spd_kmh":
                    weather.setWind_spd_kmh(Integer.parseInt(value));
                    break;
                case "wind_spd_kt":
                    weather.setWind_spd_kt(Integer.parseInt(value));
                    break;
                default:
                    break;
            }
        }

        weather.setStationId(ContentServer.stationId);

        return weather;
    }

    public static List<String> splitWeatherData(String input) {
        String[] weatherData = input.split("id:");
    
        List<String> formattedWeatherData = new ArrayList<>();
    
        for (int i = 1; i < weatherData.length; i++) {
            formattedWeatherData.add("id:" + weatherData[i].trim());
        }
    
        return formattedWeatherData;
    }

    public static List<String> removeInvalidWeather(List<String> weatherData) {
        Iterator<String> iterator = weatherData.iterator();
        while (iterator.hasNext()) {
            String entry = iterator.next();
            if (entry.contains("id:name")) { // THIS MAY NOT BE RIGHT, DEPENDS IF ERROR ID MEANS ID FIELD IS BLANK
                iterator.remove();
            }
        }
        return weatherData;
    }

    public static void putReq(BufferedWriter bufferedWriter, List<String> json) {

        // Get content length
        int contentLength = 0;
        for (String entry : json) {
            contentLength += entry.length();
        }
        
        String body = "";

        for (String entry : json) {
            body += entry + "\n"; 
        }

        if (!body.isEmpty()) {
            body = body.substring(0, body.length() - 1);
        }

        String putMessage = Http.HttpRequest("PUT", "/content/weather.json", true, "application/json", contentLength, body);

        System.out.println(putMessage);

        try {

            Http.write(bufferedWriter, putMessage);

        } catch (IOException e ){
            System.out.println("Error: Failed to send PUT request to the server...");
        } 

    }

    public static void postReq(BufferedWriter bufferedWriter, boolean retry) {
        // Get content length
        String content = "";
        int contentLength = 0;

        String postMessage = "";

        if (retry == true) {
            content = "Retrying connection\r\n";
            contentLength = content.length();

            postMessage = Http.HttpRequest("POST", null, false, "text/plain", contentLength, content);

        } else {
            content = "StationId: " + ContentServer.stationId + " is alive\r\n";
            contentLength = content.length();

            postMessage = Http.HttpRequest("POST", null, false, "text/plain", contentLength, content);

        }

        System.out.println("SENDING POST MESSAGE!\r\n");
        System.out.println(postMessage);

        try {

            Http.write(bufferedWriter, postMessage);

        } catch (IOException e ){
            System.out.println("Error: Failed to send POST request to the server...");
        } 

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
                
                postReq(bufferedWriter, true);

                while ((response = bufferedReader.readLine()) != null && !response.isEmpty()) {
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

    public static void sendHeartbeat(String serverAddress, int port) {

        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {

            socket = new Socket(serverAddress, port);

            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            setReaderWriter(socket);

            postReq(bufferedWriter, false);
            boolean sentReq = true;

            String response = "";

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Failed to wait");
            }

                response = bufferedReader.readLine();
                System.out.println(response);

                // Retry if server is busy
                if (response.equals("Server too busy. Please try again later...")) {
                    sentReq = false;

                    Tool.closeSocket(socket);

                    socket = retryConnection(serverAddress, port);
                    if (socket != null) {

                        setReaderWriter(socket);

                        if (sentReq == false) {
                            postReq(bufferedWriter, false);
                        }

                    } else {
                        System.err.println("Error: Failed to establish a connection to the server, please reconnect to restore weather data to the server");
                    }
                }
            
            System.out.println("YEA"+ response); // issue is agg server receives random blank line
            while (true) {

                response = bufferedReader.readLine();
                System.out.println(response);
        
                if (response != null && response.isBlank()) {
                    Http.write(bufferedWriter, "BYE\r\n");
                    System.out.println(bufferedReader.readLine());
                    break;
                }
            }
            
        } catch (IOException e) {
            System.out.println("Error: Failed to send heartbeat...");
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

    private static Runnable sendHeartbeatPeriodically(final String serverAddress, final int port) {
        return new Runnable() {
            public void run() {
                sendHeartbeat(serverAddress, port);
            }
        };
    }

    public static void main(String[] args) {
        
        Scanner scanner = null;
        String serverAddress = "localhost";
        int port = 4567;
        String filename = "content/test.txt";

        Random random = new Random();
        int randomNumber = random.nextInt(1000000);
        ContentServer.stationId = randomNumber;

        if (args.length != 2){
            System.out.println("Incorrect parameters, input should be as follows: java ContentServer <domain:port> <file location>");
            System.exit(1);
        }

        // Parse URL in server address and port
        String[] splitURL = parseURL(args[0]);
        serverAddress = splitURL[0];
        port = Integer.parseInt(splitURL[1]);
        filename = args[1];

        // Read file from file system
        String content = readFile(filename);

        // Split file into seperate entries
        List<String> weatherData = splitWeatherData(content);

        // Remove invalid entries
        weatherData = removeInvalidWeather(weatherData);

        // Build objects for each entry in content
        List<WeatherObject> weathers = new ArrayList<>();
        for (String weather : weatherData) {
            weathers.add(buildWeatherObject(weather));
        }

        // Serializes object to JSON string
        Gson gson = new Gson();
        List<String> json = new ArrayList<>();
        for (WeatherObject weather : weathers) {
            json.add(gson.toJson(weather));
        }

        try {

            socket = new Socket(serverAddress, port);

            setReaderWriter(socket);

            putReq(bufferedWriter, json);
            boolean sentReq = true;
            String response = "";

            response = bufferedReader.readLine();
            System.out.println(response);

            if (response.equals("Server too busy. Please try again later...")) {
                sentReq = false;
                socket = retryConnection(serverAddress, port);
                if (socket != null) {

                    setReaderWriter(socket);

                    if (sentReq == false) {
                        putReq(bufferedWriter, json);
                    }
                    response = bufferedReader.readLine();
                    System.out.println("Servers: " + response);
                } else {
                    System.err.println("Error: Failed to establish a connection to the server, please check host location or try again later...");
                    System.exit(1);
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Failed to wait");
            }

            while (true) {

                response = bufferedReader.readLine();
                System.out.println(response);
    
                if (response != null && response.isBlank()) {
                    Http.write(bufferedWriter, "BYE\r\n");
                    System.out.println(bufferedReader.readLine());
                    break;
                }
            }
        } catch (IOException e){
            System.out.println("Connection to aggregation server was not established or lost...");
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
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleWithFixedDelay(sendHeartbeatPeriodically(serverAddress, port), 2, 15, TimeUnit.SECONDS);
        }
    }
}

import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import lib.*;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;


public class AggregationServer {

    private static Map<String, List<WeatherObject>> weatherDataMap = new HashMap<>();
    private static Map<Integer, Boolean> stationHeartbeat = new HashMap<>();
    public static ClientThread[] threads = new ClientThread[5];
    private static final Logger logger = Logger.getLogger(AggregationServer.class.getName());

    private static Socket socket = null;
    private static ServerSocket serverSocket = null;
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

    public static List<WeatherObject> getFeed() {
        List<WeatherObject> feed = new ArrayList<>();

        for (List<WeatherObject> weatherList : AggregationServer.weatherDataMap.values()) {
            if (!weatherList.isEmpty()) {

                WeatherObject recentWeather = weatherList.get(weatherList.size() - 1);
                feed.add(recentWeather);
            } else {
                logger.log(Level.SEVERE, "Failed to send weather feed (empty feed)...\n");
            }
        }
        return feed;
    }

    public synchronized static void updateStationHeartbeat(WeatherObject weather) {
        int stationId = weather.getStationId();
        stationHeartbeat.put(stationId, true);
    }

    public synchronized static void addWeatherData(WeatherObject weather) {

        updateStationHeartbeat(weather);

        String weatherId = weather.getId();

        if (AggregationServer.weatherDataMap.containsKey(weatherId)){
            AggregationServer.weatherDataMap.get(weatherId).add(weather);
        } else {
            List<WeatherObject> newWeatherId = new ArrayList<>();
            newWeatherId.add(weather);
            AggregationServer.weatherDataMap.put(weatherId, newWeatherId);
        }
    }

    //TESTING FUNC!!!!!!!
    public static void printWeatherMap() {

        System.out.println("<=====Weather Entries=====>");

        for (Map.Entry<String, List<WeatherObject>> entry : AggregationServer.weatherDataMap.entrySet()) {
            String weatherId = entry.getKey();
            List<WeatherObject> weatherList = entry.getValue();

            System.out.println("Weather Objects for ID: " + weatherId);

            for (WeatherObject weather : weatherList) {
                System.out.println(weather);
            }
            System.out.println("\n");
        }
    }

    public static String buildMsg(BufferedReader bufferedReader, String msg, String reqType) {
        StringBuilder content = new StringBuilder();

        content.append(msg).append("\n");

        try {
            boolean isContent = false;
            while (true) {
                String line = bufferedReader.readLine();
                System.out.println("LINE: " + line);
                if (line.isBlank()) {
                    if (reqType == "GET") {
                        break;
                    } else if (isContent == true){
                        break;
                    } else {
                        isContent = true;
                    }
                }
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to build request into a string..." + e.getMessage() + "\n", e);
        }
        return content.toString().trim();
    }

    public static String getBody(String msg) {
        int contentIndex = msg.indexOf("\n\n");
        if (contentIndex != -1) {
            String requestBody = msg.substring(contentIndex);
            return requestBody;
        } else {
            logger.log(Level.SEVERE, "No content found in the body of the request...\n");
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

    public static void handleReq(BufferedReader bufferedReader, BufferedWriter bufferedWriter, String msg) {
        
        
        try {
            if (msg.length() < 3) {
                logger.log(Level.SEVERE, "Request receieved was invalid...\n");

                Http.write(bufferedWriter, Http.HttpResponse(400));

            } else {
                switch (msg.substring(0, 3)) {
                    case "PUT":
                        msg = buildMsg(bufferedReader, msg, "PUT");
                        // maybe do lamport stuff here, find the time start and then execute the 
                        handlePutReq(bufferedWriter, msg);
                        break;
                    case "GET":
                        msg = buildMsg(bufferedReader, msg, "GET");
                        handleGetReq(bufferedWriter, msg);
                        break;
                    case "POS":
                        msg = buildMsg(bufferedReader, msg, "POST");
                        handlePostReq(bufferedWriter, msg);
                        break;
                    default:
                        System.out.println("Client: " + msg);

                        logger.log(Level.SEVERE, "Request receieved was invalid...\n");

                        String response = Http.HttpResponse(400);
                        Http.write(bufferedWriter, response);
                        break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to handle req properly..." + e.getMessage() + "\n", e);
        }
    }

    public static void updateWeatherValue() {
        for (Map.Entry<String, List<WeatherObject>> entry : AggregationServer.weatherDataMap.entrySet()) {
            List<WeatherObject> weatherList = entry.getValue();

            for (WeatherObject weather : weatherList) {
                weather.setUpdateValue(weather.getUpdateValue()+1);
            }
        }
    }

    public static void removeOutdatedWeather() {
        for (Map.Entry<String, List<WeatherObject>> entry : AggregationServer.weatherDataMap.entrySet()) {
            List<WeatherObject> weatherList = entry.getValue();
            Iterator<WeatherObject> iterator = weatherList.iterator();
    
            while (iterator.hasNext()) {
                WeatherObject weather = iterator.next();
    
                if (weather.getUpdateValue() >= 20) {
                    iterator.remove();

                }
            }
        }
    }
    
    public static void handlePutReq(BufferedWriter bufferedWriter, String msg) {

        logger.log(Level.INFO, "PUT request has been received...\n" + msg + "\n");

        try {
            
            Http.write(bufferedWriter, "PUT request received!\r\n");

            // CHECK IF FILE EXIST
            File weatherFile = new File("filesystem/weather.json");
            String putResponse = "";
            if (weatherFile.exists()) {
                putResponse = Http.HttpResponse(200);
            } else {
                putResponse = Http.HttpResponse(201);
            }

            // Increament all updateValue
            updateWeatherValue();

            Gson gson = new Gson();

            List<String> json = splitJson(getBody(msg));
            List<WeatherObject> weatherData = new ArrayList<>();
            
            for (String entry : json) {
                try {
                    WeatherObject weather = gson.fromJson(entry, WeatherObject.class);
                    weatherData.add(weather);
                    addWeatherData(weather);
                } catch (JsonSyntaxException e) {    
                    putResponse = Http.HttpResponse(500);
                    logger.log(Level.SEVERE, "Failed to parse JSON..." + e.getMessage(), e);
                }
            }

            if (json.size() == 0) {
                putResponse = Http.HttpResponse(204);
            }

            if (putResponse == "HTTP/1.1 201 Created\r\n"){
                saveWeatherPeriodically.run();
            }

            //Remove weather entries that is not in last 20 updates
            removeOutdatedWeather();

            //Testing
            printWeatherMap();

            Http.write(bufferedWriter, putResponse);
    
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to process PUT request..." + e.getMessage(), e);
        }
    
    }

    public static void handleGetReq(BufferedWriter bufferedWriter, String msg) {
        
        logger.log(Level.INFO, "GET request has been received..." + msg + "\n");

        try {

            List<WeatherObject> feed = getFeed();

            Gson gson = new Gson();

            List<String> json = new ArrayList<>();
            for (WeatherObject weather : feed) {
                json.add(gson.toJson(weather));
                //System.out.println(gson.toJson(weather));
            }

            int contentLength = 0;
            for (String entry : json) {
                contentLength += entry.length();
            }

            String response = Http.HttpResponse(200, contentLength);

            for (String entry : json) {
                response += entry + "\r\n"; 
            }
            response += "\r\n";
    
            System.out.println(response);

            Http.write(bufferedWriter, response);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to process GET request..." + e.getMessage(), e);        

        }
    }

    public static void handlePostReq(BufferedWriter bufferedWriter, String msg) {
        
        logger.log(Level.INFO, "POST request has been received...\n" + msg  + "\n");

        try {
    
            Http.write(bufferedWriter, "POST request received!\r\n");
            
            // extract stationID
            msg = getBody(msg);
            int stationId = -1;
            if (!msg.equals("Retrying connection")) {
                stationId = extractStationId(msg);

                // update stationId to true
                if (stationId != -1){
                    handleHeartbeat(stationId);
                } else {
                    logger.log(Level.SEVERE, "Failed to extract stationId from POST req...");        
                }
            }

            String response = Http.HttpResponse(200);

    
            Http.write(bufferedWriter, response);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to process POST request..." + e.getMessage(), e);        

        }
    }

    public static int extractStationId(String msg) {
        int index = msg.indexOf(':');
        int stationId = -1;
        if (index != -1) {
            // Extract the part of the string after the colon
            String restOfMsg = msg.substring(index + 1).trim();

            // Split the remaining text by spaces to get words
            String[] words = restOfMsg.split(" ");

            if (words.length > 0) {
                stationId = Integer.parseInt(words[0]);
            }
        }
        return stationId;
    }

    public synchronized static String weatherDataMapToJson() {

        StringBuilder fullJson = null;

        try {
            fullJson = new StringBuilder();
            Gson gson = new Gson();
    
            for (List<WeatherObject> weatherData : weatherDataMap.values()) {
                for (WeatherObject weather : weatherData) {
                    String json = gson.toJson(weather);
                    fullJson.append(json).append("\n");
                }
            }
        } catch (JsonSyntaxException e) {
            logger.log(Level.SEVERE, "Failed to convert weather entries to JSON..." + e.getMessage() + "\n", e);
        }
        return fullJson.toString();
    }

    private synchronized static void saveWeatherData(String json) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("filesystem/weather.json"))) {
            bufferedWriter.write(json);
            bufferedWriter.newLine();
        } catch (IOException e){
            logger.log(Level.SEVERE, "Failed to save weather data..." + e.getMessage() + "\n", e);        
        }
    }

    public synchronized static void uploadWeatherData() {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("filesystem/weather.json"))) {

            Gson gson = new Gson();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty()) {
                    WeatherObject weather = gson.fromJson(line, WeatherObject.class);
                    addWeatherData(weather);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to upload weather data from local filesystem..." + e.getMessage() + "\n", e);
        }
    }

    public synchronized static void removeStationWeather(int stationId) {
        Iterator<Map.Entry<String, List<WeatherObject>>> iterator = weatherDataMap.entrySet().iterator();
    
        while (iterator.hasNext()) {
            Map.Entry<String, List<WeatherObject>> entry = iterator.next();
            List<WeatherObject> weatherList = entry.getValue();
    
            Iterator<WeatherObject> listIterator = weatherList.iterator();
    
            while (listIterator.hasNext()) {
                WeatherObject weatherObject = listIterator.next();
    
                if (weatherObject.getStationId() == stationId) {
                    listIterator.remove();
                }
            }
    
            if (weatherList.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public synchronized static void handleHeartbeat(int stationId) {
        for (Map.Entry<Integer, Boolean> entry : stationHeartbeat.entrySet()) {
            int stationIdMap = entry.getKey();
            if (stationIdMap == stationId) {
                entry.setValue(true);
                break;
            }
        }
    }
    
    public synchronized static void checkHeartbeat() {
        for (Map.Entry<Integer, Boolean> entry : stationHeartbeat.entrySet()) {
            int stationId = entry.getKey();
            if (entry.getValue() == true){
                stationHeartbeat.put(stationId, false);
            } else {
                removeStationWeather(stationId);
                logger.log(Level.INFO, "Weather from stationId: " + stationId + " has been removed...\n");
            }   
        }
    }

    private static Runnable saveWeatherPeriodically = new Runnable() {
        public void run() {

            String json = weatherDataMapToJson();
            saveWeatherData(json);
            logger.log(Level.INFO, "Weather data has been saved...\n");

        }
    };

    private static Runnable checkHeartbeatPeriodically = new Runnable() {
        public void run() {

            checkHeartbeat();
            logger.log(Level.INFO, "Checking if heartbeat has been recieved for all weather stations...\n");

        }
    };       
    public static void main(String[] args) {

        int maxClients = 1;
        int port = 4567;
        logger.setLevel(Level.INFO);

        // Start logging 
        try {
            FileHandler fileHandler = new FileHandler("logs/aggregationServer.log");
            fileHandler.setFormatter(new SimpleFormatter());
    
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create log file: " + e.getMessage() + "\n", e);
        }


        if (args.length == 1){
            port = Integer.parseInt(args[0]);
        }

        // Upload weather from local filesystem
        File weatherFile = new File("filesystem/weather.json");

        if (weatherFile.exists()) {
            uploadWeatherData();
        }

        // Begin saving weather data to filesystem periodically & checking for heartbeat from content servers
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        while (weatherFile.exists()){
            executor.scheduleAtFixedRate(saveWeatherPeriodically, 0, 10, TimeUnit.SECONDS);
            executor.scheduleAtFixedRate(checkHeartbeatPeriodically, 0, 30, TimeUnit.SECONDS);
            break;
        }

        try {

            logger.log(Level.INFO, "Starting aggregation server on port: " + port + "\n");
            serverSocket = new ServerSocket(port);

            while (true) {

                try {

                    socket = serverSocket.accept();

                    int i = 1;
                    synchronized (threads) {
                        for (i = 0; i < maxClients; i++) {
                            System.out.println(threads[i]); // printing null for some reason wait its fine cause first should be null
                            if (threads[i] == null) {
                                threads[i] = new ClientThread(socket);
                                new Thread(threads[i]).start();
                                break;
                            }
                        }
                    }    
                    
                    if (i == maxClients) {

                        setReaderWriter(socket);

                        Http.write(bufferedWriter, "Server too busy. Please try again later...\r\n");

                        logger.log(Level.INFO, "Connection from a client has been received but server is too busy...\n");

                        Tool.closeSocket(socket);
                        Tool.closeOutputStreamWriter(outputStreamWriter);
                        Tool.closeBufferedWriter(bufferedWriter);

                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Aggregation server socket was unexxpectdly closed...\n");
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Aggregation server failed to listen on port: " + port + "\n");
        }
    }

}
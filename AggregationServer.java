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

/**
 * The Aggregation Server class represents a multi-thread server that manages weather data received from
 * various contetn servers. It listens for clients, handles the requests on new threads, stores new weather
 * data and sends updated weather feeds to GETClients. The server has the ability to periodically save
 * weather data into a local filesystem and checks for content server heartbeat to ensure that weather 
 * received is still up to date.
 *
 */
public class AggregationServer {

    private static Map<String, List<WeatherObject>> weatherDataMap = new HashMap<>();
    private static Map<Integer, Boolean> stationHeartbeat = new HashMap<>();
    public static ClientThread[] threads = new ClientThread[5];
    private static final Logger logger = Logger.getLogger(AggregationServer.class.getName());
    private static LamportClock clock = new LamportClock(0);

    private static Socket socket = null;
    private static ServerSocket serverSocket = null;
    private static InputStreamReader inputStreamReader = null;
    private static OutputStreamWriter outputStreamWriter = null;
    private static BufferedReader bufferedReader = null;
    private static BufferedWriter bufferedWriter = null;

    /**
     * Sets up the reader and writer for the socket.
     *
     * @param socket The socket connection to a client.
     * @throws IOException If an I/O error occurs while setting up the reader and writer.
     */
    private static void setReaderWriter(Socket socket) throws IOException {
        
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        bufferedWriter = new BufferedWriter(outputStreamWriter);

    }

    /**
     * Retrieves the latest weather data feed by selecting the most recent data for each weather Id.
     *
     * @return A list of WeatherObject instances representing the latest weather data feed.
     */
    protected static List<WeatherObject> getFeed() {
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

    /**
     * Updates the heartbeat status for the station who has given weather data.
     *
     * @param weather The WeatherObject representing the weather data from a station.
     */
    private synchronized static void updateStationHeartbeat(WeatherObject weather) {
        int stationId = weather.getStationId();
        stationHeartbeat.put(stationId, true);
    }

    /**
     * Adds weather data to the server storage, associating it with a specific weather station.
     *
     * @param weather The WeatherObject representing the weather data to be added.
     */
    protected synchronized static void addWeatherData(WeatherObject weather) {

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

    /**
     * Prints the weather entries stored in the AggregationServer's weather data map.
     */
    protected static void printWeatherMap() {

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

    /**
     * Builds a message from the input BufferedReader, given the message content, and depends on the request type.
     *
     * @param bufferedReader The BufferedReader to read the message from.
     * @param msg           The initial message content.
     * @param reqType       The type of request (PUT, GET, or POST).
     * @return A string containing the complete message.
     */
    private static String buildMsg(BufferedReader bufferedReader, String msg, String reqType) {
        StringBuilder content = new StringBuilder();

        content.append(msg).append("\n");

        try {
            boolean isContent = false;
            while (true) {
                String line = bufferedReader.readLine();
                if (line.trim().isEmpty()) {
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

    /**
     * Handles the ordering of requests based on Lamport clocks by giving 
     * out of order request time to be processed.
     *
     * @param msg The incoming message with Lamport clock information.
     * @throws InterruptedException If interrupted while waiting for previous requests to complete.
     */
    private static void handleClockOrder(String msg) throws InterruptedException {
        
        int clientClock = Http.extractLamportClock(msg);
        int serverClock = clock.getClock();
        while (true) {
            if (clientClock <= serverClock) {
                break;
            } else if (clientClock == serverClock + 1) {
                Thread.sleep(250); 
                break;
            } else {
                Thread.sleep(250);
            }
        }
    }

    /**
     * Handles incoming requests of type GET, PUT and POST requests.
     *
     * @param bufferedReader The BufferedReader containing the request message.
     * @param bufferedWriter The BufferedWriter for writing the response.
     * @param msg           The incoming message.
     */
    protected static void handleReq(BufferedReader bufferedReader, BufferedWriter bufferedWriter, String msg) {        
        
        try {
            if (msg.length() < 3) {
                logger.log(Level.SEVERE, "Request receieved was invalid... Request: " + msg + "\n");
                Http.write(bufferedWriter, Http.HttpResponse(400));
                Tool.closeBufferedReader(bufferedReader);
                Tool.closeBufferedWriter(bufferedWriter);

            } else {
                try {
                    switch (msg.substring(0, 3)) {
                        case "PUT":
                            msg = buildMsg(bufferedReader, msg, "PUT");
                            handleClockOrder(msg);
                            handlePutReq(bufferedWriter, msg);
                            break;
                        case "GET":
                            msg = buildMsg(bufferedReader, msg, "GET");
                            handleClockOrder(msg);
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
                } catch (InterruptedException e) {
                    System.out.println("Error: Failed to wait for previous request");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to handle req properly..." + e.getMessage() + "\n", e);
        }
    }

    /**
     * Updates the updateValue attribute of all weather objects in the AggregationServer's data map.
     * This value describes how many new weather entries have arrived since it has arrived.
     */
    protected static void updateWeatherValue() {
        for (Map.Entry<String, List<WeatherObject>> entry : AggregationServer.weatherDataMap.entrySet()) {
            List<WeatherObject> weatherList = entry.getValue();

            for (WeatherObject weather : weatherList) {
                weather.setUpdateValue(weather.getUpdateValue()+1);
            }
        }
    }

    /**
     * Removes outdated weather data from the AggregationServer's data map based on updateValue.
     * Removes weather data that is atleast 20 entries old.
     */
    protected static void removeOutdatedWeather() {
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
    
    /**
     * Handles PUT requests, updates Lamport clocks, processes the request, and generates a response.
     *
     * @param bufferedWriter The BufferedWriter for writing the response.
     * @param msg           The PUT request message.
     */
    private static void handlePutReq(BufferedWriter bufferedWriter, String msg) {

        logger.log(Level.INFO, "PUT request has been received...\n" + msg + "\n");
        int sentClock = Http.extractLamportClock(msg);
        clock.updateClock(sentClock);

        try {
            
            Http.write(bufferedWriter, "PUT request received!\r\n");

            File weatherFile = new File("filesystem/weather.json");
            String putResponse = "";
            if (weatherFile.exists()) {
                putResponse = Http.HttpResponse(200);
            } else {
                putResponse = Http.HttpResponse(201);
            }

            updateWeatherValue();

            List<String> json = Tool.splitJson(Http.getBody(msg));
            List<WeatherObject> weatherData = new ArrayList<>();
            
            for (String entry : json) {
                try {
                    WeatherObject weather = Tool.deserializeJson(entry);
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

            removeOutdatedWeather();
            printWeatherMap();

            Http.write(bufferedWriter, putResponse);
    
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to process PUT request..." + e.getMessage(), e);
        }
    
    }

    /**
     * Handles GET requests, updates Lamport clocks, processes the request, and generates a response.
     *
     * @param bufferedWriter The BufferedWriter for writing the response.
     * @param msg           The GET request message.
     * @throws IOException If an IO error occurs while processing the request.
     */
    private static void handleGetReq(BufferedWriter bufferedWriter, String msg) throws IOException {
        
        logger.log(Level.INFO, "GET request has been received...\n" + msg + "\n");
        int sentClock = Http.extractLamportClock(msg);
        clock.updateClock(sentClock);

        try {
            
            if (msg.contains("lamportClock")) {
                String response = Http.HttpResponse(200, clock);
                System.out.println(response);
                Http.write(bufferedWriter, response);
                return;
            }

            List<WeatherObject> feed = getFeed();

            String body = Tool.serializeJson(feed);

            int contentLength = body.length();

            String response = Http.HttpResponse(200, contentLength);
            response += body + "\r\n\r\n";
    
            System.out.println(response);

            Http.write(bufferedWriter, response);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to process GET request..." + e.getMessage(), e);        

        }
    }

    /**
     * Handles POST requests, updates station heartbeat or accepts retried connection, and generates a response.
     *
     * @param bufferedWriter The BufferedWriter for writing the response.
     * @param msg           The POST request message.
     */
    private static void handlePostReq(BufferedWriter bufferedWriter, String msg) {
        
        logger.log(Level.INFO, "POST request has been received...\n" + msg  + "\n");

        try {
    
            Http.write(bufferedWriter, "POST request received!\r\n");
            
            msg = Http.getBody(msg);
            int stationId = -1;
            if (!msg.equals("Retrying connection")) {
                stationId = extractStationId(msg);
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

    /**
     * Extracts the station ID from the provided body of a POST request.
     *
     * @param msg The message containing station ID information.
     * @return The extracted station ID or -1 if not found or invalid.
     */
    protected static int extractStationId(String msg) {
        int index = msg.indexOf(':');
        int stationId = -1;
        if (index != -1) {
            String restOfMsg = msg.substring(index + 1).trim();
            String[] words = restOfMsg.split(" ");

            if (words.length > 0) {
                stationId = Integer.parseInt(words[0]);
            }
        }
        return stationId;
    }

    /**
     * Converts the data in the weatherDataMap to a JSON-formatted string.
     *
     * @return The JSON string containing weather data.
     */
    private synchronized static String weatherDataMapToJson() {

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

    /**
     * Saves the weather data to a file in JSON format.
     *
     * @param json The JSON-formatted weather data to save.
     */
    private synchronized static void saveWeatherData(String json) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("filesystem/weather.json"))) {
            bufferedWriter.write(json);
            bufferedWriter.newLine();
        } catch (IOException e){
            logger.log(Level.SEVERE, "Failed to save weather data..." + e.getMessage() + "\n", e);        
        }
    }

    /**
     * Uploads weather data from a local JSON file into the weatherDataMap.
     */
    private synchronized static void uploadWeatherData() {

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

    /**
     * Removes weather data with the specific station ID from the weatherDataMap.
     *
     * @param stationId The station ID for which to remove associated data.
     */
    private synchronized static void removeStationWeather(int stationId) {
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

    /**
     * Updates the heartbeat status for a station ID in the stationHeartbeat map.
     *
     * @param stationId The station ID to update the heartbeat status for.
     */
    private synchronized static void handleHeartbeat(int stationId) {
        for (Map.Entry<Integer, Boolean> entry : stationHeartbeat.entrySet()) {
            int stationIdMap = entry.getKey();
            if (stationIdMap == stationId) {
                entry.setValue(true);
                break;
            }
        }
    }
    
    /**
     * Checks the heartbeat status of stations and removes data for stations with no recent heartbeat.
     */
    private synchronized static void checkHeartbeat() {
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

    /**
     * Runnable task for periodically saving weather data to a JSON file.
     */
    private static Runnable saveWeatherPeriodically = new Runnable() {
        public void run() {

            String json = weatherDataMapToJson();
            saveWeatherData(json);
            logger.log(Level.INFO, "Weather data has been saved...\n");

        }
    };

    /**
     * Runnable task for periodically checking station heartbeat status and removing outdated data.
     */
    private static Runnable checkHeartbeatPeriodically = new Runnable() {
        public void run() {

            checkHeartbeat();
            logger.log(Level.INFO, "Checking if heartbeat has been recieved for all weather stations...\n");

        }
    };   
    
    /**
     * The main class for the Aggregation Server, responsible for handling client connections and requests.
     *
     * @param args Command-line arguments. Optionally choose the port for the server.
     */
    public static void main(String[] args) {
        int maxClients = 15;
        int port = 4567;
        logger.setLevel(Level.INFO);

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

        File weatherFile = new File("filesystem/weather.json");
        if (weatherFile.exists()) {
            uploadWeatherData();
        }

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
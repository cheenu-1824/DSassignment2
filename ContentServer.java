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

import lib.*;

/**
 * This class represents a content server that communicates with an aggregation server to input weather data.
 */
public class ContentServer {

    private static int stationId = -1;
    private static Socket socket = null;
    private static InputStreamReader inputStreamReader = null;
    private static OutputStreamWriter outputStreamWriter = null;
    private static BufferedReader bufferedReader = null;
    private static BufferedWriter bufferedWriter = null;
    private static LamportClock clock = new LamportClock(0);


    /**
     * Generates a random station ID and assigns it to the content servers stationId variable.
     * Uses oldStationId as the content server stationId to simulate a content server restart.
     */
    private static void setStationId(int oldStationId) {

        if (oldStationId != -1) {
            ContentServer.stationId = oldStationId;
            return;
        }

        Random random = new Random();
        int randomNumber = random.nextInt(1000000);
        ContentServer.stationId = randomNumber;

    }

    /**
     * Sets up the reader and writer for the socket.
     *
     * @param socket The socket connection to the server.
     * @throws IOException If an I/O error occurs while setting up the reader and writer.
     */
    private static void setReaderWriter(Socket socket) throws IOException {
        
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        bufferedWriter = new BufferedWriter(outputStreamWriter);

    }

    /**
     * Reads the contents of a file and returns it as a string.
     *
     * @param filename The name of the file to read.
     * @return The content of the file as a string.
     */
    protected static String readFile(String filename) {

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

    /**
     * Parses the input weather data string and constructs it as a WeatherObject.
     *
     * @param input The input string representing weather data.
     * @return A WeatherObject containing the parsed data.
     */
    protected static WeatherObject buildWeatherObject(String input) {

        String[] lines = input.split("\n");
        WeatherObject weather = new WeatherObject();

        for (String line : lines) {

            String[] objectParts = line.split(":");

            String parameter = objectParts[0].trim();
            String value = line.substring(line.indexOf(":") + 1).trim();

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
                    weather.setLat(Double.parseDouble(value));
                    break;
                case "lon":
                    weather.setLon(Double.parseDouble(value));
                    break;
                case "local_date_time":
                    weather.setLocal_date_time(value);
                    break;
                case "local_date_time_full":
                    weather.setLocal_date_time_full(value);
                    break;
                case "air_temp":
                    weather.setAir_temp(Double.parseDouble(value));
                    break;
                case "apparent_t":
                    weather.setApparent_t(Double.parseDouble(value));
                    break;
                case "cloud":
                    weather.setCloud(value);
                    break;
                case "dewpt":
                    weather.setDewpt(Double.parseDouble(value));
                    break;
                case "press":
                    weather.setPress(Double.parseDouble(value));
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

    /**
     * Splits a string of weather datas into separate entries.
     *
     * @param input The input string containing weather data.
     * @return A list of strings, each representing a weather entry.
     */
    protected static List<String> splitWeatherData(String input) {
        String[] weatherData = input.split("id:");
    
        List<String> formattedWeatherData = new ArrayList<>();
    
        for (int i = 1; i < weatherData.length; i++) {
            formattedWeatherData.add("id:" + weatherData[i].trim());
        }
    
        return formattedWeatherData;
    }

    /**
     * Removes invalid weather entries from the list.
     *
     * @param weatherData A list of weather data entries.
     * @return A list of weather data entries with invalid entries removed.
     */

    protected static List<String> removeInvalidWeather(List<String> weatherData) {
        Iterator<String> iterator = weatherData.iterator();
        while (iterator.hasNext()) {

            String entry = iterator.next();
            String[] lines = entry.split("\n");

            for (String line : lines) {
                if (!Tool.isEntryValid(line)) {
                    if (line != "eof") {
                        iterator.remove();
                        break;
                    }
                }
            }

            if (entry.contains("eof")) {
                entry = entry.replaceAll("\\b" + "eof" + "\\b", "");
            }
        }
        return weatherData;
    }

    /**
     * Sends a heartbeat to the aggregation server to indicate that the content server is still alive.
     *
     * @param serverAddress The address of the aggregation server.
     * @param port The port number of the aggregation server.
     */
    private static void sendHeartbeat(String serverAddress, int port) {

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

            Http.postRequest(bufferedWriter, Integer.toString(ContentServer.stationId), false);
            Tool.networkDelay(100);
            boolean sentReq = true;
            String response = "";
            response = bufferedReader.readLine();


                if (response.equals("Server too busy. Please try again later...")) {
                    sentReq = false;

                    Tool.closeSocket(socket);
                    socket = Http.retryConnection(serverAddress, port);
                    if (socket != null) {
                        inputStreamReader = new InputStreamReader(socket.getInputStream());
                        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                        bufferedReader = new BufferedReader(inputStreamReader);
                        bufferedWriter = new BufferedWriter(outputStreamWriter);

                        if (sentReq == false) {
                            Http.postRequest(bufferedWriter,"", false);
                        }
                    } else {
                        System.err.println("Error: Failed to establish a connection to the server, please reconnect to restore weather data to the server");
                    }
                }
            Http.read(bufferedWriter, bufferedReader);
            
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

    /**
     * Runnable for sending a heartbeat periodically every 15 seconds.
     *
     * @param serverAddress The address of the aggregation server.
     * @param port The port number of the aggregation server.
     * @return A runnable object for sending a heartbeat.
     */
    private static Runnable sendHeartbeatPeriodically(final String serverAddress, final int port) {
        return new Runnable() {
            public void run() {
                sendHeartbeat(serverAddress, port);
            }
        };
    }

    /**
     * Handles Lamport clock synchronization to order events with the aggregation server.
     *
     * @param bufferedReader The BufferedReader for reading server responses.
     * @param body The request body to send to the server.
     * @return The response from the server.
     */
    public static String handleLamportClock(BufferedReader bufferedReader, String body) {
        
        String response = "";

        try {
            String lamportClockHeader = bufferedReader.readLine();
            System.out.println(lamportClockHeader);
            System.out.println(bufferedReader.readLine());
            int serverClock = Http.extractLamportClock(lamportClockHeader);
            clock.updateClock(serverClock);
            System.out.println(clock);

            Http.putRequest(bufferedWriter, clock, body);
            response = bufferedReader.readLine();

        } catch (IOException e) {
            System.out.println("Error: Failed to retrieve server lamport clock");
        }

        return response;

    }

    /**
     * Main method for running the content server.
     *
     * @param args Command-line arguments for server configuration.
     */
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 4567;
        String filename = "content/test.txt";

        if (args.length != 2 && args.length != 3){
            System.out.println("Incorrect parameters, input should be as follows: java ContentServer <domain:port> <file location> <stationId (Not required)>");
            System.exit(1);
        }
        if (args.length == 3) {
            setStationId(Integer.parseInt(args[2]));
        } else {
            setStationId(-1);
        }
        String[] splitURL = Tool.parseURL(args[0]);
        serverAddress = splitURL[0];
        port = Integer.parseInt(splitURL[1]);
        filename = args[1];

        String content = readFile(filename);
        List<String> weatherData = splitWeatherData(content);
        weatherData = removeInvalidWeather(weatherData);
        if (weatherData.isEmpty()) {
            System.out.println("Error: No valid weather data found from the input file...");
            System.exit(1);
        }
        List<WeatherObject> weathers = new ArrayList<>();
        for (String weather : weatherData) {
            weathers.add(buildWeatherObject(weather));
        }
        String body = Tool.serializeJson(weathers);

        try {
            socket = new Socket(serverAddress, port);
            setReaderWriter(socket);
            Http.getRequest(bufferedWriter, clock);
            Tool.networkDelay(100);
            boolean sentReq = true;
            String response = "";
            response = bufferedReader.readLine();
            System.out.println(response);
            if (response.equals("Server too busy. Please try again later...")) {
                sentReq = false;
                socket = Http.retryConnection(serverAddress, port);
                if (socket != null) {
                    setReaderWriter(socket);
                } else {
                    System.err.println("Error: Failed to establish a connection to the server, please check host location or try again later...");
                    System.exit(1);
                }
            }
            if (sentReq == false && socket != null) {
                Http.getRequest(bufferedWriter, clock);
            }

            response = handleLamportClock(bufferedReader, body);
            Http.read(bufferedWriter, bufferedReader);
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
            executor.scheduleWithFixedDelay(sendHeartbeatPeriodically(serverAddress, port), 0, 15, TimeUnit.SECONDS);
        }
    }
}

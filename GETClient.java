import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;

import lib.*;

/**
 * The GETClient class represents a client making HTTP GET requests
 * to retrieve updated weather data from an aggregation server.
 */
public class GETClient {

    private static Socket socket = null;
    private static InputStreamReader inputStreamReader = null;
    private static OutputStreamWriter outputStreamWriter = null;
    private static BufferedReader bufferedReader = null;
    private static BufferedWriter bufferedWriter = null;
    private static LamportClock clock = new LamportClock(0);


    /**
     * Sets up the reader and writer for the socket.
     *
     * @param socket The socket connection to the server.
     * @throws IOException If an I/O error occurs while setting up the reader and writer.
     */
    public static void setReaderWriter(Socket socket) throws IOException {
        
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        bufferedWriter = new BufferedWriter(outputStreamWriter);

    }

    /**
     * Builds a full message from the lines read from the buffered reader.
     *
     * @param bufferedReader The buffered reader to read the message from.
     * @return The complete message as a string.
     */
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

    /**
     * Handles a response from the aggregation server, retrieves weather data,
     * and returns it as a list of WeatherObject instances.
     *
     * @param bufferedReader The buffered reader for reading the server's response.
     * @return A list of WeatherObject instances containing weather data.
     */
    public static List<WeatherObject> handleRes(BufferedReader bufferedReader) {
        
        String msg = buildMsg(bufferedReader);
        String body = Http.getBody(msg);
        System.out.println(msg);
        
        if (body == null) {
            return null;
        }

        List<String> json = Tool.splitJson(body);
        List<WeatherObject> weatherData = new ArrayList<>();
        
        for (String entry : json) {
            weatherData.add(Tool.deserializeJson(entry));
        }
       return weatherData;

    } 

    /**
     * Displays weather data nicely formatted in the console.
     *
     * @param weatherData A list of WeatherObject instances containing weather data.
     */
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
    
    public static void handleLamportClock(BufferedReader bufferedReader) {
        
        try {
            String lamportClockHeader = bufferedReader.readLine();
            System.out.println(lamportClockHeader);
            System.out.println(bufferedReader.readLine());
            int serverClock = Http.extractLamportClock(lamportClockHeader);
            clock.updateClock(serverClock);

            Http.getRequest(bufferedWriter, clock);
        } catch (IOException e) {
            System.out.println("Error: Failed to retrieve server lamport clock");
        }

    }

    /**
     * The main method for the GETClient program used to connect to the aggregation server,
     * sends requests, retrieves weather data, and displays it.
     *
     * @param args Command-line arguments (URL in the format http://<domain>:<port>).
     */
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 4567;
        if (args.length != 1){
            System.out.println("Incorrect parameters, input should be as follows: java GETClient http://<domain:port>");
            System.exit(1);
        }
        String[] splitURL = Tool.parseURL(args[0]);
        serverAddress = splitURL[0];
        port = Integer.parseInt(splitURL[1]);

        try {
            socket = new Socket(serverAddress, port);
            setReaderWriter(socket);
            Http.getRequest(bufferedWriter, clock);
            boolean sentReq = true;
            String response = bufferedReader.readLine();
            System.out.println(response);

            if (!response.equals("HTTP/1.1 200 OK")) {
                sentReq = false;
                Tool.closeSocket(socket);
                socket = Http.retryConnection(serverAddress, port);
                if (socket != null) {
                    setReaderWriter(socket);
                } else {
                    System.err.println("Error: Failed to establish a connection to the server, please reconnect to restore weather data to the server");
                }
            }
            if (sentReq == false && socket != null) {
                Http.getRequest(bufferedWriter, clock);
                response = bufferedReader.readLine();
                System.out.println(response);
            }

            handleLamportClock(bufferedReader);

            List<WeatherObject> weatherData = handleRes(bufferedReader);
            bufferedReader.readLine();

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

            // Simulate network delay
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
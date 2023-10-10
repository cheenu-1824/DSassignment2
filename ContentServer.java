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


public class ContentServer {

    private static int stationId = -1;
    private static Socket socket = null;
    private static InputStreamReader inputStreamReader = null;
    private static OutputStreamWriter outputStreamWriter = null;
    private static BufferedReader bufferedReader = null;
    private static BufferedWriter bufferedWriter = null;
    private LamportClock clock = new LamportClock(0);


    private static void setStationId() {
        Random random = new Random();
        int randomNumber = random.nextInt(1000000);
        ContentServer.stationId = randomNumber;

    }

    private static void setReaderWriter(Socket socket) throws IOException {
        
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        bufferedWriter = new BufferedWriter(outputStreamWriter);

    }

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

    protected static List<String> splitWeatherData(String input) {
        String[] weatherData = input.split("id:");
    
        List<String> formattedWeatherData = new ArrayList<>();
    
        for (int i = 1; i < weatherData.length; i++) {
            formattedWeatherData.add("id:" + weatherData[i].trim());
        }
    
        return formattedWeatherData;
    }

    protected static List<String> removeInvalidWeather(List<String> weatherData) { // eof thing needs to be implemented
        Iterator<String> iterator = weatherData.iterator();
        while (iterator.hasNext()) {
            String entry = iterator.next();
            if (entry.contains("id:name")) { // THIS MAY NOT BE RIGHT, DEPENDS IF ERROR ID MEANS ID FIELD IS BLANK
                iterator.remove();
            }
        }
        return weatherData;
    }

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

    private static Runnable sendHeartbeatPeriodically(final String serverAddress, final int port) {
        return new Runnable() {
            public void run() {
                sendHeartbeat(serverAddress, port);
            }
        };
    }

    public static void main(String[] args) {
        
        String serverAddress = "localhost";
        int port = 4567;
        String filename = "content/test.txt";

        setStationId();

        if (args.length != 2){
            System.out.println("Incorrect parameters, input should be as follows: java ContentServer <domain:port> <file location>");
            System.exit(1);
        }

        // Parse URL in server address and port
        String[] splitURL = Tool.parseURL(args[0]);
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
        String body = Tool.serializeJson(weathers);

        try {

            socket = new Socket(serverAddress, port);

            setReaderWriter(socket);

            Http.putRequest(bufferedWriter, body);
            Tool.networkDelay(100);
            boolean sentReq = true;
            String response = "";

            response = bufferedReader.readLine();
            if (response.equals("Server too busy. Please try again later...")) {
                sentReq = false;
                socket = Http.retryConnection(serverAddress, port);
                if (socket != null) {
                    setReaderWriter(socket);

                    if (sentReq == false) {
                        Http.putRequest(bufferedWriter, body);
                    }
                    response = bufferedReader.readLine();
                } else {
                    System.err.println("Error: Failed to establish a connection to the server, please check host location or try again later...");
                    System.exit(1);
                }
            }
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

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.google.gson.Gson;


public class ContentServer {

    private static int stationId = -1;

    public static void menu() {
        
        System.out.println("<===Content Server Menu===>");
        System.out.println("1) Change heartbeat rate");
        System.out.println("2) Shutdown content server");
        System.out.println("<=========================>");


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

        String putMessage = "PUT /weather.json HTTP/1.1\r\n"
                    + "User-Agent: ATOMClient/1/0\r\n"
                    + "Content-Type: application/json\r\n" // I NEED TO WORK THIS OUT
                    + "Content-Length: " + contentLength +"\r\n\r";
        for (String entry : json) {
            putMessage += "\n" + entry; 
        }
        putMessage += "\r\n";

        System.out.println(putMessage);

        try {

            bufferedWriter.write(putMessage);
            bufferedWriter.newLine();
            bufferedWriter.flush();

        } catch (IOException e ){
            System.out.println("Error: Failed to send PUT request the the server...");
        } 

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
        String filename = "content/test.txt";

        Random random = new Random();
        int randomNumber = random.nextInt(1000000);
        ContentServer.stationId = randomNumber;

        if (args.length != 2){
            System.out.println("Incorrect parameters, input should be as follows: java ContentServer <domain:port> <filename>");
            System.exit(1);
        }

        // Parse URL in server address and port
        String[] splitURL = parseURL(args[0]);
        serverAddress = splitURL[0];
        port = Integer.parseInt(splitURL[1]);
        filename = args[1];

        // Read file from file system
        String content = readFile(filename);
        //System.out.println(content);

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
            //System.out.println(gson.toJson(weather));
        }

        try {

            // Establish connection to aggregation server
            socket = new Socket(serverAddress, port);

            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            scanner = new Scanner(System.in);

            
            // Send put request
            putReq(bufferedWriter, json);
            String response = "";

            while (true) {

                response = bufferedReader.readLine();
                System.out.println("Server: " + response);
                
                if (response != null || response.isEmpty()) {
                    bufferedWriter.write("BYE");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("Server: " + bufferedReader.readLine());
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

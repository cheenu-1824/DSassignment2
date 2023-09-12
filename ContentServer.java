import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Map;

import com.google.gson.Gson;


public class ContentServer {

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
        return weather;
    }


    public static void main(String[] args) {
        
        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        Scanner scanner = null;
        String serverAddress = "localhost";
        int port = 9999;
        String filename = "content/test.txt";

        if (args.length != 2){
            System.out.println("Incorrect parameters, input should be as follows: make contentServer <domain:port> <filename>");
            System.exit(1);
        }

        // Parse URL in domain and port
        String[] splitURL = parseURL(args[0]);
        serverAddress = splitURL[0];
        port = Integer.parseInt(splitURL[1]);
        filename = args[1];


        System.out.println(serverAddress);
        System.out.println(port);
        System.out.println(filename);

        // Read file from file system
        String content = readFile(filename);
        System.out.println(content);

        // Build objects for each entry in content
        WeatherObject weather1 = buildWeatherObject(content);
        System.out.println(weather1);

        // Serializes object to JSON string
        Gson gson = new Gson();
        String json = gson.toJson(weather1);
        System.out.println(json);



        //Map<String, Object> map = gson.fromJson(content, Map.class);
        //System.out.println(map);


        try {

            // Establish connection to aggregation server
            socket = new Socket(serverAddress, port);


            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            scanner = new Scanner(System.in);

            while (true) {
                
                String msg = scanner.nextLine();
                bufferedWriter.write(msg);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                System.out.println("Server: " + bufferedReader.readLine());

                if (msg.equalsIgnoreCase("BYE")){
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

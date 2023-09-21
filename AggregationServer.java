import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;


public class AggregationServer {

    private static Map<String, List<WeatherObject>> weatherDataMap = new HashMap<>();

    public static List<WeatherObject> getFeed() {
        List<WeatherObject> feed = new ArrayList<>();

        for (List<WeatherObject> weatherList : AggregationServer.weatherDataMap.values()) {
            if (!weatherList.isEmpty()) {

                WeatherObject recentWeather = weatherList.get(weatherList.size() - 1);
                feed.add(recentWeather);
            }
        }
        return feed;
    }

    public static void addWeatherData(WeatherObject weather) {

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

    public static String buildMsg(BufferedReader bufferedReader, String reqType) {
        StringBuilder content = new StringBuilder();
        try {
            boolean isContent = false;
            while (true) {
                String line = bufferedReader.readLine();
                if (line.isEmpty()) {
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
            System.out.println("Error: Failed to build request into a string...");
        }
        return content.toString().trim();
    }

    public static String getBody(String msg) {
        int contentIndex = msg.indexOf("{");
        if (contentIndex != -1) {
            String requestBody = msg.substring(contentIndex);
            return requestBody;
        } else {
            System.out.println("Error: No content found in the body of the request...");
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
                System.out.println("Error: Request receieved was too short...");
                bufferedWriter.write("Error: Request too short, please try again...");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } else {
                switch (msg.substring(0, 3)) {
                    case "PUT":
                        msg = buildMsg(bufferedReader, "PUT");
                        handlePutReq(bufferedWriter, msg);
                        break;
                    case "GET":
                        msg = buildMsg(bufferedReader, "GET");
                        handleGetReq(bufferedWriter, msg);
                        break;
                    default:
                        System.out.println("Client: " + msg);
        
                        bufferedWriter.write("MSG received!");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        break;
                }
            }

        } catch (IOException e) {
            System.out.println("Error: Failed to handle req properly...");
        }

    }

    public static void handlePutReq(BufferedWriter bufferedWriter, String msg) {

        System.out.println("PUT request:\n" + msg + "\n");
        
        try {
    
            bufferedWriter.write("PUT request received!");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Gson gson = new Gson();

            List<String> json = splitJson(getBody(msg));
            List<WeatherObject> weatherData = new ArrayList<>();
            
            for (String entry : json) {
                weatherData.add(gson.fromJson(entry, WeatherObject.class));
                addWeatherData(gson.fromJson(entry, WeatherObject.class));
            }

            //Testing
            printWeatherMap();
    
        } catch (IOException e) {
            System.out.println("Error: Failed to process PUT request...");
        }
    
    }

    public static void handleGetReq(BufferedWriter bufferedWriter, String msg) {
        
        System.out.println("GET request:\n" + msg + "\n");

        try {
    
            bufferedWriter.write("GET request received!");
            bufferedWriter.newLine();
            bufferedWriter.flush();

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

            String response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json\r\n" // I NEED TO WORK THIS OUT
                    + "Content-Length: " + contentLength +"\r\n\r";

            for (String entry : json) {
                response += "\n" + entry; 
            }
            response += "\r\n";
    
            System.out.println(response);

            bufferedWriter.write(response);
            bufferedWriter.newLine();
            bufferedWriter.flush();

        } catch (IOException e) {
            System.out.println("Error: Failed to process GET request...");
        }
    }

    public static String weatherDataMapToJson() {

        StringBuilder fullJson = new StringBuilder();
        Gson gson = new Gson();

        for (List<WeatherObject> weatherData : weatherDataMap.values()) {
            for (WeatherObject weather : weatherData) {
                String json = gson.toJson(weather);
                fullJson.append(json).append("\n");
            }
        }
        return fullJson.toString();
    }

    private static void saveWeatherData(String json) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("filesystem/weather.json"))) {
            bufferedWriter.write(json);
            bufferedWriter.newLine();
        } catch (IOException e){
            System.out.println("Error: Failed save weather data...");
        }
        System.out.println("Weather data has been saved...");
    }

    public static void uploadWeatherData() {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("filesystem/weather.json"))) {

            Gson gson = new Gson();
            String line;

            while ((line = bufferedReader.readLine()) != null) {

                WeatherObject weather = gson.fromJson(line, WeatherObject.class);
                addWeatherData(weather);
                
            }


        } catch (IOException e) {
            System.out.println("Error: Failed to upload weather data from local filesystem...");
        }
    }

    private static Runnable saveWeatherPeriodically = new Runnable() {
        public void run() {

            String json = weatherDataMapToJson();
            saveWeatherData(json);

        }
    };
        

    public static void main(String[] args) {

        Socket socket = null;
        ServerSocket serverSocket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        String serverAddress = "localhost";
        int port = 4567;

        System.out.println("Starting aggregation server on port: " + port);

        // Upload weather from local filesystem
        //uploadWeatherData(); FIXX

        // Begin saving weather data to filesystem periodically
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(saveWeatherPeriodically, 0, 60, TimeUnit.SECONDS);

        try {
            
            serverSocket = new ServerSocket(port);

            while (true) {

                try {

                    socket = serverSocket.accept();

                    // int maxClients 5;
                    // int i = 0;
                    // for (i = 0; i < maxClients; i++){
                    //     if (threads[i] == null){
                    //         (threads[i] = new clientThread(socket, threads)).start();
                    //         break;
                    //     }
                    // }

                    // if (i == maxClients) {
                    //     bufferedWriter.write("Server too busy! Please try again later...");
                    //     bufferedWriter.newLine();
                    //     bufferedWriter.flush();

                    //     socket.close();
                    // }

                    inputStreamReader = new InputStreamReader(socket.getInputStream());
                    outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                    bufferedReader = new BufferedReader(inputStreamReader);
                    bufferedWriter = new BufferedWriter(outputStreamWriter);

                    while (true) {

                        String msg = bufferedReader.readLine();

                        if (msg.equalsIgnoreCase("BYE")){
                            bufferedWriter.write("BYE!");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            break;
                        }

                        // Handle requests "GET", "PUT"
                        handleReq(bufferedReader, bufferedWriter, msg);

                    }

                    socket.close();
                    inputStreamReader.close();
                    outputStreamWriter.close();
                    bufferedReader.close();
                    bufferedWriter.close();

                } catch (IOException e) {
                    System.out.println("Aggregation server socket was closed...");
                }
            }

        } catch (IOException e) {
            System.out.println("Aggregation server could not listen on port: " + port);
        }
    }
}
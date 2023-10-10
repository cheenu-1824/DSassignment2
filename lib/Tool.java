package lib;

import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;

/**
 * The Tool class provides reusable methods for various tasks, including JSON handling,
 * network delay simulation, URL parsing, and resource closing.
 */
public class Tool {

    /**
     * Splits a JSON message into individual JSON objects and stores them in a list.
     *
     * @param msg The JSON message to split.
     * @return A list of individual JSON objects as strings.
     */
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

    /**
     * Converts a list of strings into a single string separeted by new lines.
     *
     * @param listOfStrings The list of strings to be joined.
     * @return A single string with new lines between each element of the list.
     */
    public static String listStringsToString(List<String> listOfStrings) {
        String body = "";

        for (String listEntry : listOfStrings) {
            body += listEntry + "\n"; 
        }
        return body;
    }

    /**
     * Deserializes a JSON formatted string into a WeatherObject instance.
     *
     * @param jsonString The JSON formatted string to be deserialized.
     * @return A WeatherObject instance representing the deserialized weather data.
     */
    public static WeatherObject deserializeJson(String jsonString) {
        Gson gson = new Gson();

        WeatherObject weather = gson.fromJson(jsonString, WeatherObject.class);

        return weather;
    }

    /**
     * Serializes a list of WeatherObject instances into a JSON formatted string.
     *
     * @param weatherList The list of WeatherObject instances to be serialized.
     * @return A JSON formatted string containing the serialized weather data.
     */
    public static String serializeJson(List<WeatherObject> weatherList) {

        Gson gson = new Gson();
        List<String> json = new ArrayList<>();
        for (WeatherObject weather : weatherList) {
            json.add(gson.toJson(weather));
        }
        return listStringsToString(json);
    }

    /**
     * Simulates a network delay by pausing the current thread for the specified number of milliseconds.
     *
     * @param milisec The number of milliseconds to pause the thread.
     */
    public static void networkDelay(int milisec) {
        try {
            Thread.sleep(milisec);
        } catch (InterruptedException e) {
            System.out.println("Error: Failed to simulate network delay");
        }
    }

       /**
     * Parses a URL string in the format "http://<domain>:<port>" and returns an array of the domain and port.
     *
     * @param url The URL string to be parsed.
     * @return An array of two strings in the format [domain, port].
     * @throws IllegalArgumentException If the URL format is invalid.
     */
    public static String[] parseURL(String url) {

        url = url.substring(7);
        
        String[] splitURL = url.split(":");
        
        if (splitURL.length == 2) {
            return splitURL;
        } else {
            throw new IllegalArgumentException("Invalid URL format: " + url + ". Correct usage: <domain>:<port>");
        }
    }

    /**
     * Closes a socket and handles IOException if occurs.
     *
     * @param socket The socket to be closed.
     * @throws IOException If an I/O error occurs while closing the socket.
     */
    public static void closeSocket (Socket socket) throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    /**
     * Closes an InputStreamReader and handles IOExceptions if occurs. 
     *
     * @param inputStreamReader The InputStreamReader to be closed.
     * @throws IOException If an I/O error occurs while closing the InputStreamReader.
     */
    public static void closeInputStreamReader(InputStreamReader inputStreamReader) throws IOException {
        if (inputStreamReader != null) {
            inputStreamReader.close();
        }
    }

        /**
     * Closes an OutputStreamWriter and handles IOException if occurs.
     *
     * @param outputStreamWriter The OutputStreamWriter to be closed.
     * @throws IOException If an I/O error occurs while closing the OutputStreamWriter.
     */
    public static void closeOutputStreamWriter(OutputStreamWriter outputStreamWriter) throws IOException {
        if (outputStreamWriter != null) {
            outputStreamWriter.close();
        }
    }

        /**
     * Closes a BufferedReader and handles IOException if occurs.
     *
     * @param bufferedReader The BufferedReader to be closed.
     * @throws IOException If an I/O error occurs while closing the BufferedReader.
     */
    public static void closeBufferedReader(BufferedReader bufferedReader) throws IOException {
        if (bufferedReader != null) {
            bufferedReader.close();
        }
    }

    /**
     * Closes a BufferedWriter and handles IOException if occurs.
     *
     * @param bufferedWriter The BufferedWriter to be closed.
     * @throws IOException If an I/O error occurs while closing the BufferedWriter.
     */
    public static void closeBufferedWriter(BufferedWriter bufferedWriter) throws IOException {
        if (bufferedWriter != null) {
            bufferedWriter.close();
        }
    }

}
package lib;

import java.net.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Http {

    public static int extractLamportClock(String input) {

        Pattern pattern = Pattern.compile("Lamport-Clock: (\\d+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String clockValue = matcher.group(1);
            return Integer.parseInt(clockValue);
        } else {
            return -1; 
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
                
                Http.postRequest(bufferedWriter, "", true);

                while ((response = bufferedReader.readLine()) != null) {
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

    public static void postRequest(BufferedWriter bufferedWriter, String stationId, boolean retry) {
        // Get content length
        String content = "";
        int contentLength = 0;

        String postMessage = "";

        if (retry == true) {
            content = "Retrying connection";
            contentLength = content.length();
            postMessage = Http.HttpRequest("POST", null, false, "text/plain", contentLength, content, null);
        } else {
            content = "StationId: " + stationId + " is alive";
            contentLength = content.length();
            postMessage = Http.HttpRequest("POST", null, false, "text/plain", contentLength, content, null);
        }

        System.out.println(postMessage);

        try {
            Http.write(bufferedWriter, postMessage);
        } catch (IOException e ){
            System.out.println("Error: Failed to send POST request to the server...");
        } 
    }

    public static void putRequest(BufferedWriter bufferedWriter, LamportClock clock, String body) {

        //clock.incrementClock();
        int contentLength = body.length();

        if (!body.isEmpty()) {
            body = body.substring(0, body.length() - 1);
        }

        String putMessage = Http.HttpRequest("PUT", "/content/weather.json", true, "application/json", contentLength, body, clock);

        System.out.println(putMessage);

        try {
            Http.write(bufferedWriter, putMessage);
        } catch (IOException e ){
            System.out.println("Error: Failed to send PUT request to the server...");
        } 
    }

    public static void getRequest(BufferedWriter bufferedWriter, LamportClock clock) {

        String getMessage = "";
        //clock.incrementClock();
        int clockValue = clock.getClock();
        if (clockValue != 0) {
            getMessage = "GET /filesystem/weather.json HTTP/1.1\r\n" //find correct dir
            + "Host: " + "localhost\r\n" 
            + "Lamport-Clock: " + clockValue + "\r\n\r\n";
        } else {
            getMessage = "GET /lamportClock HTTP/1.1\r\n" //find correct dir
            + "Host: " + "localhost\r\n" 
            + "Lamport-Clock: " + clockValue + "\r\n\r\n";
        }

        try {

            Http.write(bufferedWriter, getMessage);

        } catch (IOException e ){
            System.out.println("Error: Failed to send GET request the the server...");
        } 

    }

    public static String HttpRequest(String reqType, String resource, Boolean userAgent, String contentType, int contentLength, String msg, LamportClock clock) {
        
        String httpMsg = reqType + " ";

        if (resource == null) {
            httpMsg += "/ HTTP/1.1\r\n";
        } else {
            httpMsg += resource + " HTTP/1.1\r\n";
        }

        if (userAgent == true) {
            httpMsg += "User-Agent: ATOMClient/1/0\r\n";
        }

        if (clock != null) {
            httpMsg += "Lamport-Clock: " + clock.getClock() + "\r\n";
        }

        if (contentType != null) {
            httpMsg += "Content-Type: " + contentType + "\r\n";
        }

        if (contentLength == 0) {
            httpMsg += "Content-Length: " + contentLength + "\r\n\r\n";
            return httpMsg;
        } else {
            httpMsg += "Content-Length: " + contentLength + "\r\n\r\n";
        }

        if (msg != null) {
            httpMsg += msg + "\r\n\r\n";
        }

        return httpMsg;
    }

    public static String HttpResponse(int status, int contentLength) {

        String httpMsg = "HTTP/1.1 " + status + " " + getStatusMsg(status) + "\r\n";
        httpMsg += "Content-Length: " + contentLength + "\r\n\r\n";

        return httpMsg;
    }

    public static String HttpResponse(int status, LamportClock clock) {

        String httpMsg = "HTTP/1.1 " + status + " " + getStatusMsg(status) + "\r\n";
        httpMsg += "Lamport-Clock: " + clock.getClock() + "\r\n\r\n";

        return httpMsg;
    }

    public static String HttpResponse(int status) {

        String httpMsg = "HTTP/1.1 " + status + " " + getStatusMsg(status) + "\r\n\r\n";
        return httpMsg;

    }

    public static String getStatusMsg(int status) {
        
        switch (status) {

            case 200: return "OK";
            case 201: return "HTTP_CREATED";
            case 204: return "No Content";
            case 400: return "Bad Request";
            case 500: return "Internal Error";
            default:  throw new RuntimeException("Error: Invalid status number: " + status);

        }
    }

    public static String getBody(String msg) {

        if (msg.length() == 0) {
            System.out.println("Error: No content found in the request, please request again...");
            return null;
        } else if (msg.charAt(msg.length() - 1) == '0') {
            System.out.println("Error: No content found in the body of the request, please request again...");
            return null;
        }

        int contentIndex = msg.indexOf("\n\n");

        if (contentIndex == -1) {
            contentIndex = msg.indexOf("\r\n\r\n");
        }

        if (contentIndex != -1) {
            String requestBody = msg.substring(contentIndex);
            return requestBody;
        } else {
            System.out.println("Error: Content has been malformed...");
            return null;
        }
    }

    public static void write(BufferedWriter bufferedWriter, String response) throws IOException {

        if (!response.endsWith("\r\n")) {
            throw new IllegalArgumentException("Response does not end with \\r\\n");
        }

        bufferedWriter.write(response);
        bufferedWriter.flush();
    }

    public static void read(BufferedWriter bufferedWriter, BufferedReader bufferedReader) throws IOException {

        String response = "";

        while (true) {

            response = bufferedReader.readLine();
            System.out.println(response);
    
            if (response != null && response.isBlank()) {
                Http.write(bufferedWriter, "BYE\r\n");
                bufferedReader.readLine();
                break;
            }
        }
    }
}

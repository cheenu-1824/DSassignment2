package lib;

import java.io.*;

public class Http {
    
    public static void sendRequest(BufferedWriter bufferedWriter, String reqType, String body) {

        switch (reqType) {
            case "GET":
                getRequest(bufferedWriter);

        }
        
    }

    public static void getRequest(BufferedWriter bufferedWriter) {

        String getMessage = "GET /filesystem/weather.json HTTP/1.1\r\n" //find correct dir
                    + "Host: " + "localhost" + "\r\n\r\n";
        try {

            Http.write(bufferedWriter, getMessage);

        } catch (IOException e ){
            System.out.println("Error: Failed to send GET request the the server...");
        } 

    }

    public static String HttpRequest(String reqType, String resource, Boolean userAgent, String contentType, int contentLength, String msg) {
        
        String httpMsg = reqType + " ";

        if (resource == null) {
            httpMsg += "/ HTTP/1.1\r\n";
        } else {
            httpMsg += resource + " HTTP/1.1\r\n";
        }

        if (userAgent == true) {
            httpMsg += "User-Agent: ATOMClient/1/0\r\n";
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

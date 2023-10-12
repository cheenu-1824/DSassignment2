package lib;

import java.net.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Http class provides utility methods for handling HTTP requests and responses for client-server interactions.
 */
public class Http {

    /**
     * Extracts the Lamport clock value from a given string.
     *
     * @param input The input string containing the Lamport clock information.
     * @return The extracted Lamport clock value as an integer, or -1 if not found.
     */
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

    /**
     * Retries a delayed reconnection to the server with a specified number of retries.
     *
     * @param serverAddress The server's address to connect to.
     * @param port          The server's port to connect to.
     * @return A Socket instance if the connection is established, or null if retries are exhausted.
     */
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

    /**
     * Sends a POST request to the server with option to send a retry message instead.
     *
     * @param bufferedWriter The BufferedWriter for sending the request.
     * @param stationId      The station ID for the request.
     * @param retry          Indicates if this is a retry request.
     */
    public static void postRequest(BufferedWriter bufferedWriter, String stationId, boolean retry) {
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

    /**
     * Sends a PUT request to the server with a request headers and a request body.
     *
     * @param bufferedWriter The BufferedWriter for sending the request.
     * @param clock          The Lamport clock for the request.
     * @param body           The request body to be sent.
     */
    public static void putRequest(BufferedWriter bufferedWriter, LamportClock clock, String body) {

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

    /**
     * Sends a GET request to the server for the feed
     * If Lamport clock value if requester is 0, it will send a get request for the servers clock.
     *
     * @param bufferedWriter The BufferedWriter for sending the request.
     * @param clock          The Lamport clock for the request.
     */
    public static void getRequest(BufferedWriter bufferedWriter, LamportClock clock) {

        String getMessage = "";
        int clockValue = clock.getClock();
        if (clockValue != 0) {
            getMessage = "GET /filesystem/weather.json HTTP/1.1\r\n"
            + "Host: " + "localhost\r\n" 
            + "Lamport-Clock: " + clockValue + "\r\n\r\n";
        } else {
            getMessage = "GET /lamportClock HTTP/1.1\r\n"
            + "Host: " + "localhost\r\n" 
            + "Lamport-Clock: " + clockValue + "\r\n\r\n";
        }

        try {

            Http.write(bufferedWriter, getMessage);

        } catch (IOException e ){
            System.out.println("Error: Failed to send GET request the the server...");
        } 

    }

    /**
     * Generates an HTTP request message with various headers and content.
     *
     * @param reqType       The HTTP request type (e.g., "POST", "PUT", "GET").
     * @param resource      The resource path.
     * @param userAgent     Indicates if the User-Agent header should be included.
     * @param contentType   The Content-Type header value.
     * @param contentLength The Content-Length header value.
     * @param msg           The request message body.
     * @param clock         The Lamport clock for the request.
     * @return The formatted HTTP request message.
     */
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

    /**
     * Generates an HTTP response message with a specified status code and content length.
     *
     * @param status        The HTTP response status code (200, 201, 500, etc).
     * @param contentLength The Content-Length header value.
     * @return The formatted HTTP response message.
     */
    public static String HttpResponse(int status, int contentLength) {

        String httpMsg = "HTTP/1.1 " + status + " " + getStatusMsg(status) + "\r\n";
        httpMsg += "Content-Length: " + contentLength + "\r\n\r\n";

        return httpMsg;
    }

    /**
     * Generates an HTTP response message with a specified status code and a Lamport clock value.
     *
     * @param status The HTTP response status code (200).
     * @param clock  The Lamport clock for the response.
     * @return The formatted HTTP response message.
     */
    public static String HttpResponse(int status, LamportClock clock) {

        String httpMsg = "HTTP/1.1 " + status + " " + getStatusMsg(status) + "\r\n";
        httpMsg += "Lamport-Clock: " + clock.getClock() + "\r\n\r\n";

        return httpMsg;
    }

    /**
     * Generates an HTTP response message with a specified status code.
     *
     * @param status The HTTP response status code (200, 201, 500, etc).
     * @return The formatted HTTP response message.
     */
    public static String HttpResponse(int status) {

        String httpMsg = "HTTP/1.1 " + status + " " + getStatusMsg(status) + "\r\n\r\n";
        return httpMsg;

    }

    /**
     * Retrieves the status message associated with a given HTTP response status code.
     *
     * @param status The HTTP response status code.
     * @return The corresponding status message ( status 200 is "OK").
     * @throws RuntimeException If the status code is invalid.
     */
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

    /**
     * Extracts and returns the body from an HTTP request or response.
     *
     * @param msg The HTTP request or response message.
     * @return The message body, or null if not found or malformed.
     */
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
            return requestBody.trim();
        } else {
            System.out.println("Error: Content has been malformed...");
            return null;
        }
    }

    /**
     * Writes a HTTP request/response to the given BufferedWriter.
     *
     * @param bufferedWriter The BufferedWriter for writing the request/response.
     * @param response       The HTTP request/response message to write.
     * @throws IOException If an I/O error occurs while writing.
     * @throws IllegalArgumentException If the request/response does not end with '\r\n'.
     */
    public static void write(BufferedWriter bufferedWriter, String response) throws IOException {

        if (!response.endsWith("\r\n")) {
            throw new IllegalArgumentException("Response does not end with \\r\\n");
        }

        bufferedWriter.write(response);
        bufferedWriter.flush();
    }

    /**
     * Reads and prints an HTTP response from the given BufferedReader, and sends a "BYE" message to close the connection to the server.
     *
     * @param bufferedWriter The BufferedWriter for sending the "BYE" message.
     * @param bufferedReader  The BufferedReader for reading the response.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    public static void read(BufferedWriter bufferedWriter, BufferedReader bufferedReader) throws IOException {

        String response = "";

        while (true) {

            response = bufferedReader.readLine();
            System.out.println(response);
    
            if (response != null && response.trim().isEmpty()) {
                Http.write(bufferedWriter, "BYE\r\n");
                bufferedReader.readLine();
                break;
            }
        }
    }
}

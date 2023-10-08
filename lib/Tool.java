package lib;

import java.net.*;
import java.io.*;


public class Tool {

    public static String[] parseURL(String url) {
        String[] splitURL = url.split(":");
    
        if (splitURL.length == 2) {
            return splitURL;
        } else {
            throw new IllegalArgumentException("Invalid URL format: " + url + ". Correct usage: <domain>:<port>");
        }
    }

    public static void closeSocket (Socket socket) throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    public static void closeInputStreamReader(InputStreamReader inputStreamReader) throws IOException {
        if (inputStreamReader != null) {
            inputStreamReader.close();
        }
    }

    public static void closeOutputStreamWriter(OutputStreamWriter outputStreamWriter) throws IOException {
        if (outputStreamWriter != null) {
            outputStreamWriter.close();
        }
    }

    public static void closeBufferedReader(BufferedReader bufferedReader) throws IOException {
        if (bufferedReader != null) {
            bufferedReader.close();
        }
    }

    public static void closeBufferedWriter(BufferedWriter bufferedWriter) throws IOException {
        if (bufferedWriter != null) {
            bufferedWriter.close();
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

}
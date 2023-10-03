package lib;

import java.io.*;

public class Http {
    
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

    public static void write(BufferedWriter bufferedWriter, String response) throws IOException {

        if (!response.endsWith("\r\n")) {
            throw new IllegalArgumentException("Response does not end with \\r\\n");
        }

        bufferedWriter.write(response);
        bufferedWriter.flush();
    }

}

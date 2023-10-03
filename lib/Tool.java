package lib;

import java.net.*;
import java.io.*;


public class Tool {

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
}
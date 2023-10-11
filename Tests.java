import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.JsonSyntaxException;

import lib.*;

import java.util.Random;

public class Tests  {

    public static void menu() {

    
        System.out.println("<=====TestingMenu=====>");
        System.out.println("1) Unit Testing");
        System.out.println("2) Integration Testing");
        System.out.println("<=====================>");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {

                int input = scanner.nextInt();

                switch (input) {
                    case 1: 
                        UnitTestingMenu();
                        break;
                    case 2: 
                        IntegrationTestingMenu();
                        break;
                    default:
                        System.out.println("Invalid Input, try again!");
                        menu();
                        break;
                }
            }
        }
    }

    public static void UnitTestingMenu() {
        
        System.out.println("<====UnitTesting=====>");
        System.out.println("0) Back to Menu");
        System.out.println("1) ExtractStationId()");
        System.out.println("2) GetBody()");
        System.out.println("3) parseURL()");
        System.out.println("4) buildWeatherObject()");
        System.out.println("5) serializeJson()");
        System.out.println("6) deserializeJson()"); 
        System.out.println("<=====================>");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {

                int input = scanner.nextInt();

                switch (input) {
                    case 0: 
                        menu();
                        break;
                    case 1:
                        testExtractStationId();
                        UnitTestingMenu();
                        break;
                    case 2:
                        testGetBody();
                        UnitTestingMenu();
                        break;
                    case 3:
                        testParseURL();
                        UnitTestingMenu();
                        break;
                    case 4:
                        testBuildWeatherObject();
                        UnitTestingMenu();
                        break;
                    case 5:
                        testSerializeJson();
                        UnitTestingMenu();
                        break;
                    case 6:
                        testDeserializeJson();
                        UnitTestingMenu();
                        break;
                    default:
                        System.out.println("Invalid Input, try again!");
                        UnitTestingMenu();
                        break;
                }
            }
        }
    }

    public static void IntegrationTestingMenu() {
        
        System.out.println("<==IntegrationTesting==>");
        System.out.println("0) Back to Menu");
        System.out.println("1) ContentServer Put Body");
        System.out.println("2) GETClient display feed");
        System.out.println("3) AggregationServer handle Put To Get");
        System.out.println("4) End to End");
        System.out.println("<=====================>");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {

                int input = scanner.nextInt();

                switch (input) {
                    case 0: 
                        menu();
                        break;
                    case 1:
                        testContentServerPutBody();
                        IntegrationTestingMenu();
                        break;
                    case 2: 
                    testGETClientdisplayFeed();
                        IntegrationTestingMenu();
                        break;
                    case 3:
                        testAggregationServerHandlePutToGet();
                        IntegrationTestingMenu();
                        break;
                    case 4:
                        testEndtoEnd();
                        IntegrationTestingMenu();
                        break;
                    default:
                        System.out.println("Invalid Input, try again!");
                        break;
                }
            }
        }

    }

    public static void testLamportClockImplementation() {

            LamportClock serverClock = new LamportClock(0);
            LamportClock GETClientClock = new LamportClock(0);
            LamportClock contentClock1 = new LamportClock(0);
            LamportClock contentClock2 = new LamportClock(0);

            //Simluate put get put

            // make the arival of msg like put put get

            // and it should wait for get before 2nd put

            //hence i need 3 threads

            // first put




            //serverClock.updateClock(1); // Simulate the server's clock
    
            // Invoke the method
            //AggregationServer.handleClockOrder(GETClientClock.getClock(), serverClock);
    
            // Now, you can make assertions based on your expected behavior
            // For example, check if the server's clock has been updated as expected
            //assertEquals(3, clock.getClock());
    }

    public static void testEndtoEnd() {

        // CONTENT SERVER
        String content = ContentServer.readFile("content/test.txt");

        List<String> weatherData = ContentServer.splitWeatherData(content);

        weatherData = ContentServer.removeInvalidWeather(weatherData);

        System.out.println(weatherData);

        List<WeatherObject> weathers = new ArrayList<>();
        for (String weather : weatherData) {
            weathers.add(ContentServer.buildWeatherObject(weather));
        }

        System.out.println(weathers);

        String putBody = Tool.serializeJson(weathers);
        int contentLength = putBody.length();

        String putMessage = Http.HttpRequest("PUT", "/content/weather.json", true, "application/json", contentLength, putBody, null);


        // AGGREGATION SERVER
        Map<String, List<WeatherObject>> weatherDataMap = new HashMap<>();
        AggregationServer.updateWeatherValue();

        List<String> json = Tool.splitJson(Http.getBody(putMessage));
        List<WeatherObject> weatherDataAggregationServer = new ArrayList<>();

        for (String entry : json) {
            try {
                WeatherObject weather = Tool.deserializeJson(entry);
                weatherDataAggregationServer.add(weather);
                AggregationServer.addWeatherData(weather);
            } catch (JsonSyntaxException e) { 
                System.out.println("Failed to parse JSON...");
            }
        }

        AggregationServer.removeOutdatedWeather();
        AggregationServer.printWeatherMap();

        List<WeatherObject> feed = AggregationServer.getFeed();

        String body = Tool.serializeJson(feed);
        int contentLengthAggreagtionServer = body.length();

        String feedString = Http.HttpResponse(200, contentLengthAggreagtionServer);
        feedString += body + "\r\n\r\n";


        // GETClient
            String bodyGETClient = Http.getBody(feedString);

            List<String> jsonGETClient = Tool.splitJson(bodyGETClient);
    
            List<WeatherObject> weatherDataGETClient = new ArrayList<>();
            
            for (String entry : jsonGETClient) {
                weatherDataGETClient.add(Tool.deserializeJson(entry));
            }
    
            GETClient.displayWeather(weatherDataGETClient);
            System.out.println("PASSED!");

    }

    public static void testAggregationServerHandlePutToGet() {
        
        Map<String, List<WeatherObject>> weatherDataMap = new HashMap<>();

        System.out.println("CURRENT Weather Entries");
        AggregationServer.printWeatherMap();
        System.out.println();


        String putMessage = "PUT /content/weather.json HTTP/1.1\n" +
        "User-Agent: ATOMClient/1/0\n" +
        "Content-Type: application/json\n" +
        "Content-Length: 367\n" +
        "\n" +
        "{\"id\":\"IDS60901\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"state\":\"SA\",\"time_zone\":\"CST\"," +
        "\"lat\":-34.9,\"lon\":138.6,\"local_date_time\":\"12/02:00pm\",\"local_date_time_full\":\"20230712160000\"," +
        "\"air_temp\":10.3,\"apparent_t\":8.5,\"cloud\":\"Cloudy\",\"dewpt\":5.2,\"press\":1003.9," +
        "\"rel_hum\":45,\"wind_dir\":\"S\",\"wind_spd_kmh\":20,\"wind_spd_kt\":9,\"updateValue\":0,\"stationId\":919096}\n\n";

        AggregationServer.updateWeatherValue();

        List<String> json = Tool.splitJson(Http.getBody(putMessage));
        List<WeatherObject> weatherData = new ArrayList<>();

        for (String entry : json) {
            try {
                WeatherObject weather = Tool.deserializeJson(entry);
                weatherData.add(weather);
                AggregationServer.addWeatherData(weather);
            } catch (JsonSyntaxException e) { 
                System.out.println("Failed to parse JSON...");
            }
        }

        AggregationServer.removeOutdatedWeather();

        System.out.println("UPDATED Weather Entries");
        AggregationServer.printWeatherMap();

        List<WeatherObject> feed = AggregationServer.getFeed();

        String body = Tool.serializeJson(feed);

        int contentLength = body.length();

        String response = Http.HttpResponse(200, contentLength);
        response += body + "\r\n\r\n";

        String expectedResponse = "HTTP/1.1 200 OK\r\n" +
        "Content-Length: 367\r\n" +
        "\r\n" +
        "{\"id\":\"IDS60901\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"state\":\"SA\",\"time_zone\":\"CST\"," +
        "\"lat\":-34.9,\"lon\":138.6,\"local_date_time\":\"12/02:00pm\",\"local_date_time_full\":\"20230712160000\"," +
        "\"air_temp\":10.3,\"apparent_t\":8.5,\"cloud\":\"Cloudy\",\"dewpt\":5.2,\"press\":1003.9," +
        "\"rel_hum\":45,\"wind_dir\":\"S\",\"wind_spd_kmh\":20,\"wind_spd_kt\":9,\"updateValue\":0,\"stationId\":919096}\n\r\n\r\n";

        System.out.println(response);

        System.out.println(expectedResponse);

        
        if (expectedResponse.equals(response)) {
            System.out.println("PASSED!");
        } else {
            System.out.println("FAILED!");
        }

    }

    public static void testGETClientdisplayFeed() {

        String[] feedResponses = new String[2];

        feedResponses[0] = "PUT /content/weather.json HTTP/1.1\n" +
        "User-Agent: ATOMClient/1/0\n" +
        "Content-Type: application/json\n" +
        "Content-Length: 367\n" +
        "\n" +
        "{\"id\":\"IDS60901\",\"name\":\"Adelaide (West Terrace / ngayirdapira)\",\"state\":\"SA\",\"time_zone\":\"CST\"," +
        "\"lat\":-34.9,\"lon\":138.6,\"local_date_time\":\"12/02:00pm\",\"local_date_time_full\":\"20230712160000\"," +
        "\"air_temp\":10.3,\"apparent_t\":8.5,\"cloud\":\"Cloudy\",\"dewpt\":5.2,\"press\":1003.9," +
        "\"rel_hum\":45,\"wind_dir\":\"S\",\"wind_spd_kmh\":20,\"wind_spd_kt\":9,\"updateValue\":0,\"stationId\":375358}";

        feedResponses[1] = "PUT /content/weather.json HTTP/1.1\n" +
        "User-Agent: ATOMClient/1/0\n" +
        "Content-Type: application/json\n" +
        "Content-Length: 728\n" +
        "\n" +
        "{\"id\":\"IDS60901\",\"name\":\"Adelaide (West Terrace / ngayirdapira)\",\"state\":\"SA\",\"time_zone\":\"CST\"," +
        "\"lat\":-34.9,\"lon\":138.6,\"local_date_time\":\"15/04:00pm\",\"local_date_time_full\":\"20230715160000\"," +
        "\"air_temp\":13.3,\"apparent_t\":9.5,\"cloud\":\"Partly cloudy\",\"dewpt\":5.7,\"press\":1023.9," +
        "\"rel_hum\":60,\"wind_dir\":\"S\",\"wind_spd_kmh\":15,\"wind_spd_kt\":8,\"updateValue\":0,\"stationId\":431021}\n" +
        "{\"id\":\"IDS12345\",\"name\":\"New York City\",\"state\":\"NY\",\"time_zone\":\"EST\",\"lat\":40.7128," +
        "\"lon\":-74.006,\"local_date_time\":\"15/04:30pm\",\"local_date_time_full\":\"20230715163000\"," +
        "\"air_temp\":21.5,\"apparent_t\":19.8,\"cloud\":\"Partly cloudy\",\"dewpt\":15.2,\"press\":1015.7," +
        "\"rel_hum\":55,\"wind_dir\":\"W\",\"wind_spd_kmh\":10,\"wind_spd_kt\":5,\"updateValue\":0,\"stationId\":431021}";

        String[] expectedOutput = new String[2];

        expectedOutput[0] = "\n\n<=====WEATHER FEED=====>\n" + "\n" +
        "ID: IDS60901\n" + "Name: Adelaide (West Terrace / ngayirdapira)\n" + "State: SA\n" + "Time Zone: CST\n" +
        "Latitude: -34.900001525878906\n" + "Longitude: 138.60000610351562\n" + "Local Date Time: 12/02\n" +
        "Local Date Time Full: 20230712160000\n" + "Air Temperature: 10.300000190734863\n" + "Apparent Temperature: 8.5\n" +
        "Cloud: Cloudy\n" + "Dew Point: 5.199999809265137\n" + "Pressure: 1003.9000244140625\n" + "Relative Humidity: 45\n" +
        "Wind Direction: S\n" + "Wind Speed (km/h): 20\n" + "Wind Speed (kt): 9\n" + "\n" + "<======================>";

        expectedOutput[1] = "\n\n<=====WEATHER FEED=====>\n" + "\n" + "ID: IDS60901\n" + "Name: Adelaide (West Terrace / ngayirdapira)\n" +
        "State: SA\n" + "Time Zone: CST\n" + "Latitude: -34.9\n" + "Longitude: 138.6\n" + "Local Date Time: 15/04:00pm\n" +
        "Local Date Time Full: 20230715160000\n" +  "Air Temperature: 13.3\n" + "Apparent Temperature: 9.5\n" + "Cloud: Partly cloudy\n" +
        "Dew Point: 5.7\n" + "Pressure: 1023.9\n" + "Relative Humidity: 60\n" + "Wind Direction: S\n" + "Wind Speed (km/h): 15\n" + "Wind Speed (kt): 8\n" +
        "\n" + "<======================>\n" + "\n" + "ID: IDS12345\n" + "Name: New York City\n" + "State: NY\n" + "Time Zone: EST\n" +
        "Latitude: 40.7128\n" + "Longitude: -74.006\n" + "Local Date Time: 15/04:30pm\n" + "Local Date Time Full: 20230715163000\n" +
        "Air Temperature: 21.5\n" + "Apparent Temperature: 19.8\n" + "Cloud: Partly cloudy\n" + "Dew Point: 15.2\n" + "Pressure: 1015.7\n" +
        "Relative Humidity: 55\n" + "Wind Direction: W\n" + "Wind Speed (km/h): 10\n" + "Wind Speed (kt): 5\n" + "\n" + "<======================>";

        int i = 0;
        for (String feed : feedResponses) {
            String body = Http.getBody(feed);

            List<String> json = Tool.splitJson(body);
    
            List<WeatherObject> weatherData = new ArrayList<>();
            
            for (String entry : json) {
                weatherData.add(Tool.deserializeJson(entry));
            }
    
            System.out.println("Expected output:");
            System.out.println(expectedOutput[i]);
            System.out.println("Output:");
            GETClient.displayWeather(weatherData);

            i += 1;
        }

        System.out.println("COMPARE EXPECTED vs. OUTPUT");

    }

    public static void testContentServerPutBody() {
        
        String content = ContentServer.readFile("content/test.txt");

        List<String> weatherData = ContentServer.splitWeatherData(content);

        weatherData = ContentServer.removeInvalidWeather(weatherData);

        List<WeatherObject> weathers = new ArrayList<>();
        for (String weather : weatherData) {
            weathers.add(ContentServer.buildWeatherObject(weather));
        }

        String outputBody = Tool.serializeJson(weathers);


        System.out.println(outputBody);

        String expectedBody = "{" +
        "\"id\":\"IDS60901\"," +
        "\"name\":\"Adelaide (West Terrace /  ngayirdapira)\"," +
        "\"state\":\"SA\"," +
        "\"time_zone\":\"CST\"," +
        "\"lat\":-34.9," +
        "\"lon\":138.6," +
        "\"local_date_time\":\"12/02:00pm\"," +
        "\"local_date_time_full\":\"20230712160000\"," +
        "\"air_temp\":10.3," +
        "\"apparent_t\":8.5," +
        "\"cloud\":\"Cloudy\"," +
        "\"dewpt\":5.2," +
        "\"press\":1003.9," +
        "\"rel_hum\":45," +
        "\"wind_dir\":\"S\"," +
        "\"wind_spd_kmh\":20," +
        "\"wind_spd_kt\":9," +
        "\"updateValue\":0," +
        "\"stationId\":-1" +
        "}\n";

        System.out.println(expectedBody);

    
        if (outputBody.equals(expectedBody)) {
            System.out.println("PASSED!");
        } else {
            System.out.println("FAILED!");
        }
    }

    public static void testExtractStationId() {

        Random random = new Random();
        int numPassed = 0;
        for (int i = 0; i < 100; i++) {
            int randomStationId = random.nextInt(1000000);
            String content = "StationId: " + randomStationId + " is alive\r\n";
            int extractStationId = AggregationServer.extractStationId(content);
            if (randomStationId == extractStationId) {
                System.out.println("Test " + i + ": PASSED!");
                numPassed += 1;
            } else {
                System.out.println("Test " + i + ": FAILED! Expected output: " + randomStationId + ". Output: " + extractStationId);
            }
            System.out.println("Number of tests passed: " + numPassed + " of 100");
        }
    }

    public static void testGetBody() {

        // NEED TO MAKE TO SHARED TEST BETWEEN GETCLIENT AND AGGREGATIONSERVER
        int numPassed = 0;
        String[] responses = new String[6];
        String[] expectedBodies = new String[6];
        responses[0] = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 15\r\n\r\n{\"key\": \"value\"}\r\n";
        expectedBodies[0] = "{\"key\": \"value\"}";

        responses[1] = "HTTP/1.1 200 OK\r\n\r\n";
        expectedBodies[1] = null;


        responses[2] = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 0\r\n\r\n";
        expectedBodies[2] = null;

        responses[3] = "POST / HTTP/1.1\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: text/plain\r\nContent-Length: 19\r\n\r\nRetrying Connection\r\n";
        expectedBodies[3] = "Retrying Connection";

        responses[4] = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 107\r\n\r\n"
        + "{\"id\":\"IDS60901\",\"name\":\"Adelaide (West Terrace /  ngayirdapira\",\"state\":\"SA\",\"time_zone\":\"CST\",\"lat\":-34.9,\"lon\":138.6,\"local_date_time\":\"15/04:00pm\",\"local_date_time_full\":\"20230715160000\",\"air_temp\":13.3,\"apparent_t\":9.5,\"cloud\":\"Partly cloudy\",\"dewpt\":5.7,\"press\":1023.9,\"rel_hum\":60,\"wind_dir\":\"S\",\"wind_spd_kmh\":15,\"wind_spd_kt\":8}\r\n";
        expectedBodies[4] = "{\"id\":\"IDS60901\",\"name\":\"Adelaide (West Terrace /  ngayirdapira\",\"state\":\"SA\",\"time_zone\":\"CST\",\"lat\":-34.9,\"lon\":138.6,\"local_date_time\":\"15/04:00pm\",\"local_date_time_full\":\"20230715160000\",\"air_temp\":13.3,\"apparent_t\":9.5,\"cloud\":\"Partly cloudy\",\"dewpt\":5.7,\"press\":1023.9,\"rel_hum\":60,\"wind_dir\":\"S\",\"wind_spd_kmh\":15,\"wind_spd_kt\":8}";

        responses[5] = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 110\r\n\r\n"
        + "{\"id\":\"IDS12345\",\"name\":\"New York City\",\"state\":\"NY\",\"time_zone\":\"EST\",\"lat\":40.7128,\"lon\":-74.0060,\"local_date_time\":\"15/04:30pm\",\"local_date_time_full\":\"20230715163000\",\"air_temp\":21.5,\"apparent_t\":19.8,\"cloud\":\"Partly cloudy\",\"dewpt\":15.2,\"press\":1015.7,\"rel_hum\":55,\"wind_dir\":\"W\",\"wind_spd_kmh\":10,\"wind_spd_kt\":5}\r\n"
        + "{\"id\":\"IDS60901\",\"name\":\"Adelaide (West Terrace /  ngayirdapira\",\"state\":\"SA\",\"time_zone\":\"CST\",\"lat\":-34.9,\"lon\":138.6,\"local_date_time\":\"15/04:00pm\",\"local_date_time_full\":\"20230715160000\",\"air_temp\":13.3,\"apparent_t\":9.5,\"cloud\":\"Partly cloudy\",\"dewpt\":5.7,\"press\":1023.9,\"rel_hum\":60,\"wind_dir\":\"S\",\"wind_spd_kmh\":15,\"wind_spd_kt\":8}\r\n";

        expectedBodies[5] = "{\"id\":\"IDS12345\",\"name\":\"New York City\",\"state\":\"NY\",\"time_zone\":\"EST\",\"lat\":40.7128,\"lon\":-74.0060,\"local_date_time\":\"15/04:30pm\",\"local_date_time_full\":\"20230715163000\",\"air_temp\":21.5,\"apparent_t\":19.8,\"cloud\":\"Partly cloudy\",\"dewpt\":15.2,\"press\":1015.7,\"rel_hum\":55,\"wind_dir\":\"W\",\"wind_spd_kmh\":10,\"wind_spd_kt\":5}\n"
        + "{\"id\":\"IDS60901\",\"name\":\"Adelaide (West Terrace /  ngayirdapira\",\"state\":\"SA\",\"time_zone\":\"CST\",\"lat\":-34.9,\"lon\":138.6,\"local_date_time\":\"15/04:00pm\",\"local_date_time_full\":\"20230715160000\",\"air_temp\":13.3,\"apparent_t\":9.5,\"cloud\":\"Partly cloudy\",\"dewpt\":5.7,\"press\":1023.9,\"rel_hum\":60,\"wind_dir\":\"S\",\"wind_spd_kmh\":15,\"wind_spd_kt\":8}";


        for (int i = 0; i < 6; i++) {
            String body = Http.getBody(responses[i]);

            if (body == expectedBodies[i]) {
                System.out.println("Test " + i + ": PASSED!");
                numPassed += 1;
            } else {
                System.out.println("Test " + i + ": FAILED! Expected output: " + expectedBodies[i] + ". Output: " + body);
            }
        }

        System.out.println("Number of tests passed: " + numPassed + " of 6");

    }

    public static void testParseURL() {
        
        // NEED TO MAKE TO SHARED TEST BETWEEN GETCLIENT AND CONTENT
        int numPassed = 0;
        String[] urls = new String[5];
        String[] expectedDomain = new String[5];
        String[] expectedPort = new String[5];
        urls[0] = "localhost:4567";
        expectedDomain[0] = "localhost";
        expectedPort[0] = "4567";

        urls[1] = "example.com:8080";
        expectedDomain[1] = "example.com";
        expectedPort[1] = "8080";
        
        urls[2] = "google.com:80";
        expectedDomain[2] = "google.com";
        expectedPort[2] = "80";

        urls[3] = "weather.com.au:12000";
        expectedDomain[3] = "weather.com.au";
        expectedPort[3] = "12000";

        urls[4] = "localhost:453";
        expectedDomain[4] = "localhost";
        expectedPort[4] = "453";

        for (int i = 0; i < 5; i++) {
            String[] result = Tool.parseURL(urls[i]);

            if (result[0].equals(expectedDomain[i]) == true && result[1].equals(expectedPort[i]) == true) {
                System.out.println("Test " + i + ": PASSED!");
                numPassed += 1;
            } else {
                System.out.println("Test " + i + ": FAILED! Expected domain: " + expectedDomain[i] + " and port: " + expectedPort + ". Output domain: " + result[0] + " and output port: " + result[1]);
            }
        }
        System.out.println("Number of tests passed: " + numPassed + " of 5");
    }
    
    public static void testBuildWeatherObject() {
            
        int numPassed = 0;
        String input = "id:IDS12345\nname:New York City\nstate:NY\n" +
                        "time_zone:EST\nlat:40.7128\nlon:-74.0060\n" +
                        "local_date_time:2/04:30pm\nlocal_date_time_full:20230702163000\n" +
                        "air_temp:19.5\napparent_t:17.8\ncloud:Sunny\n" +
                        "dewpt:12.2\npress:1005.7\nrel_hum:50\n" +
                        "wind_dir:W\nwind_spd_kmh:15\nwind_spd_kt:8";

        WeatherObject result = ContentServer.buildWeatherObject(input);

        if ("IDS12345".equals(result.getId())) {
            System.out.println("Correct Id field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Id field!");
        }
        
        if ("New York City".equals(result.getName())) {
            System.out.println("Correct Name field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Name field!");
        }
        
        if ("NY".equals(result.getState())) {
            System.out.println("Correct State field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect State field!");
        }
        
        if ("EST".equals(result.getTime_zone())) {
            System.out.println("Correct Time Zone field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Time Zone field!");
        }
        
        if (40.7128 == result.getLat()) {
            System.out.println("Correct Latitude field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Latitude field!");
        }
        
        if (-74.006 == result.getLon()) {
            System.out.println("Correct Longitude field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Longitude field!");
        }
        
        if ("2/04:30pm".equals(result.getLocal_date_time())) {
            System.out.println("Correct Local Date Time field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Local Date Time field!");
        }
        
        if ("20230702163000".equals(result.getLocal_date_time_full())) {
            System.out.println("Correct Local Date Time Full field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Local Date Time Full field!");
        }
        
        if (19.5 == result.getAir_temp()) {
            System.out.println("Correct Air Temperature field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Air Temperature field!");
        }
        
        if (17.8 == result.getApparent_t()) {
            System.out.println("Correct Apparent Temperature field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Apparent Temperature field!");
        }
        
        if ("Sunny".equals(result.getCloud())) {
            System.out.println("Correct Cloud field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Cloud field!");
        }
        
        if (12.2 == result.getDewpt()) {
            System.out.println("Correct Dew Point field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Dew Point field!");
        }
        
        if (1005.7 == result.getPress()) {
            System.out.println("Correct Pressure field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Pressure field!");
        }
        
        if (50 == result.getRel_hum()) {
            System.out.println("Correct Relative Humidity field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Relative Humidity field!");
        }
        
        if ("W".equals(result.getWind_dir())) {
            System.out.println("Correct Wind Direction field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Wind Direction field!");
        }
        
        if (15 == result.getWind_spd_kmh()) {
            System.out.println("Correct Wind Speed (km/h) field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Wind Speed (km/h) field!");
        }
        
        if (8 == result.getWind_spd_kt()) {
            System.out.println("Correct Wind Speed (kt) field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Wind Speed (kt) field!");
        }

        System.out.println(numPassed + " of 17 field correct!");
    }

    public static void testSerializeJson() {
        
        List<WeatherObject> weatherList = new ArrayList<>();
        WeatherObject weather1 = new WeatherObject();
        weather1.setId("IDS60901");
        weather1.setName("Adelaide (West Terrace / ngayirdapira)");
        weather1.setState("SA");
        weather1.setTime_zone("CST");
        weather1.setLat(-34.9);
        weather1.setLon(138.6);
        weather1.setLocal_date_time("12/02:00pm");
        weather1.setLocal_date_time_full("20230712160000");
        weather1.setAir_temp(10.3);
        weather1.setApparent_t(8.5);
        weather1.setCloud("Cloudy");
        weather1.setDewpt(5.2);
        weather1.setPress(1003.9);
        weather1.setRel_hum(45);
        weather1.setWind_dir("S");
        weather1.setWind_spd_kmh(20);
        weather1.setWind_spd_kt(9);

        WeatherObject weather2 = new WeatherObject();
        weather2.setId("IDS12345");
        weather2.setName("New York City");
        weather2.setState("NY");
        weather2.setTime_zone("EST");
        weather2.setLat(40.7128);
        weather2.setLon(-74.0060);
        weather2.setLocal_date_time("2/04:30pm");
        weather2.setLocal_date_time_full("20230702163000");
        weather2.setAir_temp(19.5);
        weather2.setApparent_t(17.8);
        weather2.setCloud("Sunny");
        weather2.setDewpt(12.2);
        weather2.setPress(1005.7);
        weather2.setRel_hum(50);
        weather2.setWind_dir("W");
        weather2.setWind_spd_kmh(15);
        weather2.setWind_spd_kt(8);

        weatherList.add(weather1);
        weatherList.add(weather2);

        String expectedJson = "{" +
        "\"id\":\"IDS60901\"," +
        "\"name\":\"Adelaide (West Terrace / ngayirdapira)\"," +
        "\"state\":\"SA\"," +
        "\"time_zone\":\"CST\"," +
        "\"lat\":-34.9," +
        "\"lon\":138.6," +
        "\"local_date_time\":\"12/02:00pm\"," +
        "\"local_date_time_full\":\"20230712160000\"," +
        "\"air_temp\":10.3," +
        "\"apparent_t\":8.5," +
        "\"cloud\":\"Cloudy\"," +
        "\"dewpt\":5.2," +
        "\"press\":1003.9," +
        "\"rel_hum\":45," +
        "\"wind_dir\":\"S\"," +
        "\"wind_spd_kmh\":20," +
        "\"wind_spd_kt\":9," +
        "\"updateValue\":0," +
        "\"stationId\":-1" +
        "}" + "\n" +
        "{" +
        "\"id\":\"IDS12345\"," +
        "\"name\":\"New York City\"," +
        "\"state\":\"NY\"," +
        "\"time_zone\":\"EST\"," +
        "\"lat\":40.7128," +
        "\"lon\":-74.006," +
        "\"local_date_time\":\"2/04:30pm\"," +
        "\"local_date_time_full\":\"20230702163000\"," +
        "\"air_temp\":19.5," +
        "\"apparent_t\":17.8," +
        "\"cloud\":\"Sunny\"," +
        "\"dewpt\":12.2," +
        "\"press\":1005.7," +
        "\"rel_hum\":50," +
        "\"wind_dir\":\"W\"," +
        "\"wind_spd_kmh\":15," +
        "\"wind_spd_kt\":8," +
        "\"updateValue\":0," +
        "\"stationId\":-1" +
        "}\n";

        String outputJson = Tool.serializeJson(weatherList);

        if (outputJson.equals(expectedJson)) {
            System.out.println("PASSED!");
        } else {
            System.out.println("FAILED!");
        }

    }

    public static void testDeserializeJson() {
        
        int numPassed = 0;

        String jsonString = "{" +
        "\"id\":\"IDS12345\"," +
        "\"name\":\"New York City\"," +
        "\"state\":\"NY\"," +
        "\"time_zone\":\"EST\"," +
        "\"lat\":40.7128," +
        "\"lon\":-74.006," +
        "\"local_date_time\":\"2/04:30pm\"," +
        "\"local_date_time_full\":\"20230702163000\"," +
        "\"air_temp\":19.5," +
        "\"apparent_t\":17.8," +
        "\"cloud\":\"Sunny\"," +
        "\"dewpt\":12.2," +
        "\"press\":1005.7," +
        "\"rel_hum\":50," +
        "\"wind_dir\":\"W\"," +
        "\"wind_spd_kmh\":15," +
        "\"wind_spd_kt\":8," +
        "\"updateValue\":0," +
        "\"stationId\":-1" +
        "}\n";

        WeatherObject result = Tool.deserializeJson(jsonString);

        if ("IDS12345".equals(result.getId())) {
            System.out.println("Correct Id field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Id field!");
        }
        
        if ("New York City".equals(result.getName())) {
            System.out.println("Correct Name field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Name field!");
        }
        
        if ("NY".equals(result.getState())) {
            System.out.println("Correct State field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect State field!");
        }
        
        if ("EST".equals(result.getTime_zone())) {
            System.out.println("Correct Time Zone field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Time Zone field!");
        }
        
        if (40.7128 == result.getLat()) {
            System.out.println("Correct Latitude field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Latitude field!");
        }
        
        if (-74.006 == result.getLon()) {
            System.out.println("Correct Longitude field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Longitude field!");
        }
        
        if ("2/04:30pm".equals(result.getLocal_date_time())) {
            System.out.println("Correct Local Date Time field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Local Date Time field!");
        }
        
        if ("20230702163000".equals(result.getLocal_date_time_full())) {
            System.out.println("Correct Local Date Time Full field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Local Date Time Full field!");
        }
        
        if (19.5 == result.getAir_temp()) {
            System.out.println("Correct Air Temperature field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Air Temperature field!");
        }
        
        if (17.8 == result.getApparent_t()) {
            System.out.println("Correct Apparent Temperature field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Apparent Temperature field!");
        }
        
        if ("Sunny".equals(result.getCloud())) {
            System.out.println("Correct Cloud field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Cloud field!");
        }
        
        if (12.2 == result.getDewpt()) {
            System.out.println("Correct Dew Point field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Dew Point field!");
        }
        
        if (1005.7 == result.getPress()) {
            System.out.println("Correct Pressure field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Pressure field!");
        }
        
        if (50 == result.getRel_hum()) {
            System.out.println("Correct Relative Humidity field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Relative Humidity field!");
        }
        
        if ("W".equals(result.getWind_dir())) {
            System.out.println("Correct Wind Direction field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Wind Direction field!");
        }
        
        if (15 == result.getWind_spd_kmh()) {
            System.out.println("Correct Wind Speed (km/h) field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Wind Speed (km/h) field!");
        }
        
        if (8 == result.getWind_spd_kt()) {
            System.out.println("Correct Wind Speed (kt) field!");
            numPassed += 1;
        } else {
            System.out.println("Incorrect Wind Speed (kt) field!");
        }

        System.out.println(numPassed + " of 17 field correct!");

    }
    public static void main(String[] args) {

        menu();
        
    }

}
# Assignment 2: Weather Feed Service 

This README file describes the architecture of the system and provides steps to run the program as intended.

## How to Run the Weather Feed Service

### Initial Commands for All Cases
- Make sure to run the command: `make` to build the required files for running the client and server.
- You should see the following output: `javac -cp "lib/*:./" AggregationServer.java ClientThread.java ContentServer.java GETClient.java Tests.java`, which indicates that the code has been successfully compiled. 
- `java -cp './lib/*:./' AggregationServer <port>` starts an Aggregation Server on the specified port. *specifiying port is not necessary*
- `java -cp './lib/*:./' ContentServer <domain>:<port> <file location> <stationId>` Starts a Content Server to connect to given url, use weather data in content folder to upload to server and optionally add a stationId to simulate a crashed station re connection to server.
- `java -cp './lib/*:./' GETClient http://<domain>:<port>` GETClient connects to url to retrive weather feed.
- You should use the URL `http://localhost:4567` for testing!

### How to play with a simple GETClient-Aggregation Server interaction with a weather entries from a single Content Server
1. Open three terminals in the same main directory to simulate three different virtual machines (VMs).
2. In the first terminal, launch the Aggregation Server by running the command: `java -cp './lib/*:./' AggregationServer <port>`. *Adding a port number is not required. This will run indefinitely.*
3. In the second terminal, create a Content Server to PUT a weather entry from any of the ones in the content directory by executing the command: `java -cp './lib/*:./' ContentServer <domain>:<port> <file location> <stationId>`. *Choose a test file for different weather entries in the content directory. For example, use `/content/test.txt`.*
3. In the third terminal, create a GET Client to GET a weather feed from the Aggregation Server by executing the command: `java -cp './lib/*:./' GETClient http://<domain>:<port>`. *Default domain and port is localhost:4567*
4. In the GET Cient's terminal, you will see the most up to date weather feed from the Aggregation Server.
5. In the Aggregation server's terminal, you will see logs of all connections and actions taken place.
5. In the Content Server's terminal, you will see if content sent was sucessful and logs for every time a heartbeat is sent.

## How to Run Tests
- *NOTE*
- All outputs from automatic shell script tests will go to tests/outputs/<testname>.
- All expected ouputs for the respective test will go to tests/outputs/<testname>.
- You may need to run the command `chmod +x <shell-filename>` to give permission to run the bash script.
- Automatic scripts will show wether each case has passed or failed. 
- Automatic scripts will print `WHOLE TEST PASSED` if test was sucessful, `WHOLE TEST FAILED` if not.

### How to run automatic Unit/Integration tests
1. In a terminal, run the command: `java -cp './lib/*:./' Tests`. This will start up the testing menu for these test.
2. Use the numbers show beside to choose which option on the menu. *Note: one test requires manual testing through looking at the console*

### How to run automatic Parallel clients requesting the Weather Feed Service
- To run parallel GETClients requesting to the Aggregation Server, run the script: `./runClientsX.sh`. *Where X is the desired number of GETClients to run simultaneously*
What the Script is doing!
1. Runs the Aggregation Server and a single Content Server in seperate background processes.
2. Runs multiple GETClient request concurrenlty to test the synchronisation of shared data in the Aggregation Server.
3. Compares result with expected response excluding Lamport Clock value as it can be different each time the test is run.
4. If the test passes, the expected output, `WHOLE TEST PASSED` will be printed, otherwise `WHOLE TEST FAILED` will be printed.

### How to run automatic Aggregation Server crashing and restarting test
- To simulate an Aggregation Server crashing, run the script: `./runAggregationCrashTest.sh`.
What the Script is doing!
1. Runs an Aggregation Server, Content Server and GETClients to show it gets the correct intial feed and waits to store the weather data.
2. Then the Aggregation Server crashes and a GETClient requests for a weather feed during this period but recieves no connection.
3. Then the Aggregation Server restarts and a GETClient requests for a weather feed from the save weather from Aggregation Server.
4. If the test passes, the expected output, `WHOLE TEST PASSED` will be printed, otherwise `WHOLE TEST FAILED` will be printed.


### How to run automatic Content Server crashing and restarting test
- To simulate a Content Server crashing, run the script: `./runContentCrashTest.sh`.
What the Script is doing!
1. Runs an Aggregation Server, Content Server and GETClients to show it gets the correct intial feed.
2. Then the Content Server crashes and a GETClient requests for a weather feed during this short period to ensure AggregationServer has not removed it yet.
3. Then the Content Server restarts and a GETClient requests for a weather feed from the save weather from Aggregation Server.
2. Then the Content Server crashes and a GETClient requests for a weather feed(should return no content) but waits a longer period to check if weather from dead Content Servers get removed from the Aggregation Server.
4. If the test passes, the expected output, `WHOLE TEST PASSED` will be printed, otherwise `WHOLE TEST FAILED` will be printed.

### How to run automatic checking if weather data gets updated test
- To run this test, run the script: `./runUpdateWeatherTest.sh`.
What the Script is doing!
1. Runs an Aggregation Server, Content Server and GETClients to show it gets the correct intial feed.
2. New Content Server is started and puts updated weather data of the same weather id.
3. Another GETClient request for the weather feed and is used to check if it retrieved the newer updated weather data from the new Content Server.
4. If the test passes, the expected output, `WHOLE TEST PASSED` will be printed, otherwise `WHOLE TEST FAILED` will be printed.

### How to test checking for invalid weather entries
1. Open two terminals in the same main directory to simulate two different virtual machines (VMs).
2. Run the Aggregation Server and run a ContentServer with one of the `testInvalidX.txt` files. *X is a number from 1-3* 
3. To test if the Content Server correctly handles invalid weather entries, the Content Server should not send these weather entries to the Aggergation Server. Instead, it should exit gracefully with the error `Error: No valid weather data found from the input file...`.
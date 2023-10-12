# Assignment 2: Weather Feed Service 

This README file describes the architecture of the system and provides steps to run the program as intended.

## Calculator Server Architecture


## How to Run the Weather Feed Service

### Initial Commands for All Cases
- Make sure to run the command: `make` to build the required files for running the client and server.
- You should see the following output: "javac -cp "lib/gson-2.10.1.jar" AggregationServer.java ClientThread.java ContentServer.java GETClient.java LamportClock.java WeatherObject.java", which indicates that the code has been successfully compiled.

### How to test a simple GETClient-Aggregation Server interaction with a weather entries from a single Content Server
1. Open three terminals in the same main directory to simulate three different virtual machines (VMs).
2. In the first terminal, launch the Aggregation Server by running the command: `java -cp './lib/*:./' AggregationServer <port>`. *Adding a port number is not required. This will run indefinitely.*
3. In the second terminal, create a Content Server to PUT a weather entry from any of the ones in the content directory by executing the command: `java -cp './lib/*:./' ContentServer <domain>:<port> <file location> <stationId (Not Required)>`. *Choose a test file for different weather entries in the content directory. For example, use `/content/test.txt`.*
3. In the third terminal, create a GET Client to GET a weather feed from the Aggregation Server by executing the command: `java -cp './lib/*:./' GETClient http://<domain>:<port>`. *Default domain and port is localhost:4567*
4. In the GET Cient's terminal, you will see the most up to date weather feed from the Aggregation Server.
5. In the Aggregation server's terminal, you will see logs of all connections and actions taken place.
5. In the Content Server's terminal, you will see if content sent was sucessful and logs for every time a heartbeat is sent.

MAKE THESE AUTOMATIC TESTS NOT TOO HARD ACTUALLY LOL

### How to simulate an Aggreagtion Server crashing and restarting
1. To simulate an Aggregation Server crashing and restarting, simply end the Aggregation Server process by the shortcut `Crtl + C`.
2. Then simply run the command to run an Aggregation Server again with the same port number.

### How to simulate an Content Server crashing and restarting
1. To simulate an Content Server crashing and restarting, simply end the Content Server process by the shortcut `Crtl + C`.
2. Then run the command: `java -cp './lib/*:./' ContentServer <domain>:<port> <file location> <stationId>` where the field stationId is the stationId of the content server that has been shutdown.

## How to Run Tests
- *NOTE*
- All outputs from automatic shell script tests will go to tests/outputs/<testname>.
- All expected ouputs for the respective test will go to tests/outputs/<testname>.
- You may need to run the command `chmod +x <shell-filename>` to give permission to run the bash script.

### How to run automatic Unit/Integration tests
1. In a terminal, run the command: `java -cp './lib/*:./' Tests`. This will start up the testing menu for these test.
2. Use the numbers show beside to choose which option on the menu. *Note: one test requires manual testing through looking at the console*

### How to run automatic Parallel clients requesting the Weather Feed Service
1. To run parallel GETClients requesting to the Aggregation Server, run the script: `./runClientsX.sh`. *Where X is the desired number of GETClients to run simultaneously*
2. Note: You may need to run the command `chmod +x <shell-filename>` to give permission to run the bash script.

What the Script is doing!
1. Runs the Aggregation Server and a single Content Server in seperate background processes.
2. Then runs parallel GETClients as background processes concurrently. 
3. Performs a check to see if all GETClients sucessfully retrives weather feed which conform to the expected output.
4. If it equals expected output, `TEST PASSED` will be printed, otherwise `TEST FAILED` will be printed.

### How to run automatic Aggreagtion Server crashing and restarting test
1. To simulate an Aggregation Server crashing and restarting, simply end the Aggregation Server process by the shortcut `Crtl + C`.
2. Then simply run the command to run an Aggregation Server again with the same port number.

### How to run automatic Content Server crashing and restarting test
1. To simulate an Content Server crashing and restarting, simply end the Content Server process by the shortcut `Crtl + C`.
2. Then run the command: `java -cp './lib/*:./' ContentServer <domain>:<port> <file location> <stationId>` where the field stationId is the stationId of the content server that has been shutdown.


### How to run stress tests



### How to test checking for invalid weather entries *SINGLE INVALID entry, double invalid and then single no id: empty*
1. Open two terminals in the same main directory to simulate two different virtual machines (VMs).
2. Run the Aggregation Server and run a ContentServer with one of the `testInvalidX.txt` files. *X is a number from 1-3* 
3. To test if the Content Server correctly handles invalid weather entries, the Content Server should not send these weather entries to the Aggergation Server. Instead, it should exit gracefully with the error `Error: No valid weather data found from the input file...`.

### How to test the heartbeat of the Content Server
1. Run a simple GETClient-Aggregation Server interaction stated above.
2. Both, console of Content Server and logs of Aggregation Server will show when a heartbeat is sent and received, hence, visually check if both are occuring.
3. Check timestamp on logs on Aggregation Server and weather.json file to see if to check if the heartbeat is sent within the 30 second window to ensure that weather entries are not removed for no reason.


### How to perform a test of storing correctly updated and alive weather entries on the Aggregation Server
1. Open four terminals in the same main directory to simulate different virtual machines (VMs).
2. Run the Aggregation Server and run a ContentServer with any test file (unchanged version).
3. After some time (around 15 seconds), begin another ContentServer with the changed version of the already running test file.
4. Stop the intial Content Server by the shortcut: `Crtl + C` and observe (arround 30 seconds) the weather.json file and logs on of the Aggregation Server. *You should see the new values enter on the weather.json file and Logs indicating that outdated weather entries have been removed. When this happens, the new values should only be left in the weather.json file*



### How to perform a stress test on the Aggregation Server
1. Open ten terminals in the same main directory to simulate different virtual machines (VMs).
2. Change the maxClients value in the main() of AggregationServer.java and run() of ClientThread.java to 1.
3. Run the Aggregation Server and run 8 different Content Servers with different test files.
4. Attempt to run a GETClient onces or multiple times to check if the server is still stable and able to still handle requests.
5. Instead of repeatedily running a single GETClient, you may also test this with multiple simultaneous clients using any parallel clients driver.
4. To specifically test the ability for ContentServer to retry connection with the server, reduce the maxClients value in the main() of AggregationServer.java and run() of ClientThread.java to 1.








How to compile:

make

How to run Content Server:

java -cp './lib/*:./' ContentServer localhost:4567 content/test.txt

How to run Aggregation Server:

java -cp './lib/*:./' AggregationServer

How to run GET Client:

java -cp './lib/*:./' GETClient localhost:4567

How Lamport clock will be employed:
Will be implemented by creating a class to manage timestamps. 
Will use lamport clock to synchronise the requests by using the timestamps sent through the request.

How testing will be employed:

Unit testing:
- Test function using console/can make automated by creating test functions.

Integration testing:
- Create a test function which creates intances of different classes and communicates information through the system and compares with expected output.

Stress Testing:
- use a shell script to run multiple clients simuletanously

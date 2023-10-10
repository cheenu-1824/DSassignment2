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
3. In the second terminal, create a Content Server to PUT a weather entry from any of the ones in the content directory by executing the command: `java -cp './lib/*:./' ContentServer <domain>:<port> <file location>`. *Choose a test file for different weather entries in the content directory. For example, use `/content/test.txt`.*
3. In the third terminal, create a GET Client to GET a weather feed from the Aggregation Server by executing the command: `java -cp './lib/*:./' GETClient http://<domain>:<port>`. *Default domain and port is localhost:4567*
4. In the GET Cient's terminal, you will see the most up to date weather feed from the Aggregation Server.
5. In the Aggregation server's terminal, you will see logs of all connections and actions taken place.
5. In the Content Server's terminal, you will see if content sent was sucessful and logs for every time a heartbeat is sent.

## How to Run Tests

### How to run automatic Unit/Integration tests
1. In a terminal, run the command: `java -cp './lib/*:./' Tests`. This will start up the testing menu for these test.
2. Use the numbers show beside to choose which option on the menu. *Note: one test requires manual testing through looking at the console*

### How to run stress tests




### How to test checking for invalid weather entries
1. Open two terminals in the same main directory to simulate two different virtual machines (VMs).
2. Run the Aggregation Server and run a ContentServer with one of the `testInvalidX.txt` files. *X is a number from 1-3*
3. To test if the Content Server correctly handles invalid weather entries, compare the expected JSON from the corresponding `testInvalidXExpected.json` file with the JSON file being held on the Aggregation Server.

### How to test the heartbeat of the Content Server
1. Run a simple GETClient-Aggregation Server interaction stated above.
2. Both, console of Content Server and logs of Aggregation Server will show when a heartbeat is sent and received, hence, visually check if both are occuring.
3. Check timestamp on logs on Aggregation Server and weather.json file to see if to check if the heartbeat is sent within the 30 second window to ensure that weather entries are not removed for no reason.



### How to perform an Integration test on between different components of the Weather Feed Service

### How to perform a test of storing correctly updated and alive weather entries on the Aggregation Server
1. Open four terminals in the same main directory to simulate different virtual machines (VMs).
2. Run the Aggregation Server and run a ContentServer with any test file (unchanged version).
3. After some time (around 15 seconds), begin another ContentServer with the changed version of the already running test file.
4. Stop the intial Content Server by the shortcut: `Crtl + C` and observe (arround 30 seconds) the weather.json file and logs on of the Aggregation Server. *You should see the new values enter on the weather.json file and Logs indicating that outdated weather entries have been removed. When this happens, the new values should only be left in the weather.json file*

### How to perform a Parallel clients test on between different components of the Weather Feed Service
1. Open three terminals in the same main directory to simulate three different virtual machines (VMs).
2. Run the Aggregation Server and a single Content Server in seperates VMs.
3. In the final VM, run a parallel clients driver from the /driver directory with the command: `./runClientsX.sh`. *Where X is the desired number of clients to run simultaneously*
4. To specifically test the ability for GETCLient to retry connection with the server, reduce the maxClients value in the main() of AggregationServer.java and run() of ClientThread.java to 1. *Note: 100ms delay has been added to requests to simulate network delays in order to show test these features*

### How to perform a stress test on the Aggregation Server
1. Open ten terminals in the same main directory to simulate different virtual machines (VMs).
2. Change the maxClients value in the main() of AggregationServer.java and run() of ClientThread.java to 1.
3. Run the Aggregation Server and run 8 different Content Servers with different test files.
4. Attempt to run a GETClient onces or multiple times to check if the server is still stable and able to still handle requests.
5. Instead of repeatedily running a single GETClient, you may also test this with multiple simultaneous clients using any parallel clients driver.
4. To specifically test the ability for ContentServer to retry connection with the server, reduce the maxClients value in the main() of AggregationServer.java and run() of ClientThread.java to 1.

DONT FORGET CASE WHEN GET CLIENT TRIES TO GET EMPTY WEATHER.JSON file. NULL POINTER

Currently what has been implemented is the weather feed service as a single threaded service. 

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


IMPLEMENT eof END OF FILE INPUT VALLIDATION ON CONTENT SERVER
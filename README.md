Currently what has been implemented is the weather feed service as a single threaded service. 

How to compile:

make

How to run Content Server:

java -cp './lib/*:./' ContentServer localhost:9999 content/test.txt

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
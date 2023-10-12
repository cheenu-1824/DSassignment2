#!/bin/bash

rm "filesystem/weather.json"
touch "filesystem/weather.json"

sleep 2

CP='./lib/*:./'

success=true

java -classpath "$CP" AggregationServer 4567 &
aggregationServerPID=$!

sleep 2

java -classpath "$CP" ContentServer http://localhost:4567 content/test.txt &
contentServerPID=$!

sleep 2

java -classpath "$CP" GETClient http://localhost:4567 &
clientPID=$!

wait ${clientPID}
if [ $clientPID -ne 0 ]; then
    echo "Intial GETClient did not handle requests successfully."
    success=false
else 
    echo "Intial GETClient getting feed working..."
fi

echo "Simulating Aggregation Server crashing..."
kill "$aggregationServerPID"

sleep 2

echo "Simulating GETClient connecting to crashed Aggregation Server..."
java -classpath "$CP" GETClient http://localhost:4567 &
clientPID=$!

sleep 2

echo "Simulating Aggregation Server restarting..."
java -classpath "$CP" AggregationServer 4567 &
aggregationServerPID=$!

sleep 2

java -classpath "$CP" GETClient http://localhost:4567 &
clientPID=$!

wait ${clientPID}
if [ $clientPID -ne 0 ]; then
    echo "Second GETClient did not handle requests successfully."
    success=false
else 
    echo "Second GETClient getting feed working..."
fi

sleep 10

java -classpath "$CP" GETClient http://localhost:4567 &
clientPID=$!

wait ${clientPID}
if [ $clientPID -ne 0 ]; then
    echo "Second GETClient did not handle requests successfully."
    success=false
else 
    echo "Second GETClient getting feed working..."
fi

sleep 2

kill "$aggregationServerPID"
kill "$contentServerPID"

if [ "$success" = true ]; then
    echo "TEST PASSED"
else
    echo "TEST FAILED"
fi

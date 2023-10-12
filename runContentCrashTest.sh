#!/bin/bash

output_directory="tests/outputs/runContentCrashTest"

rm "filesystem/weather.json"
touch "filesystem/weather.json"

CP='./lib/*:./'
success=true

java -classpath "$CP" AggregationServer 4567 &
aggregationServerPID=$!

sleep 2

java -classpath "$CP" ContentServer http://localhost:4567 content/test1.txt 50 &
contentServerPID=$!

sleep 2

log_files=()
client_pids=()

start_getclient() {
    log_file="$output_directory/client_$1.log"
    java -classpath "$CP" GETClient http://localhost:4567 > "$log_file" 2>&1 &
    client_pids+=($!)
    log_files+=("$log_file")
}

start_getclient 1

wait ${client_pids[0]}
if [ $? -ne 0 ]; then
    echo "Initial GETClient did not handle requests successfully."
    success=false
else 
    echo "Initial GETClient getting feed working..."
fi

echo "Simulating Content Server crashing..."
kill "$contentServerPID"

sleep 2

echo "Simulating GETClient connecting to Aggregation Server when Content Server is crashed..."
start_getclient 2

sleep 2

echo "Simulating Content Server restarting..."
java -classpath "$CP" ContentServer http://localhost:4567 content/test1.txt 50 &
contentServerPID=$!

echo "Waiting over 30 seconds to check if weather from crashed server is still saved on Aggregation Server..."
sleep 35

echo "GETClient attempting to get weather delivered from crashed Content Server..."
start_getclient 3

wait ${client_pids[2]}
if [ $? -ne 0 ]; then
    echo "Second GETClient did not handle requests successfully."
    success=false
else 
    echo "Second GETClient getting feed working..."
fi

echo "Simulating Content Server crashing again..."
kill "$contentServerPID"

echo "Waiting for a while to check if weather from crashed server get removed from Aggregation Server..."
sleep 60

start_getclient 4

for pid in "${client_pids[@]}"; do
    wait "$pid"
    if [ $? -ne 0 ]; then
        echo "GETClient $pid did not handle requests successfully."
        success=false
    fi
done

kill "$aggregationServerPID"

for i in "${!log_files[@]}"; do
    expected_file="tests/expected/runContentCrashTest/client_$(($i + 1)).log"
    if cmp -s <(tail -n +3 "${log_files[$i]}") <(tail -n +3 "$expected_file") &> /dev/null; then
        echo "client_$i.log: TEST PASSED"
    else
        diff <(tail -n +3 "${log_files[$i]}") <(tail -n +3 "$expected_file")
        echo "client_$i.log: TEST FAILED"
        success=false
    fi
done



if [ "$success" = true ]; then
    echo "WHOLE TEST PASSED"
else
    echo "WHOLE TEST FAILED"
fi
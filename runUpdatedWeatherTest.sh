#!/bin/bash

output_directory="tests/outputs/runUpdatedWeatherTest"

# Remove existing "weather.json" and create an empty file
rm "filesystem/weather.json"
touch "filesystem/weather.json"

CP='./lib/*:./'
success=true

java -classpath "$CP" AggregationServer 4567 &
aggregationServerPID=$!
sleep 2

java -classpath "$CP" ContentServer http://localhost:4567 content/test1.txt 100 &
contentServer1PID=$!
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

for pid in "${client_pids[@]}"; do
    wait "$pid"
    if [ $? -ne 0 ]; then
        echo "GETClient $pid did not handle requests successfully."
        success=false
    fi
done

echo "Simulating new Content Server updating the same location..."
java -classpath "$CP" ContentServer http://localhost:4567 content/test1Changed.txt 150&
contentServer2PID=$!
sleep 2

echo "New GETClient attempting to retrieve new updated weather from Aggregation Server..."
start_getclient 2
sleep 2

for pid in "${client_pids[@]:1}"; do
    wait "$pid"
    if [ $? -ne 0 ]; then
        echo "GETClient $pid did not handle requests successfully."
        success=false
    fi
done

kill "$aggregationServerPID"
kill "$contentServer1PID"
kill "$contentServer2PID"

for i in "${!log_files[@]}"; do
    if cmp -s "${log_files[$i]}" "tests/expected/runUpdatedWeatherTest/client_$(($i + 1)).log" &> /dev/null; then
        echo "client_$(($i + 1)).log: TEST PASSED"
    else
        echo "client_$(($i + 1)).log: TEST FAILED"
        success=false
    fi
done


if [ "$success" = true ]; then
    echo "WHOLE TEST PASSED"
else
    echo "WHOLE TEST FAILED"
fi

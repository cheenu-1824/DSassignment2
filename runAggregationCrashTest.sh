#!/bin/bash

output_directory="tests/outputs/runAggregationCrashTest"

rm "filesystem/weather.json"
touch "filesystem/weather.json"
sleep 1

CP='./lib/*:./'
success=true

java -classpath "$CP" AggregationServer 4567 &
aggregationServerPID=$!
sleep 2

java -classpath "$CP" ContentServer http://localhost:4567 content/test2.txt 25 &
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

for pid in "${client_pids[@]}"; do
    wait "$pid"
    if [ $? -ne 0 ]; then
        echo "GETClient $pid did not handle requests successfully."
    fi
done

echo "Waiting for Aggregation Server to save weather..."
sleep 20

echo "Simulating Aggregation Server crashing..."
kill "$aggregationServerPID"
sleep 2

start_getclient 2

echo "Simulating Aggregation Server restarting..."
java -classpath "$CP" AggregationServer 4567 &
aggregationServerPID=$!
sleep 2

start_getclient 3

for pid in "${client_pids[@]:1}"; do
    wait "$pid"
    if [ $? -ne 0 ]; then
        echo "GETClient $pid did not handle requests successfully."
    fi
done

kill "$aggregationServerPID"
kill "$contentServerPID"

for log_file in "${log_files[@]}"; do
    cat "$log_file"
done

for i in "${!log_files[@]}"; do
    expected_file="tests/expected/runAggregationCrashTest/client_$(($i + 1)).log"
    if diff -q "${log_files[$i]}" "$expected_file"; then
        echo "client_$i.log: TEST PASSED"
    else
        diff "${log_files[$i]}" "$expected_file"
        echo "client_$i.log: TEST FAILED"
        success=false
    fi
done

if [ "$success" = true ]; then
    echo "WHOLE TEST PASSED"
else
    echo "WHOLE TEST FAILED"
fi

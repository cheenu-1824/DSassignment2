#!/bin/bash

rm "filesystem/weather.json"
touch "filesystem/weather.json"

CP='./lib/*:./'

numClients=10
success=true

java -classpath "$CP" AggregationServer 4567 &
aggregationServerPID=$!
sleep 2

java -classpath "$CP" ContentServer http://localhost:4567 content/test.txt 1 &
contentServerPID=$!
sleep 2

# Array to store the log file names for each client
log_files=()

for ((i = 1; i <= numClients; i++)); do
    log_file="tests/outputs/runClients10/client_$i.log"
    java -classpath "$CP" GETClient http://localhost:4567 > "$log_file" 2>&1 &
    client_pids[$i]=$!
    log_files+=("$log_file")
done

echo "All clients have started running..."

for ((i = 1; i <= numClients; i++)); do
    wait ${client_pids[$i]}
    if [ $? -ne 0 ]; then
        echo "GETClient $i did not handle requests successfully."
        success=false
    fi
done

sleep 12

kill "$aggregationServerPID"
kill "$contentServerPID"

# Display the contents of the log files in order
for log_file in "${log_files[@]}"; do
    cat "$log_file"
done

expected_output="tests/expected/runClients10/expectedOutput.log"

for log_file in "${log_files[@]}"; do
    if cmp -s <(tail -n +3 "$log_file") <(tail -n +3 "$expected_output") &> /dev/null; then
        echo "$log_file: TEST PASSED"
    else
        echo "$log_file: TEST FAILED"
        success=false
    fi
done


if [ "$success" = true ]; then
    echo "WHOLE TEST PASSED"
else
    echo "WHOLE TEST FAILED"
fi

#!/bin/bash

# Run the Content Server
java -cp './lib/*:./' ContentServer localhost:4567 content/test.txt

# Run the Content Server
java -cp './lib/*:./' ContentServer localhost:4567 content/test.txt

# Sleep for a few seconds to give servers time to start
#sleep 5

# Run the GET Client
#java -cp './lib/*:./' GETClient localhost:4567

# Clean up (optional)
# killall java
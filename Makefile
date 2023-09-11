# Makefile for Assignmen2

# Set the Java compiler and options
JAVAC = javac -cp .:lib/gson-2.10.1.jar

# List of source files
SOURCES = AggregationServer.java GETClient.java ContentServer.java
CLASSES = $(SOURCES:.java=.class)
all: compile

# Command-line arguments
GETCLIENT_ARGS = arg1 arg2 arg3 arg4

compile: $(SOURCES)
	$(JAVAC) $(SOURCES)

clean:
	rm -f *.class

# Run the aggregation server
aggregationServer: AggregationServer.class
	java AggregationServer

# Run the client
client: GETClient.class
	java GETClient  

# Run the content server
contentServer: ContentServer.class
	java ContentServer 

.PHONY: all compile clean aggregationServer client contentServer

# Makefile for Assignmen2

# Set the Java compiler and options
JAVAC = javac

# List of source files
SOURCES = AggregationServer.java Client.java
CLASSES = $(SOURCES:.java=.class)
all: compile

compile: $(SOURCES)
	$(JAVAC) $(SOURCES)

clean:
	rm -f *.class

# Run the aggregation server
aggregationServer: AggregationServer.class
	java AggregationServer

# Run the client
client: Client.class
	java Client

# Run the content server
#contentServer: ContentServer.class
#	java ContentServer

.PHONY: all compile clean aggregationServer client #contentServer

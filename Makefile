# Makefile for Assignmen2

# Set the Java compiler and options
JAVAC = javac

# Classpath variable for libraries
LIB = -cp "lib/gson-2.10.1.jar"

# Variable for the .java files
SOURCES = $(wildcard *.java)

# Default make
all:
	$(JAVAC) $(LIB) $(SOURCES)

# Cleanup class files
clean:
	rm -f *.class

.PHONY: all clean
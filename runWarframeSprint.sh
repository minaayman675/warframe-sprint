#!/bin/sh
EXECUTABLE="$(ls -t1 target/*-jar-with-dependencies.jar | head -n1)" # get most recently modified jar file
echo "Running $EXECUTABLE" # tell the user which version we are running
java -jar "$EXECUTABLE" 2>&1 | tee -a WarframeSprint.log # run the program and append output to log

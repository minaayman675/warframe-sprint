#!/bin/sh
java -jar target/warframe-sprint-1.0-SNAPSHOT-jar-with-dependencies.jar 2>&1 | tee -a ToggleShift.log

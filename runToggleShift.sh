#!/bin/sh
java -jar target/toggle-shift-1.0-SNAPSHOT-jar-with-dependencies.jar 2>&1 | tee -a ToggleShift.log

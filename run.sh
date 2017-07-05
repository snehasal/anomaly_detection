#!/bin/bash
javac -classpath ".:./src:./src/jars/*" ./src/AnomalyDetector.java
java -classpath ".:./src:./src/jars/*" AnomalyDetector ./log_input/batch_log.json ./log_input/stream_log.json ./log_output/flagged_purchases.json
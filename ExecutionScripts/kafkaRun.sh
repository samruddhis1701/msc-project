#!/bin/bash
clear

# RUNNING RECEIVER FOR KAFKA
echo "RUNNING RECEIVER"
java -jar MScReceiver.jar KAFKA

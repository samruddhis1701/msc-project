#!/bin/bash
clear

# RUNNING RECEIVER FOR RABBITMQ
echo "RUNNING RECEIVER"
java -jar MScReceiver.jar RABBITMQ

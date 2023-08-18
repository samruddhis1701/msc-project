#!/bin/bash
clear

# RUNNING MESSAGE VARIABILITY TESTS FOR ALL QUEUES
echo "RUNNING MESSAGE VARIABILITY TESTS"

#RABBITMQ SHORT 1 million
echo "+++++++++++++++++RABBITMQ +++++++++++++++++++"
echo "RABBITMQ TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" RABBITMQ 40 1
date +%s
echo "END RABBITMQ TEST"

#APACHE KAFKA SHORT 1 million
echo "+++++++++++++++++APACHE KAFKA +++++++++++++++++++"
echo "KAFKA TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" KAFKA 40 1
date +%s
echo "END KAFKA TEST"

#APACHE ACTIVEMQ ARTEMIS SHORT 1 million
echo "++++++++++++++++++APACHE ACTIVEMQ ARTEMIS ++++++++++++++++++"
echo "ARTEMIS TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" ARTEMIS 40 1
date +%s
echo "END ARTEMIS TEST"

#RABBITMQ SHORT 5 million
echo "+++++++++++++++++RABBITMQ +++++++++++++++++++"
echo "RABBITMQ TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" RABBITMQ 200 1
date +%s
echo "END RABBITMQ TEST"

#APACHE KAFKA SHORT 5 million
echo "+++++++++++++++++APACHE KAFKA +++++++++++++++++++"
echo "KAFKA TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" KAFKA 200 1
date +%s
echo "END KAFKA TEST"

#APACHE ACTIVEMQ ARTEMIS SHORT 5 million
echo "++++++++++++++++++APACHE ACTIVEMQ ARTEMIS ++++++++++++++++++"
echo "ARTEMIS TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" ARTEMIS 200 1
date +%s
echo "END ARTEMIS TEST"

#RABBITMQ STANDARD 1 million
echo "+++++++++++++++++RABBITMQ SHORT+++++++++++++++++++"
echo "RABBITMQ TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" RABBITMQ 40 4
date +%s
echo "END RABBITMQ TEST"

#APACHE KAFKA STANDARD 1 million
echo "+++++++++++++++++APACHE KAFKA +++++++++++++++++++"
echo "KAFKA TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" KAFKA 40 4
date +%s
echo "END KAFKA TEST"

#APACHE ACTIVEMQ ARTEMIS STANDARD 1 million
echo "++++++++++++++++++APACHE ACTIVEMQ ARTEMIS ++++++++++++++++++"
echo "ARTEMIS TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" ARTEMIS 40 4
date +%s
echo "END ARTEMIS TEST"

#RABBITMQ STANDARD 5 million
echo "+++++++++++++++++RABBITMQ SHORT+++++++++++++++++++"
echo "RABBITMQ TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" RABBITMQ 200 4
date +%s
echo "END RABBITMQ TEST"

#APACHE KAFKA STANDARD 5 million
echo "+++++++++++++++++APACHE KAFKA +++++++++++++++++++"
echo "KAFKA TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" KAFKA 200 4
date +%s
echo "END KAFKA TEST"

#APACHE ACTIVEMQ ARTEMIS STANDARD 5 million
echo "++++++++++++++++++APACHE ACTIVEMQ ARTEMIS ++++++++++++++++++"
echo "ARTEMIS TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" ARTEMIS 200 4 
date +%s
echo "END ARTEMIS TEST"

#!/bin/bash
clear

# RUNNING TIMER TESTS FOR ALL QUEUES
echo "RUNNING TIMER TESTS"

#RABBITMQ SHORT
echo "+++++++++++++++++RABBITMQ +++++++++++++++++++"
echo "RABBITMQ TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" RABBITMQ 50 5 & javapid=$!
sleep 1500
kill $javapid
date +%s
echo "END RABBITMQ TEST"

#APACHE KAFKA SHORT
echo "+++++++++++++++++APACHE KAFKA +++++++++++++++++++"
echo "KAFKA TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" KAFKA 50 5 & javapid=$!
sleep 1500
kill $javapid
date +%s
echo "END KAFKA TEST"

#APACHE ACTIVEMQ ARTEMIS SHORT
echo "++++++++++++++++++APACHE ACTIVEMQ ARTEMIS ++++++++++++++++++"
echo "ARTEMIS TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" ARTEMIS 50 5 & javapid=$!
sleep 1500
kill $javapid
date +%s
echo "END ARTEMIS TEST"

#RABBITMQ STANDARD
echo "+++++++++++++++++RABBITMQ SHORT+++++++++++++++++++"
echo "RABBITMQ TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" RABBITMQ 50 5 & javapid=$!
sleep 1500
kill $javapid
date +%s
echo "END RABBITMQ TEST"

#APACHE KAFKA STANDARD
echo "+++++++++++++++++APACHE KAFKA +++++++++++++++++++"
echo "KAFKA TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" KAFKA 50 5 & javapid=$!
sleep 1500
kill $javapid
date +%s
echo "END KAFKA TEST"

#APACHE ACTIVEMQ ARTEMIS STANDARD
echo "++++++++++++++++++APACHE ACTIVEMQ ARTEMIS ++++++++++++++++++"
echo "ARTEMIS TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/video" ARTEMIS 50 5 & javapid=$!
sleep 1500
kill $javapid
date +%s
echo "END ARTEMIS TEST"

#RABBITMQ LONG
echo "+++++++++++++++++RABBITMQ SHORT+++++++++++++++++++"
echo "RABBITMQ TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/150mbvideo" RABBITMQ 300 5 & javapid=$!
sleep 1500
kill $javapid
date +%s
echo "END RABBITMQ TEST"

#APACHE KAFKA LONG
echo "+++++++++++++++++APACHE KAFKA +++++++++++++++++++"
echo "KAFKA TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/150mbvideo" KAFKA 300 5 & javapid=$!
sleep 1500
kill $javapid
date +%s
echo "END KAFKA TEST"

#APACHE ACTIVEMQ ARTEMIS LONG
echo "++++++++++++++++++APACHE ACTIVEMQ ARTEMIS ++++++++++++++++++"
echo "ARTEMIS TEST START TIME"
date +%s
java -jar MScSender.jar "/home/azureuser/150mbvideo" ARTEMIS 300 5 & javapid=$!
sleep 1500
kill $javapid
date +%s
echo "END ARTEMIS TEST"

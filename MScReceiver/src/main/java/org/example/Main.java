package org.example;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.*;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import javax.jms.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class Main {
    private final static String RABBITMQ_QUEUE_NAME = "videoStreamData.queue";
    private final static String RABBITMQ_TOPIC_NAME = "videoStreamData.topic";
    private final static String KAFKA_TOPIC_NAME = "videoStreamData.topic";
    private final static String ARTEMIS_QUEUE_NAME = "videoStreamData.topic";
    private static final String RABBITMQ_DATADOG_LATENCY_METRIC = "rabbitmq.message.latency";
    private static final String KAFKA_DATADOG_LATENCY_METRIC = "kafka.message.latency";
    private static final String ARTEMIS_DATADOG_LATENCY_METRIC = "activemq.artemis.message.latency";

    public static void main(String[] args) throws Exception {

        var main = new Main();
        Thread rabbitThread = new Thread(main.rabbitmqRunnable);
        Thread kafkaThread = new Thread(main.kafkaRunnable);
        Thread artemisThread = new Thread(main.artemisRunnable);

        if (args.length == 1) {
            var queueName = args[0];
            if ("RABBITMQ".equals(queueName)) {
                rabbitThread.start();
                rabbitThread.join();
            } else if ("KAFKA".equals(queueName)) {
                kafkaThread.start();
                kafkaThread.join();
            } else if ("ARTEMIS".equals(queueName)) {
                artemisThread.start();
                artemisThread.join();
            } else {
                System.out.println("Queue name is wrong");
            }
        } else {
            System.out.println("No queue name provided");
        }
    }

    Runnable rabbitmqRunnable = () -> {
        try {
            String username = "rabbitsam";
            String password = "rabbitsam";
            int port = 5672;
            String virtualHost = "/";
            String routingKey = "videoStreamData.key";

            ConnectionFactory rabbitMQFactory = new ConnectionFactory();
            rabbitMQFactory.setHost("40.88.129.65");
            rabbitMQFactory.setUsername(username);
            rabbitMQFactory.setPassword(password);
            rabbitMQFactory.setPort(port);
            rabbitMQFactory.setVirtualHost(virtualHost);

            try (Connection rabbitMQConnection = rabbitMQFactory.newConnection();
                 var statsClient = statsClient()) {
                Channel rabbitMQChannel = rabbitMQConnection.createChannel();
                rabbitMQChannel.exchangeDeclare(RABBITMQ_TOPIC_NAME, BuiltinExchangeType.TOPIC, true);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    long endTime = System.currentTimeMillis();
                    long sentTimestamp = (long) delivery.getProperties().getHeaders().get("rabbitmq.timestamp");
                    long latency = Math.abs(endTime - sentTimestamp);
                    System.out.println("Received from RabbitMQ: " + delivery.getBody().length + ", Latency: " + latency + " ms");
                    // send metrics to Datadog server
                    statsClient.recordDistributionValue(RABBITMQ_DATADOG_LATENCY_METRIC, latency);
                };

                rabbitMQChannel.basicConsume(RABBITMQ_QUEUE_NAME, true, deliverCallback, consumerTag -> {
                });

                while (true) {
                    Thread.sleep(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
    Runnable kafkaRunnable = () -> {
        try {
            // Kafka setup
            Properties kafkaProps = new Properties();
            kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "40.88.129.65:9092");
            kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
            kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

            try (KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(kafkaProps);
                 var statsClient = statsClient()) {
                kafkaConsumer.subscribe(Collections.singletonList(KAFKA_TOPIC_NAME));

                while (true) {
                    var records = kafkaConsumer.poll(Duration.ofMillis(100));
                    for (var record : records) {
                        var endTime = System.currentTimeMillis();
                        var value = record.value();
                        var startTime = bytesToLong(record.headers().lastHeader("kafka.start-time").value());
                        var latency = Math.abs(endTime - startTime);
                        System.out.println("Received from Kafka: " + value.length + ", Latency " + latency + " ms");
                        statsClient.recordDistributionValue(KAFKA_DATADOG_LATENCY_METRIC, latency);

                    }
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    };
    Runnable artemisRunnable = () -> {
        {
            try (ActiveMQConnectionFactory artemisFactory = new ActiveMQConnectionFactory();
                 var statsClient = statsClient()) {
                artemisFactory.setBrokerURL("tcp://40.88.129.65:61616");
                artemisFactory.setUser("admin");
                artemisFactory.setPassword("admin");
                javax.jms.Connection artemisConnection = artemisFactory.createConnection();
                artemisConnection.start();

                Session artemisSession = artemisConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue artemisQueue = artemisSession.createQueue(ARTEMIS_QUEUE_NAME);
                MessageConsumer artemisConsumer = artemisSession.createConsumer(artemisQueue);

                while (true) {
                    Message artemisMessage = artemisConsumer.receiveNoWait();
                    if (artemisMessage instanceof BytesMessage) {
                        long endTime = System.currentTimeMillis();
                        long startTime = artemisMessage.getLongProperty("artemisTimestamp");
                        long latency = Math.abs(endTime - startTime);
                        System.out.println("Received from Artemis: " + artemisMessage.getBody(byte[].class).length
                                + " Latency: " + latency + " ms");
                        statsClient.recordDistributionValue(ARTEMIS_DATADOG_LATENCY_METRIC, latency);
                    }
                }
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private static long bytesToLong(byte[] byteArray) {
//        System.out.println("Sending custom metrics..");
        var buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(byteArray);
        buffer.flip(); // Prepare the buffer for reading
        return buffer.getLong();
    }

    private StatsDClient statsClient() {
        StatsDClient Statsd = new NonBlockingStatsDClientBuilder()
                .prefix("statsd").hostname("localhost")
                .port(8125)
                .processorWorkers(2)
                .prefix("custom")
                .build();
        return Statsd;
    }

}






package org.example;

import com.rabbitmq.client.*;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.jms.*;
import javax.jms.Queue;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class FileByteReader implements Runnable {

    private final String directoryPath;
    private final String queue;
    private final int numberOfIterations;
    private static final String TOPIC_NAME = "videoStreamData.topic";

    public FileByteReader(String directoryPath, String queueName, int numberOfIterations) {
        this.directoryPath = directoryPath;
        this.queue = queueName;
        this.numberOfIterations = numberOfIterations;
    }

    @Override
    public void run() {
        if ("RABBITMQ".equals(queue)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(String.format("START TIME: " + formatter.format(new Date(System.currentTimeMillis()))));
            sendToRabbitMQ();
            System.out.println(String.format("END TIME: " + formatter.format(new Date(System.currentTimeMillis()))));
        } else if ("KAFKA".equals(queue)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(String.format("START TIME: " + formatter.format(new Date(System.currentTimeMillis()))));
            sendToApacheKafka();
            System.out.println(String.format("END TIME: " + formatter.format(new Date(System.currentTimeMillis()))));
        } else if ("ARTEMIS".equals(queue)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(String.format("START TIME: " + formatter.format(new Date(System.currentTimeMillis()))));
            sendToActiveMQArtemis();
            System.out.println(String.format("END TIME: " + formatter.format(new Date(System.currentTimeMillis()))));
        } else {
            System.out.println("Wrong option.");
        }
    }

    public void sendToRabbitMQ() {
        String username = "rabbitsam";
        String password = "rabbitsam";
        int port = 5672;
        String virtualHost = "/";
        String routingKey = "videoStreamData.key";
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("40.88.129.65");
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        factory.setPort(port);

        var videoFiles = getFileDirectory(directoryPath);
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(TOPIC_NAME, BuiltinExchangeType.TOPIC, true);

            var filesCollection = Arrays.stream(videoFiles.listFiles())
                    .filter(File::isFile)
                    .collect(Collectors.toList());
            for (int i = 0; i < numberOfIterations; i++) {
                for (File file : filesCollection) {
                    try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(file))) {
                        int batches = 0;
                        byte[] buffer = new byte[1048400];
                        int bytesRead;

                        while ((bytesRead = br.read(buffer)) != -1) {

                            Map<String, Object> headers = new HashMap<>();

                            headers.put("rabbitmq.timestamp", System.currentTimeMillis());
                            var property = new AMQP.BasicProperties.Builder()
                                    .headers(headers)
                                    .build();

                            channel.basicPublish(TOPIC_NAME, routingKey, property, buffer);
                            Thread.sleep(15);
                            System.out.println(" Number of Iterations " + i);
                        }
                    } catch (IOException e) {
                        System.out.println("Error occurred while reading directory: " + e.getMessage());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendToApacheKafka() {

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "40.88.129.65:9092"); // Replace with your Kafka broker(s)
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");

        var videoFiles = getFileDirectory(directoryPath);
        var filesCollection = Arrays.stream(videoFiles.listFiles())
                .filter(File::isFile)
                .collect(Collectors.toList());

        for (int i = 0; i < numberOfIterations; i++) {
            for (File file : filesCollection) {
                try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(file))) {

                    KafkaProducer<String, byte[]> producer = new KafkaProducer<>(properties);

                    int batches = 0;
                    byte[] buffer = new byte[1048400];
                    int bytesRead;

                    while ((bytesRead = br.read(buffer)) != -1) {
                        ProducerRecord<String, byte[]> record = new ProducerRecord<>(TOPIC_NAME, buffer);
                        record.headers().add("kafka.start-time", longToBytes(System.currentTimeMillis()));
                        producer.send(record).get();
                        Thread.sleep(15);
//                        System.out.println(" Batch: " + ++batches + ", Bytes Read: " + bytesRead);
                        System.out.println(" Number of Iterations " + i);
                    }
                    producer.flush();
                    producer.close();
                } catch (IOException | InterruptedException | ExecutionException e) {
                    System.out.println("Error occurred while reading directory: ");
                }
            }
        }
    }

    public static byte[] longToBytes(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return buffer.array();
    }

    public void sendToActiveMQArtemis() {
        String brokerURL = "tcp://40.88.129.65:61616";

        var connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        try (javax.jms.Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

            Queue queue = session.createQueue(TOPIC_NAME);
            boolean newDurableValue = false;

            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            var videoFiles = getFileDirectory(directoryPath);
            var filesCollection = Arrays.stream(videoFiles.listFiles())
                    .filter(File::isFile)
                    .collect(Collectors.toList());

            for (int i = 0; i < numberOfIterations; i++) {
                for (File file : filesCollection) {
                    try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(file))) {

                        int batches = 0;
                        byte[] buffer = new byte[946004];
                        int bytesRead;

                        while ((bytesRead = br.read(buffer)) != -1) {
                            BytesMessage message = session.createBytesMessage();
                            message.setLongProperty("artemisTimestamp", System.currentTimeMillis());
                            message.writeBytes(buffer, 0, bytesRead);
                            producer.send(message);
                            Thread.sleep(15);
                            System.out.println(" Number of Iterations " + i);
//                            System.out.println(" Batch: " + ++batches + ", Bytes Read: " + bytesRead);
                        }
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Error occurred while reading directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private File getFileDirectory(String directoryPath) {
        File videoFiles = new File(directoryPath);
        if (videoFiles.exists() && videoFiles.isDirectory()) {
            return videoFiles;
        } else {
            System.out.println("One or both of the specified files do not exist or are not valid files.");
            throw new RuntimeException();
        }
    }
}
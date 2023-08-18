package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {

        String directoryPath = args[0];
        String queueName = args[1];
        String loop = args[2];
        String threadPools = args[3];

        int numberOfIterations = Integer.parseInt(loop);
        int numberOfThreads = Integer.parseInt(threadPools);

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        try {
            for (int i = 0; i < numberOfThreads; i++) {
                Runnable task = new FileByteReader(directoryPath, queueName, numberOfIterations);
                executorService.submit(task);
            }
        } finally {
            executorService.shutdown();
        }


    }
}
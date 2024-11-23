package org.example;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.resource.ClientResources;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int nThreads = 2;
        int nRequests = 100000;
        int delayMs = 0;
        String redisUri = "redis://localhost:6379";
        String provider = "redisson";

        for(int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "--threads" -> nThreads = Integer.parseInt(args[i + 1]);
                case "--provider" -> provider = args[i + 1];
                case "--request-pairs" -> nRequests = Integer.parseInt(args[i + 1]);
                case "--redis" -> redisUri = args[i + 1];
                case "--delay-ms" -> delayMs = Integer.parseInt(args[i + 1]);
                case "--help" -> {
                    printHelp(nThreads, nRequests, delayMs, redisUri, provider);
                    return;
                }
            }
        }

        printArguments(nThreads, nRequests, delayMs, redisUri, provider);

        if (provider.equals("redisson")) {
            redisson(nThreads, nRequests, delayMs, redisUri);
        } else if (provider.equals("lettuce")) {
            lettuce(nThreads, nRequests, delayMs, redisUri);
        } else {
            System.out.println("Unknown provider: " + provider);
        }
    }

    private static void printArguments(int nThreads, int nRequests, int testDelayMs, String redisUri, String provider) {
        System.out.println("Arguments: redis = " + redisUri + ", threads = " + nThreads + ", provider = " + provider + ", request-pairs = " + nRequests + ", delay-ms = " + testDelayMs);
    }

    private static void printHelp(int nThreads, int nRequests, int testDelayMs, String redisUri, String provider) {
        System.out.println("Commands with defaults:");
        System.out.println("\t--threads " + nThreads);
        System.out.println("\t--provider " + provider + " (redisson, lettuce)");
        System.out.println("\t--request-pairs " + nRequests);
        System.out.println("\t--delay-ms " + testDelayMs);
        System.out.println("\t--redis " + redisUri);
    }

    private static void redisson(int nThreads, int nRequests, int testDelayMs, String redisUri) throws InterruptedException {
        Config config = new Config();

        config.setNettyThreads(nThreads);

        config.useSingleServer()
                .setAddress(redisUri);

        RedissonClient redissonClient = Redisson.create(config);

        System.out.println("Sleep start");

        Thread.sleep(testDelayMs);

        System.out.println("Start test");

        var futures = new ArrayList<CompletableFuture<Void>>();
        for(int i = 0; i < nRequests; ++i) {
            String key = "myKey" + i;
            String value = "Hello, Redis!";
            RBucket<String> bucket = redissonClient.getBucket(key);
            var f = bucket.setAsync(value).thenCompose(x -> bucket.getAsync().thenApply(y -> null)).toCompletableFuture();
            futures.add(f.thenApply(x -> null));
        }

        checkResults(futures);

        redissonClient.shutdown();
    }

    private static void lettuce(int nThreads, int nRequests, int testDelayMs, String redisUri) throws InterruptedException {
        StatefulRedisConnection<String, String> connection;
        try (RedisClient redisClient = RedisClient.create(
                ClientResources.builder()
                    .ioThreadPoolSize(nThreads)
                    .build(),
                redisUri
        )) {
            connection = redisClient.connect();

            System.out.println("Sleep start");

            Thread.sleep(testDelayMs);

            System.out.println("Start test");

            RedisAsyncCommands<String, String> commands = connection.async();
            var futures = new ArrayList<CompletableFuture<Void>>(nRequests);
            for(int i = 0; i < nRequests; ++i) {
                String key = "myKey" + i;
                String value = "Hello, Redis!";
                var f = commands.set(key, value).thenCompose(x -> commands.get(key)).toCompletableFuture();
                futures.add(f.thenApply(x -> null));
            }

            checkResults(futures);
        }
    }

    private static void checkResults(ArrayList<CompletableFuture<Void>> futures) {
        int error = 0;
        int success = 0;
        for(var task : futures) {
            try {
                task.get();
                success += 1;
            } catch (Exception ex ) {
                error += 1;
            }
        }
        System.out.println("success = " + success + " , errors = " + error);
    }
}
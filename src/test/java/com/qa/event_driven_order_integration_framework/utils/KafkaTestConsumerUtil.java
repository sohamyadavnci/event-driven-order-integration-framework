package com.qa.event_driven_order_integration_framework.utils;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class KafkaTestConsumerUtil {

    public static <T> T consumeMessageByKey(
            String bootstrapServers,
            String topic,
            Class<T> targetClass,
            String expectedKey,
            int timeoutSeconds
    ) {
        Map<String, Object> props = createConsumerProperties(bootstrapServers, targetClass);

        AtomicReference<T> matchedMessage = new AtomicReference<>();

        try (KafkaConsumer<String, T> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            Awaitility.await()
                    .atMost(Duration.ofSeconds(timeoutSeconds))
                    .pollInterval(Duration.ofMillis(500))
                    .until(() -> {
                        ConsumerRecords<String, T> records =
                                consumer.poll(Duration.ofMillis(500));

                        for (ConsumerRecord<String, T> record : records) {
                            if (expectedKey.equals(record.key())) {
                                matchedMessage.set(record.value());
                                return true;
                            }
                        }

                        return false;
                    });
        }

        return matchedMessage.get();
    }

    public static <T> List<T> consumeMessagesByKeyForDuration(
            String bootstrapServers,
            String topic,
            Class<T> targetClass,
            String expectedKey,
            int durationSeconds
    ) {
        Map<String, Object> props = createConsumerProperties(bootstrapServers, targetClass);

        List<T> matchedMessages = new ArrayList<>();

        long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);

        try (KafkaConsumer<String, T> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            while (System.currentTimeMillis() < endTime) {
                ConsumerRecords<String, T> records =
                        consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, T> record : records) {
                    if (expectedKey.equals(record.key())) {
                        matchedMessages.add(record.value());
                    }
                }
            }
        }

        return matchedMessages;
    }

    private static <T> Map<String, Object> createConsumerProperties(
            String bootstrapServers,
            Class<T> targetClass
    ) {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, targetClass.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return props;
    }
}
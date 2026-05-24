package com.qa.event_driven_order_integration_framework.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${topics.order-events}")
    private String orderEventsTopic;

    @Value("${topics.processed-orders}")
    private String processedOrdersTopic;

    @Value("${topics.order-dlq}")
    private String orderDlqTopic;

    @Bean
    public NewTopic orderEventsTopic() {
        return new NewTopic(orderEventsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic processedOrdersTopic() {
        return new NewTopic(processedOrdersTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic orderDlqTopic() {
        return new NewTopic(orderDlqTopic, 1, (short) 1);
    }
}
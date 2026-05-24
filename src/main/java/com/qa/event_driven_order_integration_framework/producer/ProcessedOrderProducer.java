package com.qa.event_driven_order_integration_framework.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.qa.event_driven_order_integration_framework.model.ProcessedOrderEvent;

@Component
public class ProcessedOrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String processedOrdersTopic;

    public ProcessedOrderProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${topics.processed-orders}") String processedOrdersTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.processedOrdersTopic = processedOrdersTopic;
    }

    public void publishProcessedOrder(ProcessedOrderEvent processedOrderEvent) {
        kafkaTemplate.send(
                processedOrdersTopic,
                processedOrderEvent.getOrderId(),
                processedOrderEvent
        );
    }
}
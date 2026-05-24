package com.qa.event_driven_order_integration_framework.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.qa.event_driven_order_integration_framework.model.ErrorEvent;

@Component
public class DlqProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderDlqTopic;

    public DlqProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${topics.order-dlq}") String orderDlqTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderDlqTopic = orderDlqTopic;
    }

    public void publishToDlq(ErrorEvent errorEvent) {
        kafkaTemplate.send(
                orderDlqTopic,
                errorEvent.getOrderId(),
                errorEvent
        );
    }
}
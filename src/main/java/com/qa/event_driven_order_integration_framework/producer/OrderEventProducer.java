package com.qa.event_driven_order_integration_framework.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.qa.event_driven_order_integration_framework.model.OrderEvent;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderEventsTopic;

    public OrderEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${topics.order-events}") String orderEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderEventsTopic = orderEventsTopic;
    }

    public void publishOrderEvent(OrderEvent orderEvent) {
        kafkaTemplate.send(orderEventsTopic, orderEvent.getOrderId(), orderEvent);
    }
}
package com.qa.event_driven_order_integration_framework.tests;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import com.qa.event_driven_order_integration_framework.base.BaseIntegrationTest;
import com.qa.event_driven_order_integration_framework.model.OrderEvent;
import com.qa.event_driven_order_integration_framework.model.ProcessedOrderEvent;
import com.qa.event_driven_order_integration_framework.utils.KafkaTestConsumerUtil;
import com.qa.event_driven_order_integration_framework.utils.KafkaTestProducerUtil;

public class IdempotencyTest extends BaseIntegrationTest {

    @Value("${topics.order-events}")
    private String orderEventsTopic;

    @Value("${topics.processed-orders}")
    private String processedOrdersTopic;

    @Test
    void shouldProcessDuplicateOrderEventOnlyOnce() {
        String orderId = "ORD-IDEMP-" + System.currentTimeMillis();

        OrderEvent orderEvent = new OrderEvent(
                UUID.randomUUID().toString(),
                orderId,
                "CUST-777",
                150.00,
                "EUR",
                "ORDER_CREATED",
                Instant.now()
        );

        KafkaTestProducerUtil.publishMessage(
                getKafkaBootstrapServers(),
                orderEventsTopic,
                orderId,
                orderEvent
        );

        KafkaTestProducerUtil.publishMessage(
                getKafkaBootstrapServers(),
                orderEventsTopic,
                orderId,
                orderEvent
        );

        List<ProcessedOrderEvent> processedEvents =
                KafkaTestConsumerUtil.consumeMessagesByKeyForDuration(
                        getKafkaBootstrapServers(),
                        processedOrdersTopic,
                        ProcessedOrderEvent.class,
                        orderId,
                        8
                );

        assertEquals(
                1,
                processedEvents.size(),
                "Duplicate order event should be processed only once"
        );
    }
}
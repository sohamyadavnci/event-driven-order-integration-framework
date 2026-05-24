package com.qa.event_driven_order_integration_framework.tests;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import com.qa.event_driven_order_integration_framework.base.BaseIntegrationTest;
import com.qa.event_driven_order_integration_framework.model.ErrorEvent;
import com.qa.event_driven_order_integration_framework.model.OrderEvent;
import com.qa.event_driven_order_integration_framework.utils.KafkaTestConsumerUtil;
import com.qa.event_driven_order_integration_framework.utils.KafkaTestProducerUtil;

public class DlqValidationTest extends BaseIntegrationTest {

    @Value("${topics.order-events}")
    private String orderEventsTopic;

    @Value("${topics.order-dlq}")
    private String orderDlqTopic;

    @Test
    void shouldRouteInvalidOrderEventToDlq() {
        String orderId = "ORD-DLQ-" + System.currentTimeMillis();

        OrderEvent invalidOrderEvent = new OrderEvent(
                UUID.randomUUID().toString(),
                orderId,
                "CUST-501",
                -100.00,
                "EUR",
                "ORDER_CREATED",
                Instant.now()
        );

        KafkaTestProducerUtil.publishMessage(
                getKafkaBootstrapServers(),
                orderEventsTopic,
                orderId,
                invalidOrderEvent
        );

        ErrorEvent errorEvent = KafkaTestConsumerUtil.consumeMessageByKey(
                getKafkaBootstrapServers(),
                orderDlqTopic,
                ErrorEvent.class,
                orderId,
                20
        );

        assertNotNull(errorEvent);
        assertEquals(orderId, errorEvent.getOrderId());
        assertEquals("Invalid amount", errorEvent.getErrorReason());
        assertNotNull(errorEvent.getOriginalPayload());
        assertNotNull(errorEvent.getFailedAt());
    }
}
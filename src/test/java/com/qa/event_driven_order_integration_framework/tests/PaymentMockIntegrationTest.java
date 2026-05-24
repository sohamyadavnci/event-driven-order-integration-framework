package com.qa.event_driven_order_integration_framework.tests;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.qa.event_driven_order_integration_framework.base.BaseIntegrationTest;
import com.qa.event_driven_order_integration_framework.model.ErrorEvent;
import com.qa.event_driven_order_integration_framework.model.OrderEvent;
import com.qa.event_driven_order_integration_framework.model.ProcessedOrderEvent;
import com.qa.event_driven_order_integration_framework.utils.KafkaTestConsumerUtil;
import com.qa.event_driven_order_integration_framework.utils.KafkaTestProducerUtil;

public class PaymentMockIntegrationTest extends BaseIntegrationTest {

    @Value("${topics.order-events}")
    private String orderEventsTopic;

    @Value("${topics.processed-orders}")
    private String processedOrdersTopic;

    @Value("${topics.order-dlq}")
    private String orderDlqTopic;

    @Test
    void shouldProcessOrderWhenPaymentServiceApprovesPayment() {
        String orderId = "ORD-PAY-SUCCESS-" + System.currentTimeMillis();

        OrderEvent orderEvent = new OrderEvent(
                UUID.randomUUID().toString(),
                orderId,
                "CUST-901",
                120.50,
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

        ProcessedOrderEvent processedOrderEvent =
                KafkaTestConsumerUtil.consumeMessageByKey(
                        getKafkaBootstrapServers(),
                        processedOrdersTopic,
                        ProcessedOrderEvent.class,
                        orderId,
                        20
                );

        assertNotNull(processedOrderEvent);
        assertEquals(orderId, processedOrderEvent.getOrderId());
        assertEquals("PROCESSED", processedOrderEvent.getStatus());
        assertEquals("Order processed successfully after payment approval",
                processedOrderEvent.getMessage());

        WIREMOCK_SERVER.verify(postRequestedFor(urlEqualTo("/payments/authorize")));
    }

    @Test
    void shouldRouteOrderToDlqWhenPaymentServiceFails() {
        String orderId = "ORD-PAY-FAIL-" + System.currentTimeMillis();

        WIREMOCK_SERVER.stubFor(post(urlEqualTo("/payments/authorize"))
                .willReturn(serverError()
                        .withBody("Payment provider unavailable")));

        OrderEvent orderEvent = new OrderEvent(
                UUID.randomUUID().toString(),
                orderId,
                "CUST-902",
                300.00,
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

        ErrorEvent errorEvent =
                KafkaTestConsumerUtil.consumeMessageByKey(
                        getKafkaBootstrapServers(),
                        orderDlqTopic,
                        ErrorEvent.class,
                        orderId,
                        20
                );

        assertNotNull(errorEvent);
        assertEquals(orderId, errorEvent.getOrderId());
        assertTrue(errorEvent.getErrorReason().contains("Payment service failed"));
        assertNotNull(errorEvent.getOriginalPayload());
        assertNotNull(errorEvent.getFailedAt());

        WIREMOCK_SERVER.verify(postRequestedFor(urlEqualTo("/payments/authorize")));
    }
}
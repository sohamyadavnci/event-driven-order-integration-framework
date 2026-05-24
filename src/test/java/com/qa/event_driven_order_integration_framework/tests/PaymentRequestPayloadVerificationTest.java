package com.qa.event_driven_order_integration_framework.tests;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.qa.event_driven_order_integration_framework.base.BaseIntegrationTest;
import com.qa.event_driven_order_integration_framework.model.OrderEvent;
import com.qa.event_driven_order_integration_framework.model.ProcessedOrderEvent;
import com.qa.event_driven_order_integration_framework.utils.KafkaTestConsumerUtil;
import com.qa.event_driven_order_integration_framework.utils.KafkaTestProducerUtil;

public class PaymentRequestPayloadVerificationTest extends BaseIntegrationTest {

    @Value("${topics.order-events}")
    private String orderEventsTopic;

    @Value("${topics.processed-orders}")
    private String processedOrdersTopic;

    @Test
    void shouldSendCorrectPayloadToPaymentService() {
        String orderId = "ORD-PAYLOAD-CHECK-" + System.currentTimeMillis();

        OrderEvent orderEvent = new OrderEvent(
                UUID.randomUUID().toString(),
                orderId,
                "CUST-PAY-101",
                499.99,
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

        WIREMOCK_SERVER.verify(postRequestedFor(urlEqualTo("/payments/authorize"))
                .withRequestBody(matchingJsonPath("$.orderId", equalTo(orderId)))
                .withRequestBody(matchingJsonPath("$.customerId", equalTo("CUST-PAY-101")))
                .withRequestBody(matchingJsonPath("$.currency", equalTo("EUR"))));
    }
}
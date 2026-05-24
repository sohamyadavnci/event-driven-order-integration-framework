package com.qa.event_driven_order_integration_framework.tests;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import com.qa.event_driven_order_integration_framework.base.BaseIntegrationTest;
import com.qa.event_driven_order_integration_framework.model.OrderEvent;
import com.qa.event_driven_order_integration_framework.utils.KafkaTestConsumerUtil;

import static io.restassured.RestAssured.given;

public class OrderEventPayloadValidationTest extends BaseIntegrationTest {

    @Value("${topics.order-events}")
    private String orderEventsTopic;

    @Test
    void shouldPublishCorrectOrderEventPayloadToKafka() {
        String orderId = "ORD-PAYLOAD-" + System.currentTimeMillis();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", orderId);
        requestBody.put("customerId", "CUST-PAYLOAD-101");
        requestBody.put("amount", 350.75);
        requestBody.put("currency", "EUR");

        given()
                .contentType("application/json")
                .body(requestBody)
        .when()
                .post("/orders")
        .then()
                .statusCode(202);

        OrderEvent orderEvent = KafkaTestConsumerUtil.consumeMessageByKey(
                getKafkaBootstrapServers(),
                orderEventsTopic,
                OrderEvent.class,
                orderId,
                20
        );

        assertNotNull(orderEvent);
        assertNotNull(orderEvent.getEventId());
        assertEquals(orderId, orderEvent.getOrderId());
        assertEquals("CUST-PAYLOAD-101", orderEvent.getCustomerId());
        assertEquals(350.75, orderEvent.getAmount());
        assertEquals("EUR", orderEvent.getCurrency());
        assertEquals("ORDER_CREATED", orderEvent.getEventType());
        assertNotNull(orderEvent.getCreatedAt());
    }
}
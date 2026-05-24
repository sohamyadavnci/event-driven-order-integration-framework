package com.qa.event_driven_order_integration_framework.tests;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import com.qa.event_driven_order_integration_framework.base.BaseIntegrationTest;
import com.qa.event_driven_order_integration_framework.model.ProcessedOrderEvent;
import com.qa.event_driven_order_integration_framework.utils.KafkaTestConsumerUtil;

import static io.restassured.RestAssured.given;

public class ApiToKafkaFlowTest extends BaseIntegrationTest {

    @Value("${topics.processed-orders}")
    private String processedOrdersTopic;

    @Test
    void shouldPublishOrderEventAndProcessSuccessfully() {
        String orderId = "ORD-" + System.currentTimeMillis();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", orderId);
        requestBody.put("customerId", "CUST-501");
        requestBody.put("amount", 250.75);
        requestBody.put("currency", "EUR");

        given()
                .contentType("application/json")
                .body(requestBody)
        .when()
                .post("/orders")
        .then()
                .statusCode(202);

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
        assertEquals("Order processed successfully after payment approval", processedOrderEvent.getMessage());
        assertNotNull(processedOrderEvent.getProcessedAt());
    }
}
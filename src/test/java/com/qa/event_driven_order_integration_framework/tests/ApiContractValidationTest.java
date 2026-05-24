package com.qa.event_driven_order_integration_framework.tests;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;

import com.qa.event_driven_order_integration_framework.base.BaseIntegrationTest;

import static io.restassured.RestAssured.given;

public class ApiContractValidationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnAcceptedForValidOrderRequest() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", "ORD-API-" + System.currentTimeMillis());
        requestBody.put("customerId", "CUST-API-101");
        requestBody.put("amount", 200.00);
        requestBody.put("currency", "EUR");

        given()
                .contentType("application/json")
                .body(requestBody)
        .when()
                .post("/orders")
        .then()
                .statusCode(202)
                .body("status", equalTo("ACCEPTED"));
    }

    @Test
    void shouldReturnBadRequestForInvalidOrderRequest() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", "");
        requestBody.put("customerId", "CUST-API-102");
        requestBody.put("amount", -50.00);
        requestBody.put("currency", "EUR");

        given()
                .contentType("application/json")
                .body(requestBody)
        .when()
                .post("/orders")
        .then()
                .statusCode(400)
                .body("status", equalTo("REJECTED"))
                .body("message", equalTo("Invalid order request"));
    }
}
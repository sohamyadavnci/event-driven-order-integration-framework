package com.qa.event_driven_order_integration_framework.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.qa.event_driven_order_integration_framework.model.OrderEvent;
import com.qa.event_driven_order_integration_framework.model.PaymentResponse;

@Service
public class PaymentClientService {

    private final RestTemplate restTemplate;
    private final String paymentServiceBaseUrl;

    public PaymentClientService(RestTemplate restTemplate,
                                @Value("${payment.service.base-url}") String paymentServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.paymentServiceBaseUrl = paymentServiceBaseUrl;
    }

    public boolean authorizePayment(OrderEvent orderEvent) {
        String paymentUrl = paymentServiceBaseUrl + "/payments/authorize";

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderId", orderEvent.getOrderId());
        paymentRequest.put("customerId", orderEvent.getCustomerId());
        paymentRequest.put("amount", orderEvent.getAmount());
        paymentRequest.put("currency", orderEvent.getCurrency());

        try {
            ResponseEntity<PaymentResponse> response =
                    restTemplate.postForEntity(paymentUrl, paymentRequest, PaymentResponse.class);

            PaymentResponse paymentResponse = response.getBody();

            return response.getStatusCode().is2xxSuccessful()
                    && paymentResponse != null
                    && "APPROVED".equalsIgnoreCase(paymentResponse.getPaymentStatus());

        } catch (Exception exception) {
            throw new RuntimeException("Payment service failed: " + exception.getMessage());
        }
    }
}
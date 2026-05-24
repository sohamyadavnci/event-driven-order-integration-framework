package com.qa.event_driven_order_integration_framework.service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.qa.event_driven_order_integration_framework.model.OrderEvent;
import com.qa.event_driven_order_integration_framework.model.ProcessedOrderEvent;

@Service
public class OrderProcessingService {

    private final Set<String> processedOrderIds = ConcurrentHashMap.newKeySet();
    private final PaymentClientService paymentClientService;

    public OrderProcessingService(PaymentClientService paymentClientService) {
        this.paymentClientService = paymentClientService;
    }

    public Optional<ProcessedOrderEvent> processOrder(OrderEvent orderEvent) {

        boolean isFirstTimeProcessing = processedOrderIds.add(orderEvent.getOrderId());

        if (!isFirstTimeProcessing) {
            System.out.println("Duplicate order event detected. Skipping orderId: "
                    + orderEvent.getOrderId());

            return Optional.empty();
        }

        boolean paymentApproved = paymentClientService.authorizePayment(orderEvent);

        if (!paymentApproved) {
            throw new RuntimeException("Payment was not approved for orderId: "
                    + orderEvent.getOrderId());
        }

        ProcessedOrderEvent processedOrderEvent = new ProcessedOrderEvent(
                UUID.randomUUID().toString(),
                orderEvent.getOrderId(),
                "PROCESSED",
                "Order processed successfully after payment approval",
                Instant.now()
        );

        return Optional.of(processedOrderEvent);
    }
}
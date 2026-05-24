package com.qa.event_driven_order_integration_framework.service;

import org.springframework.stereotype.Service;

import com.qa.event_driven_order_integration_framework.model.OrderEvent;

@Service
public class OrderEventValidationService {

    public boolean isValid(OrderEvent orderEvent) {
        if (orderEvent == null) {
            return false;
        }

        if (orderEvent.getEventId() == null || orderEvent.getEventId().isBlank()) {
            return false;
        }

        if (orderEvent.getOrderId() == null || orderEvent.getOrderId().isBlank()) {
            return false;
        }

        if (orderEvent.getCustomerId() == null || orderEvent.getCustomerId().isBlank()) {
            return false;
        }

        if (orderEvent.getAmount() <= 0) {
            return false;
        }

        if (orderEvent.getCurrency() == null || orderEvent.getCurrency().isBlank()) {
            return false;
        }

        if (orderEvent.getEventType() == null || orderEvent.getEventType().isBlank()) {
            return false;
        }

        return true;
    }

    public String getValidationError(OrderEvent orderEvent) {
        if (orderEvent == null) {
            return "Order event is null";
        }

        if (orderEvent.getEventId() == null || orderEvent.getEventId().isBlank()) {
            return "Missing eventId";
        }

        if (orderEvent.getOrderId() == null || orderEvent.getOrderId().isBlank()) {
            return "Missing orderId";
        }

        if (orderEvent.getCustomerId() == null || orderEvent.getCustomerId().isBlank()) {
            return "Missing customerId";
        }

        if (orderEvent.getAmount() <= 0) {
            return "Invalid amount";
        }

        if (orderEvent.getCurrency() == null || orderEvent.getCurrency().isBlank()) {
            return "Missing currency";
        }

        if (orderEvent.getEventType() == null || orderEvent.getEventType().isBlank()) {
            return "Missing eventType";
        }

        return "Unknown validation error";
    }
}
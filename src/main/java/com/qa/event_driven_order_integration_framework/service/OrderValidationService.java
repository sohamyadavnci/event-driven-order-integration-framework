package com.qa.event_driven_order_integration_framework.service;

import org.springframework.stereotype.Service;

import com.qa.event_driven_order_integration_framework.model.OrderRequest;

@Service
public class OrderValidationService {

    public boolean isValid(OrderRequest request) {
        if (request == null) {
            return false;
        }

        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            return false;
        }

        if (request.getCustomerId() == null || request.getCustomerId().isBlank()) {
            return false;
        }

        if (request.getAmount() <= 0) {
            return false;
        }

        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            return false;
        }

        return true;
    }
}
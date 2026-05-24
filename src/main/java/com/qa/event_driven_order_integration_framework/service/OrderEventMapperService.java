package com.qa.event_driven_order_integration_framework.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.qa.event_driven_order_integration_framework.model.OrderEvent;
import com.qa.event_driven_order_integration_framework.model.OrderRequest;

@Service
public class OrderEventMapperService {

    public OrderEvent mapToOrderEvent(OrderRequest request) {
        return new OrderEvent(
                UUID.randomUUID().toString(),
                request.getOrderId(),
                request.getCustomerId(),
                request.getAmount(),
                request.getCurrency(),
                "ORDER_CREATED",
                Instant.now()
        );
    }
}
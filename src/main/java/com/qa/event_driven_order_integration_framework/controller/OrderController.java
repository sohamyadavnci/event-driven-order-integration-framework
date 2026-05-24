package com.qa.event_driven_order_integration_framework.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qa.event_driven_order_integration_framework.model.OrderEvent;
import com.qa.event_driven_order_integration_framework.model.OrderRequest;
import com.qa.event_driven_order_integration_framework.model.OrderResponse;
import com.qa.event_driven_order_integration_framework.producer.OrderEventProducer;
import com.qa.event_driven_order_integration_framework.service.OrderEventMapperService;
import com.qa.event_driven_order_integration_framework.service.OrderValidationService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderValidationService orderValidationService;
    private final OrderEventMapperService orderEventMapperService;
    private final OrderEventProducer orderEventProducer;

    public OrderController(OrderValidationService orderValidationService,
                           OrderEventMapperService orderEventMapperService,
                           OrderEventProducer orderEventProducer) {
        this.orderValidationService = orderValidationService;
        this.orderEventMapperService = orderEventMapperService;
        this.orderEventProducer = orderEventProducer;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {

        if (!orderValidationService.isValid(request)) {
            OrderResponse errorResponse = new OrderResponse(
                    request != null ? request.getOrderId() : null,
                    "REJECTED",
                    "Invalid order request"
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        OrderEvent orderEvent = orderEventMapperService.mapToOrderEvent(request);
        orderEventProducer.publishOrderEvent(orderEvent);

        OrderResponse response = new OrderResponse(
                request.getOrderId(),
                "ACCEPTED",
                "Order request accepted and published for asynchronous processing"
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
package com.qa.event_driven_order_integration_framework.consumer;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.event_driven_order_integration_framework.model.ErrorEvent;
import com.qa.event_driven_order_integration_framework.model.OrderEvent;
import com.qa.event_driven_order_integration_framework.model.ProcessedOrderEvent;
import com.qa.event_driven_order_integration_framework.producer.DlqProducer;
import com.qa.event_driven_order_integration_framework.producer.ProcessedOrderProducer;
import com.qa.event_driven_order_integration_framework.service.OrderEventValidationService;
import com.qa.event_driven_order_integration_framework.service.OrderProcessingService;

@Component
public class OrderEventConsumer {

    private final OrderProcessingService orderProcessingService;
    private final OrderEventValidationService orderEventValidationService;
    private final ProcessedOrderProducer processedOrderProducer;
    private final DlqProducer dlqProducer;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(OrderProcessingService orderProcessingService,
                              OrderEventValidationService orderEventValidationService,
                              ProcessedOrderProducer processedOrderProducer,
                              DlqProducer dlqProducer,
                              ObjectMapper objectMapper) {
        this.orderProcessingService = orderProcessingService;
        this.orderEventValidationService = orderEventValidationService;
        this.processedOrderProducer = processedOrderProducer;
        this.dlqProducer = dlqProducer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${topics.order-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrderEvent(OrderEvent orderEvent) {
        try {
            System.out.println("Consumed order event for orderId: " +
                    (orderEvent != null ? orderEvent.getOrderId() : "null"));

            if (!orderEventValidationService.isValid(orderEvent)) {
                String validationError = orderEventValidationService.getValidationError(orderEvent);
                publishFailureToDlq(orderEvent, validationError);
                return;
            }

            Optional<ProcessedOrderEvent> processedOrderEvent =
                    orderProcessingService.processOrder(orderEvent);

            if (processedOrderEvent.isPresent()) {
                processedOrderProducer.publishProcessedOrder(processedOrderEvent.get());

                System.out.println("Published processed order event for orderId: " +
                        processedOrderEvent.get().getOrderId());
            } else {
                System.out.println("Duplicate event skipped for orderId: " +
                        orderEvent.getOrderId());
            }

        } catch (Exception exception) {
            publishFailureToDlq(orderEvent, exception.getMessage());
        }
    }

    private void publishFailureToDlq(OrderEvent orderEvent, String errorReason) {
        String orderId = orderEvent != null ? orderEvent.getOrderId() : "UNKNOWN";
        String originalPayload = convertToJson(orderEvent);

        ErrorEvent errorEvent = new ErrorEvent(
                UUID.randomUUID().toString(),
                orderId,
                errorReason,
                originalPayload,
                Instant.now()
        );

        dlqProducer.publishToDlq(errorEvent);

        System.out.println("Published failed order event to DLQ. orderId: "
                + orderId + ", reason: " + errorReason);
    }

    private String convertToJson(OrderEvent orderEvent) {
        try {
            return objectMapper.writeValueAsString(orderEvent);
        } catch (JsonProcessingException e) {
            return "Unable to convert original event to JSON";
        }
    }
}
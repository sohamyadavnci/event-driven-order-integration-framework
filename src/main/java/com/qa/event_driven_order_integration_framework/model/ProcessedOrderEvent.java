package com.qa.event_driven_order_integration_framework.model;

import java.time.Instant;

public class ProcessedOrderEvent {

    private String eventId;
    private String orderId;
    private String status;
    private String message;
    private Instant processedAt;

    public ProcessedOrderEvent() {
    }

    public ProcessedOrderEvent(String eventId, String orderId, String status,
                               String message, Instant processedAt) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.processedAt = processedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
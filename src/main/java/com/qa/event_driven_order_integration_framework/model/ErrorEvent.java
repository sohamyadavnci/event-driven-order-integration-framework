package com.qa.event_driven_order_integration_framework.model;

import java.time.Instant;

public class ErrorEvent {

    private String eventId;
    private String orderId;
    private String errorReason;
    private String originalPayload;
    private Instant failedAt;

    public ErrorEvent() {
    }

    public ErrorEvent(String eventId, String orderId, String errorReason,
                      String originalPayload, Instant failedAt) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.errorReason = errorReason;
        this.originalPayload = originalPayload;
        this.failedAt = failedAt;
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

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    public String getOriginalPayload() {
        return originalPayload;
    }

    public void setOriginalPayload(String originalPayload) {
        this.originalPayload = originalPayload;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }
}
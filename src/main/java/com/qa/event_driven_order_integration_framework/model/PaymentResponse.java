package com.qa.event_driven_order_integration_framework.model;

public class PaymentResponse {

    private String orderId;
    private String paymentStatus;
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(String orderId, String paymentStatus, String message) {
        this.orderId = orderId;
        this.paymentStatus = paymentStatus;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
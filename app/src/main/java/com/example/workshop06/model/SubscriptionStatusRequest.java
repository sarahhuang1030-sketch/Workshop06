package com.example.workshop06.model;

public class SubscriptionStatusRequest {
    private String status;

    public SubscriptionStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
package com.example.workshop06.model;

public class ServiceRequestResponse {
    private Integer requestId;
    private Integer customerId;
    private Integer createdByUserId;
    private Integer assignedTechnicianUserId;
    private String requestType;
    private String status;
    private String description;
    private String createdAt;
    private String priority;
    private String customerName;
    private String createdByName;
    private String technicianName;
    private Integer addressId;
    private String addressText;

    public Integer getRequestId() {
        return requestId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public Integer getCreatedByUserId() {
        return createdByUserId;
    }

    public Integer getAssignedTechnicianUserId() {
        return assignedTechnicianUserId;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPriority() {
        return priority;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public String getAddressText() {
        return addressText;
    }
}
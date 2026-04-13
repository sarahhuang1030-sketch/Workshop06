package com.example.workshop06.model;

import com.google.gson.annotations.SerializedName;

public class ServiceTicketDTO {

    @SerializedName("requestId")
    private Integer requestId;

    @SerializedName("customerId")
    private Integer customerId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("requestType")
    private String requestType;

    @SerializedName("priority")
    private String priority;

    @SerializedName("status")
    private String status;

    @SerializedName("description")
    private String description;

    @SerializedName("technicianUserId")
    private Integer technicianUserId;

    @SerializedName("technicianName")
    private String technicianName;

    // ===== GETTERS =====

    public Integer getRequestId() {
        return requestId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public Integer getTechnicianUserId() {
        return technicianUserId;
    }

    public String getTechnicianName() {
        return technicianName;
    }
}
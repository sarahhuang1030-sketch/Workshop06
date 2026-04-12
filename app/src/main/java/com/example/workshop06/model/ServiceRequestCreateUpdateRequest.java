package com.example.workshop06.model;

public class ServiceRequestCreateUpdateRequest {
    private Integer customerId;
    private Integer createdByUserId;
    private Integer assignedTechnicianUserId;
    private Integer parentRequestId;
    private String requestType;
    private String priority;
    private String status;
    private String description;

    public ServiceRequestCreateUpdateRequest(Integer customerId,
                                             Integer createdByUserId,
                                             Integer assignedTechnicianUserId,
                                             String requestType,
                                             String priority,
                                             String status,
                                             String description) {
        this.customerId = customerId;
        this.createdByUserId = createdByUserId;
        this.assignedTechnicianUserId = assignedTechnicianUserId;
        this.requestType = requestType;
        this.priority = priority;
        this.status = status;
        this.description = description;
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

    public Integer getParentRequestId() {
        return parentRequestId;
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
}
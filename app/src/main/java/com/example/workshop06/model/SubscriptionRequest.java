package com.example.workshop06.model;

public class SubscriptionRequest {
    private Integer subscriptionId;
    private Integer customerId;
    private Integer planId;
    private String startDate;
    private String endDate;
    private String status;
    private Integer billingCycleDay;
    private String notes;

    public SubscriptionRequest(Integer subscriptionId,
                               Integer customerId,
                               Integer planId,
                               String startDate,
                               String endDate,
                               String status,
                               Integer billingCycleDay,
                               String notes) {
        this.subscriptionId = subscriptionId;
        this.customerId = customerId;
        this.planId = planId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.billingCycleDay = billingCycleDay;
        this.notes = notes;
    }

    public Integer getSubscriptionId() { return subscriptionId; }
    public Integer getCustomerId() { return customerId; }
    public Integer getPlanId() { return planId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public Integer getBillingCycleDay() { return billingCycleDay; }
    public String getNotes() { return notes; }
}
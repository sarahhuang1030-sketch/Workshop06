package com.example.workshop06.model;

import java.util.List;

public class SubscriptionResponse {
    private Integer subscriptionId;
    private Integer customerId;
    private Integer planId;
    private String startDate;
    private String endDate;
    private String status;
    private Integer billingCycleDay;
    private String notes;
    private String customerName;
    private String planName;

    public String getPlanName() {
        return planName;
    }

    public String getCustomerName() {
        return customerName;
    }

    private List<SubscriptionAddOnResponse> addons;

    public Integer getSubscriptionId() { return subscriptionId; }
    public Integer getCustomerId() { return customerId; }
    public Integer getPlanId() { return planId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public Integer getBillingCycleDay() { return billingCycleDay; }
    public String getNotes() { return notes; }
    public List<SubscriptionAddOnResponse> getAddons() { return addons; }
}
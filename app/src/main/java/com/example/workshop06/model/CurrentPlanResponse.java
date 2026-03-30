package com.example.workshop06.model;

public class CurrentPlanResponse {
    private Integer subscriptionId;
    private Integer customerId;
    private Integer planId;
    private String status;
    private String planName;
    private Double monthlyPrice;
    private Double addonTotal;
    private Double totalMonthlyPrice;

    public Integer getSubscriptionId() {
        return subscriptionId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public Integer getPlanId() {
        return planId;
    }

    public String getStatus() {
        return status;
    }

    public String getPlanName() {
        return planName;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public Double getAddonTotal() {
        return addonTotal;
    }

    public Double getTotalMonthlyPrice() {
        return totalMonthlyPrice;
    }
}
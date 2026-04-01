package com.example.workshop06.model;

public class CurrentPlanItemResponse {
    private Integer subscriptionId;
    private Integer planId;
    private String planName;
    private Double monthlyPrice;
    private Double addonTotal;
    private Double totalMonthlyPrice;
    private String startDate;

    public CurrentPlanItemResponse() {
    }

    public Integer getSubscriptionId() {
        return subscriptionId;
    }

    public Integer getPlanId() {
        return planId;
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

    public String getStartDate() {
        return startDate;
    }
}
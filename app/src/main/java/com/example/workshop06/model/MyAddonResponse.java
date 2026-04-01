package com.example.workshop06.model;

public class MyAddonResponse {
    private Integer subscriptionAddOnId;
    private Integer subscriptionId;
    private Integer addOnId;
    private String addOnName;
    private String description;
    private Double monthlyPrice;
    private String status;
    private String startDate;
    private String endDate;

    public Integer getSubscriptionAddOnId() {
        return subscriptionAddOnId;
    }

    public Integer getSubscriptionId() {
        return subscriptionId;
    }

    public Integer getAddOnId() {
        return addOnId;
    }

    public String getAddOnName() {
        return addOnName;
    }

    public String getDescription() {
        return description;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public String getStatus() {
        return status;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
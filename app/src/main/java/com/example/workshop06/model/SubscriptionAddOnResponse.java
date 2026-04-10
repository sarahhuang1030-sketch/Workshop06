package com.example.workshop06.model;

public class SubscriptionAddOnResponse {
    private Integer subscriptionAddOnId;
    private Integer addOnId;
    private String addOnName;
    private String startDate;
    private String endDate;
    private String status;

    // ADD THIS
    private Double price;

    public Integer getSubscriptionAddOnId() { return subscriptionAddOnId; }
    public Integer getAddOnId() { return addOnId; }
    public String getAddOnName() { return addOnName; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public Double getPrice() { return price; }
}
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
    private Double monthlyPrice;
    private List<SubscriptionAddOnResponse> addons;

    public Integer getSubscriptionId() { return subscriptionId; }
    public Integer getCustomerId() { return customerId; }
    public Integer getPlanId() { return planId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public Integer getBillingCycleDay() { return billingCycleDay; }
    public String getNotes() { return notes; }
    public String getCustomerName() { return customerName; }
    public String getPlanName() { return planName; }
    public Double getMonthlyPrice() { return monthlyPrice; }
    public List<SubscriptionAddOnResponse> getAddons() { return addons; }

    public double getTotalAmount() {
        double total = monthlyPrice != null ? monthlyPrice : 0.0;

        if (addons != null) {
            for (SubscriptionAddOnResponse addOn : addons) {
                if (addOn == null) continue;

                Double price = addOn.getPrice();
                String addOnStatus = addOn.getStatus();

                boolean includeInTotal =
                        price != null &&
                                (addOnStatus == null
                                        || addOnStatus.trim().isEmpty()
                                        || addOnStatus.equalsIgnoreCase("Active"));

                if (includeInTotal) {
                    total += price;
                }
            }
        }

        return total;
    }
}
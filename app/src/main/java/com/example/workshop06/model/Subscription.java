package com.example.workshop06.model;

public class Subscription {

    private Integer subscriptionId;
    private Integer customerId;
    private Integer planId;
    private String startDate;
    private String endDate;
    private String status;
    private Integer billingCycleDay;
    private String notes;
    private Integer soldByEmployeeId;

    public Integer getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Integer subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getBillingCycleDay() {
        return billingCycleDay;
    }

    public void setBillingCycleDay(Integer billingCycleDay) {
        this.billingCycleDay = billingCycleDay;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getSoldByEmployeeId() {
        return soldByEmployeeId;
    }

    public void setSoldByEmployeeId(Integer soldByEmployeeId) {
        this.soldByEmployeeId = soldByEmployeeId;
    }
}
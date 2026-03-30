package com.example.workshop06.model;

public class SubscriptionAddon {

    private Integer subscriptionAddonId;
    private Integer subscriptionId;
    private Integer addonId;
    private String startDate;
    private String endDate;
    private String status;

    public Integer getSubscriptionAddonId() {
        return subscriptionAddonId;
    }

    public void setSubscriptionAddonId(Integer subscriptionAddonId) {
        this.subscriptionAddonId = subscriptionAddonId;
    }

    public Integer getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Integer subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Integer getAddonId() {
        return addonId;
    }

    public void setAddonId(Integer addonId) {
        this.addonId = addonId;
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
}
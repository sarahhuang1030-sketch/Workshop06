package com.example.workshop06.model;

import com.google.gson.annotations.SerializedName;

public class ManagerSummaryResponse {

    // ✅ Match backend field names exactly
    @SerializedName("customers")
    private Integer customers;

    @SerializedName("activeSubs")
    private Integer activeSubs;

    @SerializedName("monthlyRevenue")
    private Double monthlyRevenue;

    @SerializedName("pastDue")
    private Integer pastDue;

    @SerializedName("addOns")
    private Long addOns;

    @SerializedName("planFeatures")
    private Long planFeatures;

    @SerializedName("location")
    private Long location;

    @SerializedName("serviceRequests")
    private Long serviceRequests;

    @SerializedName("serviceAppointments")
    private Long serviceAppointments;

    // ❗ KEEP THIS (as you requested)
    @SerializedName("totalEmployees")
    private Long totalEmployees;

    // ================= GETTERS =================

    public int getCustomers() {
        return customers != null ? customers : 0;
    }

    public int getActiveSubscriptions() {
        return activeSubs != null ? activeSubs : 0;
    }

    public double getEstimatedMonthlyRevenue() {
        return monthlyRevenue != null ? monthlyRevenue : 0.0;
    }

    public int getPastDue() {
        return pastDue != null ? pastDue : 0;
    }

    public long getTotalAddons() {
        return addOns != null ? addOns : 0L;
    }

    public long getTotalPlanFeatures() {
        return planFeatures != null ? planFeatures : 0L;
    }

    public long getTotalLocations() {
        return location != null ? location : 0L;
    }

    public long getServiceRequests() {
        return serviceRequests != null ? serviceRequests : 0L;
    }

    public long getServiceAppointments() {
        return serviceAppointments != null ? serviceAppointments : 0L;
    }

    // ❗ KEEP EXACTLY
    public long getTotalEmployees() {
        return totalEmployees != null ? totalEmployees : 0L;
    }
}
package com.example.workshop06.model;

import com.google.gson.annotations.SerializedName;

public class ManagerSummaryResponse {


    @SerializedName("totalEmployees")
    private Long totalEmployees;

    @SerializedName("totalLocations")
    private Long totalLocations;

    @SerializedName("activeSubscriptions")
    private Long activeSubscriptions;

    @SerializedName("suspendedSubscriptions")
    private Long suspendedSubscriptions;

    @SerializedName("openInvoices")
    private Long openInvoices;

    @SerializedName("estimatedMonthlyRevenue")
    private Double estimatedMonthlyRevenue;

    @SerializedName("totalAddons")
    private Long totalAddons;

    @SerializedName("activeAddons")
    private Long activeAddons;

    @SerializedName("totalPlanFeatures")
    private Long totalPlanFeatures;

    public long getTotalLocations() {
        return totalLocations != null ? totalLocations : 0L;
    }

    public long getTotalEmployees() {
        return totalEmployees != null ? totalEmployees : 0L;
    }



    public long getActiveSubscriptions() {
        return activeSubscriptions != null ? activeSubscriptions : 0L;
    }

    public long getSuspendedSubscriptions() {
        return suspendedSubscriptions != null ? suspendedSubscriptions : 0L;
    }

    public long getOpenInvoices() {
        return openInvoices != null ? openInvoices : 0L;
    }

    public double getEstimatedMonthlyRevenue() {
        return estimatedMonthlyRevenue != null ? estimatedMonthlyRevenue : 0.0;
    }

    public long getTotalAddons() {
        return totalAddons != null ? totalAddons : 0L;
    }

    public long getActiveAddons() {
        return activeAddons != null ? activeAddons : 0L;
    }

    public long getTotalPlanFeatures() {
        return totalPlanFeatures != null ? totalPlanFeatures : 0L;
    }
}
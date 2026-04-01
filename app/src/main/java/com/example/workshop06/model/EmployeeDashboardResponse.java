package com.example.workshop06.model;

public class EmployeeDashboardResponse {
    private String firstName;
    private int activeBranches;
    private int availableAddons;
    private int activeSubscriptions;
    private int pendingInvoices;
    private int recentLogs;
    private int planFeatures;

    public String getFirstName() { return firstName; }
    public int getActiveBranches() { return activeBranches; }
    public int getAvailableAddons() { return availableAddons; }
    public int getActiveSubscriptions() { return activeSubscriptions; }
    public int getPendingInvoices() { return pendingInvoices; }
    public int getRecentLogs() { return recentLogs; }

    public int getPlanFeatures(){return planFeatures;}
}
package com.example.workshop06.model;

public class ServiceDashboardSummaryResponse {
    private long assignedRequests;
    private long openRequests;
    private long todayAppointments;
    private long completedRequests;

    public long getAssignedRequests() {
        return assignedRequests;
    }

    public long getOpenRequests() {
        return openRequests;
    }

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public long getCompletedRequests() {
        return completedRequests;
    }
}
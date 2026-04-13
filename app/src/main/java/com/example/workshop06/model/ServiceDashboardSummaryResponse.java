package com.example.workshop06.model;

public class ServiceDashboardSummaryResponse {
    private long assignedRequests;
    private long openRequests;
    private long todayAppointments;
    private long completedRequests;
    private long assignedAppointments;



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

    public long getAssignedAppointments() {
        return assignedAppointments;
    }
}
package com.example.workshop06.model;

public class TechnicianWorkOrderUpdateRequest {
    private String status;
    private String scheduledEnd;
    private String notes;

    public TechnicianWorkOrderUpdateRequest(String status, String scheduledEnd, String notes) {
        this.status = status;
        this.scheduledEnd = scheduledEnd;
        this.notes = notes;
    }

    public String getStatus() { return status; }
    public String getScheduledEnd() { return scheduledEnd; }
    public String getNotes() { return notes; }
}

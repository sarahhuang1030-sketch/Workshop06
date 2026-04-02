package com.example.workshop06.model;

public class ServiceAppointmentResponse {
    private Integer appointmentId;
    private Integer requestId;
    private Integer technicianUserId;
    private Integer addressId;
    private Integer locationId;
    private String locationType;
    private String scheduledStart;
    private String scheduledEnd;
    private String status;
    private String notes;
    private String technicianName;
    private String addressText;

    public Integer getAppointmentId() { return appointmentId; }
    public Integer getRequestId() { return requestId; }
    public Integer getTechnicianUserId() { return technicianUserId; }
    public Integer getAddressId() { return addressId; }
    public Integer getLocationId() { return locationId; }
    public String getLocationType() { return locationType; }
    public String getScheduledStart() { return scheduledStart; }
    public String getScheduledEnd() { return scheduledEnd; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getTechnicianName() { return technicianName; }
    public String getAddressText() { return addressText; }
}
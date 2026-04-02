package com.example.workshop06.model;

public class ServiceAppointmentCreateUpdateRequest {
    private Integer technicianUserId;
    private Integer addressId;
    private Integer locationId;
    private String locationType;
    private String scheduledStart;
    private String scheduledEnd;
    private String status;
    private String notes;

    public ServiceAppointmentCreateUpdateRequest(Integer technicianUserId,
                                                 Integer addressId,
                                                 Integer locationId,
                                                 String locationType,
                                                 String scheduledStart,
                                                 String scheduledEnd,
                                                 String status,
                                                 String notes) {
        this.technicianUserId = technicianUserId;
        this.addressId = addressId;
        this.locationId = locationId;
        this.locationType = locationType;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.status = status;
        this.notes = notes;
    }

    public Integer getTechnicianUserId() { return technicianUserId; }
    public Integer getAddressId() { return addressId; }
    public Integer getLocationId() { return locationId; }
    public String getLocationType() { return locationType; }
    public String getScheduledStart() { return scheduledStart; }
    public String getScheduledEnd() { return scheduledEnd; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
}
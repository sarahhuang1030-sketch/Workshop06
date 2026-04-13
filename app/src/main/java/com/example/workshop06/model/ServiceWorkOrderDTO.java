package com.example.workshop06.model;

import com.google.gson.annotations.SerializedName;

public class ServiceWorkOrderDTO {

    @SerializedName("appointmentId")
    private Integer appointmentId;

    @SerializedName("requestId")
    private Integer requestId;

    @SerializedName("technicianUserId")
    private Integer technicianUserId;

    @SerializedName("technicianName")
    private String technicianName;

    @SerializedName("scheduledStart")
    private String scheduledStart;

    @SerializedName("scheduledEnd")
    private String scheduledEnd;

    @SerializedName("status")
    private String status;

    @SerializedName("addressText")
    private String addressText;

    @SerializedName("locationType")
    private String locationType;

    @SerializedName("notes")
    private String notes;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("requestType")
    private String requestType;

    @SerializedName("requestDescription")
    private String requestDescription;

    @SerializedName("priority")
    private String priority;

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public Integer getTechnicianUserId() {
        return technicianUserId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public String getScheduledStart() {
        return scheduledStart;
    }

    public String getScheduledEnd() {
        return scheduledEnd;
    }

    public String getStatus() {
        return status;
    }

    public String getAddressText() {
        return addressText;
    }

    public String getLocationType() {
        return locationType;
    }

    public String getNotes() {
        return notes;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public String getPriority() {
        return priority;
    }
}
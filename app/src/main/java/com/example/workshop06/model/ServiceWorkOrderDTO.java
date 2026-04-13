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

    // ===== GETTERS =====

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
}
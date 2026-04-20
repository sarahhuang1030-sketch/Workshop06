package com.example.workshop06.model;

import com.google.gson.annotations.SerializedName;

/**
 * DTO returned by GET /api/service/tickets (technician path).
 *
 * FIX: Added addressText field.
 *      The original class declared getAddressText() with an empty method body
 *      and no backing field, so Gson could never store the value and the method
 *      always returned null — causing tvAddress to display "—" on every card.
 */
public class ServiceTicketDTO {

    @SerializedName("requestId")
    private Integer requestId;

    @SerializedName("customerId")
    private Integer customerId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("requestType")
    private String requestType;

    @SerializedName("priority")
    private String priority;

    @SerializedName("status")
    private String status;

    @SerializedName("description")
    private String description;

    @SerializedName("technicianUserId")
    private Integer technicianUserId;

    @SerializedName("technicianName")
    private String technicianName;

    // FIX: this field was completely missing.
    //      Without it Gson has nowhere to write the JSON value "addressText",
    //      and the empty getAddressText() method always returned null.
    @SerializedName("addressText")
    private String addressText;

    // =========================================================================
    // Getters
    // =========================================================================

    public Integer getRequestId()        { return requestId; }
    public Integer getCustomerId()       { return customerId; }
    public String  getCustomerName()     { return customerName; }
    public String  getRequestType()      { return requestType; }
    public String  getPriority()         { return priority; }
    public String  getStatus()           { return status; }
    public String  getDescription()      { return description; }
    public Integer getTechnicianUserId() { return technicianUserId; }
    public String  getTechnicianName()   { return technicianName; }

    // FIX: was an empty method body with no return statement.
    public String  getAddressText()      { return addressText; }

    // =========================================================================
    // Setters — required so the conversion loop in ServiceRequestListActivity
    // can copy values into ServiceRequestResponse objects
    // =========================================================================

    public void setRequestId(Integer v)        { this.requestId = v; }
    public void setCustomerId(Integer v)       { this.customerId = v; }
    public void setCustomerName(String v)      { this.customerName = v; }
    public void setRequestType(String v)       { this.requestType = v; }
    public void setPriority(String v)          { this.priority = v; }
    public void setStatus(String v)            { this.status = v; }
    public void setDescription(String v)       { this.description = v; }
    public void setTechnicianUserId(Integer v) { this.technicianUserId = v; }
    public void setTechnicianName(String v)    { this.technicianName = v; }
    public void setAddressText(String v)       { this.addressText = v; }
}
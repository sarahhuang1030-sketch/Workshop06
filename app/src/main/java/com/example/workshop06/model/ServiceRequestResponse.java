package com.example.workshop06.model;

/**
 * Android model for a service request returned by:
 *   - GET /api/manager/service-requests  (manager/employee path)
 *   - Converted from ServiceTicketDTO    (technician path)
 *
 * FIX: Added all missing setters so Gson can deserialize every JSON field.
 *      Gson populates fields by calling the matching setter. Without a setter
 *      the field is silently left null no matter what the server sends.
 *
 *      Missing setters in the original:
 *        - setAddressText()      ← KEY FIX: address always showed "—"
 *        - setAddressId()
 *        - setCreatedAt()
 *        - setCreatedByName()
 *        - setCreatedByUserId()
 */
public class ServiceRequestResponse {

    private Integer requestId;
    private Integer customerId;
    private Integer createdByUserId;
    private Integer assignedTechnicianUserId;
    private String  requestType;
    private String  status;
    private String  description;
    private String  createdAt;
    private String  priority;
    private String  customerName;
    private String  createdByName;
    private String  technicianName;
    private Integer addressId;
    private String  addressText;

     
    // Getters
     

    public Integer getRequestId()                { return requestId; }
    public Integer getCustomerId()               { return customerId; }
    public Integer getCreatedByUserId()          { return createdByUserId; }
    public Integer getAssignedTechnicianUserId() { return assignedTechnicianUserId; }
    public String  getRequestType()              { return requestType; }
    public String  getStatus()                   { return status; }
    public String  getDescription()              { return description; }
    public String  getCreatedAt()                { return createdAt; }
    public String  getPriority()                 { return priority; }
    public String  getCustomerName()             { return customerName; }
    public String  getCreatedByName()            { return createdByName; }
    public String  getTechnicianName()           { return technicianName; }
    public Integer getAddressId()                { return addressId; }
    public String  getAddressText()              { return addressText; }

     
    // Setters  — every field needs one for Gson deserialization
     

    public void setRequestId(Integer v)                { this.requestId = v; }
    public void setCustomerId(Integer v)               { this.customerId = v; }

    public void setCreatedByUserId(Integer v)          { this.createdByUserId = v; }

    public void setAssignedTechnicianUserId(Integer v) { this.assignedTechnicianUserId = v; }
    public void setRequestType(String v)               { this.requestType = v; }
    public void setStatus(String v)                    { this.status = v; }
    public void setDescription(String v)               { this.description = v; }

    public void setCreatedAt(String v)                 { this.createdAt = v; }

    public void setPriority(String v)                  { this.priority = v; }
    public void setCustomerName(String v)              { this.customerName = v; }

    public void setCreatedByName(String v)             { this.createdByName = v; }

    public void setTechnicianName(String v)            { this.technicianName = v; }

    public void setAddressId(Integer v)                { this.addressId = v; }
    
    public void setAddressText(String v)               { this.addressText = v; }
}
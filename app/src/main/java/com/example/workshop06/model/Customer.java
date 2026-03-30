package com.example.workshop06.model;

import java.time.LocalDateTime;

public class Customer {

    private Integer customerId;
    private String customerType;
    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String homePhone;
    private String status;
    private LocalDateTime createdAt;

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHomePhone() { return homePhone; }
    public void setHomePhone(String homePhone) { this.homePhone = homePhone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
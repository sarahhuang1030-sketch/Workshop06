package com.example.workshop06.model;

public class CustomerResponse {
    private Integer customerId;
    private String customerType;
    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String homePhone;
    private String status;
    private String createdAt;
    private String passwordHash;
    private String externalCustomerId;
    private String externalProvider;
    private Integer assignedEmployeeId;

    public Integer getCustomerId() {
        return customerId;
    }

    public String getCustomerType() {
        return customerType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getEmail() {
        return email;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getExternalCustomerId() {
        return externalCustomerId;
    }

    public String getExternalProvider() {
        return externalProvider;
    }

    public Integer getAssignedEmployeeId() {
        return assignedEmployeeId;
    }

    public String getFullName() {
        if (businessName != null && !businessName.trim().isEmpty()) {
            return businessName;
        }

        String fullName = ((safe(firstName) + " " + safe(lastName)).trim());
        return fullName.isEmpty() ? "Customer #" + customerId : fullName;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
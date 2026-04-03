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
    private String externalProvider;
    private String externalCustomerId;

    public Integer getCustomerId() { return customerId; }
    public String getCustomerType() { return customerType; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getBusinessName() { return businessName; }
    public String getEmail() { return email; }
    public String getHomePhone() { return homePhone; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getExternalProvider() { return externalProvider; }
    public String getExternalCustomerId() { return externalCustomerId; }
}
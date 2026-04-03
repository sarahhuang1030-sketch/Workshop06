package com.example.workshop06.model;

public class SaveCustomerRequest {
    private String customerType;
    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String homePhone;
    private String status;

    public SaveCustomerRequest(String customerType,
                               String firstName,
                               String lastName,
                               String businessName,
                               String email,
                               String homePhone,
                               String status) {
        this.customerType = customerType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.businessName = businessName;
        this.email = email;
        this.homePhone = homePhone;
        this.status = status;
    }

    public String getCustomerType() { return customerType; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getBusinessName() { return businessName; }
    public String getEmail() { return email; }
    public String getHomePhone() { return homePhone; }
    public String getStatus() { return status; }
}
package com.example.workshop06.model;

public class CreateCustomerResponse {
    private Integer customerId;
    private String firstName;
    private String lastName;
    private String username;
    private String role;
    private String tempPassword;

    public Integer getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getTempPassword() { return tempPassword; }
}
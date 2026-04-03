package com.example.workshop06.model;

public class CreateEmployeeResponse {
    private Integer employeeId;
    private String firstName;
    private String lastName;
    private String role;
    private String username;
    private String tempPassword;

    public Integer getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public String getUsername() { return username; }
    public String getTempPassword() { return tempPassword; }
}
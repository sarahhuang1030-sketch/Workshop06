package com.example.workshop06.model;

public class EmployeeResponse {
    private Integer employeeId;
    private Integer primaryLocationId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private Double salary;
    private String hireDate;
    private String status;
    private Integer active;
    private Integer managerId;

    public Integer getEmployeeId() { return employeeId; }
    public Integer getPrimaryLocationId() { return primaryLocationId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public Double getSalary() { return salary; }
    public String getHireDate() { return hireDate; }
    public String getStatus() { return status; }
    public Integer getActive() { return active; }
    public Integer getManagerId() { return managerId; }
}
package com.example.workshop06.model;

public class SaveEmployeeRequest {
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

    public SaveEmployeeRequest(Integer primaryLocationId,
                               String firstName,
                               String lastName,
                               String email,
                               String phone,
                               String role,
                               Double salary,
                               String hireDate,
                               String status,
//                               Integer active,
                               Integer managerId) {
        this.primaryLocationId = primaryLocationId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.salary = salary;
        this.hireDate = hireDate;
        this.status = status;
//        this.active = active;
        this.managerId = managerId;
    }

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
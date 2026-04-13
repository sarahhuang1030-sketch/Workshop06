package com.example.workshop06.model;

public class EmployeeResponse {
    private Integer userId;
    private Integer employeeId;
    private Integer primaryLocationId;
    private Integer reportsToEmployeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Double salary;
    private String hireDate;
    private String status;
    private Integer active;
    private Integer managerId;
    private Integer roleId;

    private String role;
    private String roleName;
    private String positionTitle;

    public Integer getUserId() {
        return userId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public Integer getPrimaryLocationId() {
        return primaryLocationId;
    }

    public Integer getReportsToEmployeeId() {
        return reportsToEmployeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        String fullName = (safe(firstName) + " " + safe(lastName)).trim();
        return fullName.isEmpty() ? "Employee #" + employeeId : fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Double getSalary() {
        return salary;
    }

    public String getHireDate() {
        return hireDate;
    }

    public String getStatus() {
        return status;
    }

    public Integer getActive() {
        return active;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public String getRole() {
        if (role != null && !role.trim().isEmpty()) return role;
        if (roleName != null && !roleName.trim().isEmpty()) return roleName;
        if (positionTitle != null && !positionTitle.trim().isEmpty()) return positionTitle;

        if (roleId == null) return "";
        switch (roleId) {
            case 1: return "Manager";
            case 2: return "Sales Agent";
            case 3: return "Service Technician";
            case 4: return "Customer";
            default: return "";
        }
    }

    public String getRoleName() {
        if (roleName != null && !roleName.trim().isEmpty()) return roleName;
        return getRole();
    }

    public String getPositionTitle() {
        if (positionTitle != null && !positionTitle.trim().isEmpty()) return positionTitle;
        return getRole();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
package com.example.workshop06.model;

public class MeResponse {
    private Integer userId;
    private Integer employeeId;
    private Integer customerId;

    private String userType;
    private String username;
    private String role;
    private String avatarUrl;

    private String firstName;
    private String lastName;
    private String email;

    private String homePhone;
    private String phone;

    private Integer primaryLocationId;
    private String locationName;
    private String status;
    private String hireDate;
    private Integer managerId;

    public Integer getUserId() { return userId; }
    public Integer getEmployeeId() { return employeeId; }
    public Integer getCustomerId() { return customerId; }

    public String getUserType() { return userType; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getAvatarUrl() { return avatarUrl; }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }

    public String getHomePhone() { return homePhone; }
    public String getPhone() { return phone; }

    public Integer getPrimaryLocationId() { return primaryLocationId; }
    public String getLocationName() { return locationName; }
    public String getStatus() { return status; }
    public String getHireDate() { return hireDate; }
    public Integer getManagerId() { return managerId; }

    public String getResolvedPhone() {
        if (phone != null && !phone.trim().isEmpty()) {
            return phone;
        }
        if (homePhone != null && !homePhone.trim().isEmpty()) {
            return homePhone;
        }
        return null;
    }

    public String getDisplayName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "User" : full;
    }
}
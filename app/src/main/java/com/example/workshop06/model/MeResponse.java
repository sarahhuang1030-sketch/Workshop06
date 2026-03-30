package com.example.workshop06.model;

public class MeResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String homePhone;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getHomePhone() { return homePhone; }

    public String getDisplayName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "User" : full;
    }
}
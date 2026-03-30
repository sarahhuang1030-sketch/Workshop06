package com.example.workshop06.model;

public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String homePhone;

    public UpdateProfileRequest(String firstName, String lastName, String email, String homePhone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.homePhone = homePhone;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getHomePhone() {
        return homePhone;
    }
}
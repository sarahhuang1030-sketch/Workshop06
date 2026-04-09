package com.example.workshop06.model;

public class FirstLoginPasswordChangeRequest {

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    public FirstLoginPasswordChangeRequest(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
}
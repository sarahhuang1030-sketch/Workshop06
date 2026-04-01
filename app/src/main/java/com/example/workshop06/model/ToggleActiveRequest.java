package com.example.workshop06.model;

public class ToggleActiveRequest {
    private Boolean isActive;

    public ToggleActiveRequest(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
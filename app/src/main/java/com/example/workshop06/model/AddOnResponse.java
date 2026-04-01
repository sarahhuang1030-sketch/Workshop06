package com.example.workshop06.model;

public class AddOnResponse {
    private Integer addOnId;
    private Integer serviceTypeId;
    private String addOnName;
    private Double monthlyPrice;
    private String description;
    private Boolean isActive;
    private String iconKey;
    private String themeKey;

    public Integer getAddOnId() { return addOnId; }
    public Integer getServiceTypeId() { return serviceTypeId; }
    public String getAddOnName() { return addOnName; }
    public Double getMonthlyPrice() { return monthlyPrice; }
    public String getDescription() { return description; }
    public Boolean getIsActive() { return isActive; }
    public String getIconKey() { return iconKey; }
    public String getThemeKey() { return themeKey; }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
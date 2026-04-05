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
    public void setAddOnId(Integer addOnId) { this.addOnId = addOnId; }

    public Integer getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(Integer serviceTypeId) { this.serviceTypeId = serviceTypeId; }

    public String getAddOnName() { return addOnName; }
    public void setAddOnName(String addOnName) { this.addOnName = addOnName; }

    public Double getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(Double monthlyPrice) { this.monthlyPrice = monthlyPrice; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getIconKey() { return iconKey; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }

    public String getThemeKey() { return themeKey; }
    public void setThemeKey(String themeKey) { this.themeKey = themeKey; }
}
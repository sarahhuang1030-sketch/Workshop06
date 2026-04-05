package com.example.workshop06.model;

public class PlanResponse {
    private Integer planId;
    private Integer serviceTypeId;
    private String planName;
    private Double monthlyPrice;
    private Integer contractTermMonths;
    private String description;
    private Integer isActive;
    private String tagline;
    private String badge;
    private String iconKey;
    private String themeKey;
    private String dataLabel;

    private String addOnNames;

    public Integer getPlanId() { return planId; }
    public Integer getServiceTypeId() { return serviceTypeId; }
    public String getPlanName() { return planName; }
    public Double getMonthlyPrice() { return monthlyPrice; }
    public Integer getContractTermMonths() { return contractTermMonths; }
    public String getDescription() { return description; }
    public Integer getIsActive() { return isActive; }
    public String getTagline() { return tagline; }
    public String getBadge() { return badge; }
    public String getIconKey() { return iconKey; }
    public String getThemeKey() { return themeKey; }
    public String getDataLabel() { return dataLabel; }

    public String getAddOnNames() {
        return addOnNames;
    }

    public void setAddOnNames(String addOnNames) {
        this.addOnNames = addOnNames;
    }
}
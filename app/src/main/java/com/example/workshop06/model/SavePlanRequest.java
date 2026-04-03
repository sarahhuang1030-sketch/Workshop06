package com.example.workshop06.model;

public class SavePlanRequest {
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

    public SavePlanRequest(Integer serviceTypeId,
                           String planName,
                           Double monthlyPrice,
                           Integer contractTermMonths,
                           String description,
                           Integer isActive,
                           String tagline,
                           String badge,
                           String iconKey,
                           String themeKey,
                           String dataLabel) {
        this.serviceTypeId = serviceTypeId;
        this.planName = planName;
        this.monthlyPrice = monthlyPrice;
        this.contractTermMonths = contractTermMonths;
        this.description = description;
        this.isActive = isActive;
        this.tagline = tagline;
        this.badge = badge;
        this.iconKey = iconKey;
        this.themeKey = themeKey;
        this.dataLabel = dataLabel;
    }

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
}
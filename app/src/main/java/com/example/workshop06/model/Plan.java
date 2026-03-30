package com.example.workshop06.model;

public class Plan {

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

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Integer getServiceTypeId() {
        return serviceTypeId;
    }

    public void setServiceTypeId(Integer serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(Double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public Integer getContractTermMonths() {
        return contractTermMonths;
    }

    public void setContractTermMonths(Integer contractTermMonths) {
        this.contractTermMonths = contractTermMonths;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    public String getThemeKey() {
        return themeKey;
    }

    public void setThemeKey(String themeKey) {
        this.themeKey = themeKey;
    }

    public String getDataLabel() {
        return dataLabel;
    }

    public void setDataLabel(String dataLabel) {
        this.dataLabel = dataLabel;
    }
}
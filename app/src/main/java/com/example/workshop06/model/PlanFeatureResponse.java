package com.example.workshop06.model;

public class PlanFeatureResponse {
    private Integer featureId;
    private Integer planId;
    private String featureName;
    private String featureValue;
    private String unit;
    private Integer sortOrder;

    public Integer getFeatureId() {
        return featureId;
    }

    public Integer getPlanId() {
        return planId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getFeatureValue() {
        return featureValue;
    }

    public String getUnit() {
        return unit;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
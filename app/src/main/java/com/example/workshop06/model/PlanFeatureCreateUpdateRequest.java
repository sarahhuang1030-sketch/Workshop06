package com.example.workshop06.model;

public class PlanFeatureCreateUpdateRequest {
    private Integer planId;
    private String featureName;
    private String featureValue;
    private String unit;
    private Integer sortOrder;

    public PlanFeatureCreateUpdateRequest(Integer planId,
                                          String featureName,
                                          String featureValue,
                                          String unit,
                                          Integer sortOrder) {
        this.planId = planId;
        this.featureName = featureName;
        this.featureValue = featureValue;
        this.unit = unit;
        this.sortOrder = sortOrder;
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
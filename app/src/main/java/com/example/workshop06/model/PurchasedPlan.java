package com.example.workshop06.model;

public class PurchasedPlan {
    private String planName;
    private Double monthlyPrice;
    private String nextBillText;

    public PurchasedPlan(String planName, Double monthlyPrice, String nextBillText) {
        this.planName = planName;
        this.monthlyPrice = monthlyPrice;
        this.nextBillText = nextBillText;
    }

    public String getPlanName() {
        return planName;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public String getNextBillText() {
        return nextBillText;
    }
}

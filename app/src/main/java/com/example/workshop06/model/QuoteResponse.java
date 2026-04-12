package com.example.workshop06.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuoteResponse {

    @SerializedName("id")
    private Integer quoteId;

    @SerializedName("customerId")
    private Integer customerId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("planId")
    private Integer planId;

    @SerializedName("addonIds")
    private List<Integer> addonIds;

    @SerializedName("amount")
    private Double totalAmount;

    @SerializedName("status")
    private String status;

    public Integer getQuoteId() { return quoteId; }
    public Integer getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public Integer getPlanId() { return planId; }
    public List<Integer> getAddonIds() { return addonIds; }
    public Double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
}
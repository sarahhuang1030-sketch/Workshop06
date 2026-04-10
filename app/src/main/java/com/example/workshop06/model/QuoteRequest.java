package com.example.workshop06.model;

import java.util.List;

/**
 * QuoteRequest
 * This class matches backend QuoteRequestDTO structure
 */
public class QuoteRequest {

    private Integer customerId;
    private Integer planId;
    private List<Integer> addonIds;
    private Double amount;
    private String status;

    public QuoteRequest() {}

    public QuoteRequest(Integer customerId,
                        Integer planId,
                        List<Integer> addonIds,
                        Double amount,
                        String status) {
        this.customerId = customerId;
        this.planId = planId;
        this.addonIds = addonIds;
        this.amount = amount;
        this.status = status;
    }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    public List<Integer> getAddonIds() { return addonIds; }
    public void setAddonIds(List<Integer> addonIds) { this.addonIds = addonIds; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
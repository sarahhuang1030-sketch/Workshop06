package com.example.workshop06.model;

public class AddOnRequest {
    private Integer serviceTypeId;
    private String addOnName;
    private Double monthlyPrice;
    private String description;
    private Boolean isActive;
    private String iconKey;
    private String themeKey;

    public AddOnRequest(Integer serviceTypeId,
                        String addOnName,
                        Double monthlyPrice,
                        String description,
                        Boolean isActive
                        ) {
        this.serviceTypeId = serviceTypeId;
        this.addOnName = addOnName;
        this.monthlyPrice = monthlyPrice;
        this.description = description;
        this.isActive = isActive;

    }
}
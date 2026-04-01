package com.example.workshop06.model;

public class LocationRequest {
    private String locationName;
    private String locationType;
    private String street1;
    private String street2;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private String phone;
    private Boolean isActive;

    public LocationRequest(String locationName,
                           String locationType,
                           String street1,
                           String street2,
                           String city,
                           String province,
                           String postalCode,
                           String country,
                           String phone,
                           Boolean isActive) {
        this.locationName = locationName;
        this.locationType = locationType;
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.country = country;
        this.phone = phone;
        this.isActive = isActive;
    }
}
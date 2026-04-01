package com.example.workshop06.model;

public class LocationResponse {
    private Integer locationId;
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

    public Integer getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public String getLocationType() { return locationType; }
    public String getStreet1() { return street1; }
    public String getStreet2() { return street2; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    public String getPhone() { return phone; }
    public Boolean getIsActive() { return isActive; }

    public Boolean getActive() {
        return isActive;
    }
}
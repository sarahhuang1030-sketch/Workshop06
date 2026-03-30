package com.example.workshop06.model;

public class AddressResponse {

    private String street1;
    private String street2;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private String addressType;
    private Integer isPrimary;

    public String getStreet1() {
        return street1;
    }

    public String getStreet2() {
        return street2;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public String getAddressType() {
        return addressType;
    }

    public Integer getIsPrimary() {
        return isPrimary;
    }
}
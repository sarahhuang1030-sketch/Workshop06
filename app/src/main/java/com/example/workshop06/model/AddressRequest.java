package com.example.workshop06.model;

public class AddressRequest {
    private String street1;
    private String street2;
    private String city;
    private String province;
    private String postalCode;
    private String country;

    public AddressRequest(String street1, String street2, String city,
                          String province, String postalCode, String country) {
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getStreet1() { return street1; }
    public String getStreet2() { return street2; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
}
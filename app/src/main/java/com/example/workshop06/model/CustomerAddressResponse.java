package com.example.workshop06.model;

public class CustomerAddressResponse {
    private Integer addressId;
    private Integer customerId;
    private String addressType;
    private String street1;
    private String street2;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private Integer isPrimary;

    public Integer getAddressId() { return addressId; }
    public Integer getCustomerId() { return customerId; }
    public String getAddressType() { return addressType; }
    public String getStreet1() { return street1; }
    public String getStreet2() { return street2; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    public Integer getIsPrimary() { return isPrimary; }
}
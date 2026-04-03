package com.example.workshop06.model;

public class CreateCustomerRequest {
    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String homePhone;
    private String customerType;
    private String status;

    private String street1;
    private String street2;
    private String city;
    private String province;
    private String postalCode;
    private String country;

    public CreateCustomerRequest(String firstName,
                                 String lastName,
                                 String businessName,
                                 String email,
                                 String homePhone,
                                 String customerType,
                                 String status,
                                 String street1,
                                 String street2,
                                 String city,
                                 String province,
                                 String postalCode,
                                 String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.businessName = businessName;
        this.email = email;
        this.homePhone = homePhone;
        this.customerType = customerType;
        this.status = status;
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getBusinessName() { return businessName; }
    public String getEmail() { return email; }
    public String getHomePhone() { return homePhone; }
    public String getCustomerType() { return customerType; }
    public String getStatus() { return status; }
    public String getStreet1() { return street1; }
    public String getStreet2() { return street2; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
}
package com.example.workshop06.model;

public class EmployeeSalesResponse {
    private Integer employeeId;
    private String firstName;
    private String lastName;
    private Integer salesCount;
    private Double totalSales;
    private String lastSaleDate;

    public Integer getEmployeeId() {
        return employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getSalesCount() {
        return salesCount;
    }

    public Double getTotalSales() {
        return totalSales;
    }

    public String getLastSaleDate() {
        return lastSaleDate;
    }

    public String getEmployeeName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        return (first + " " + last).trim();
    }
}
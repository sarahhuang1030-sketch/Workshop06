package com.example.workshop06.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InvoiceResponse {

    @SerializedName(value = "customerId", alternate = {"CustomerId"})
    public Integer customerId;

    @SerializedName("invoiceNumber")
    public String invoiceNumber;

    @SerializedName("status")
    public String status;

    @SerializedName("issueDate")
    public String issueDate;

    @SerializedName("dueDate")
    public String dueDate;
    @SerializedName("subtotal")
    public Double subtotal;

    @SerializedName("taxTotal")
    public Double taxTotal;

    @SerializedName("total")
    public Double total;

    @SerializedName("customerName")
    public String customerName;

    @SerializedName("paidByAccount")
    public PaidAccountResponse paidByAccount;

    @SerializedName("items")
    public List<InvoiceItemResponse> items;

    public Integer getCustomerId()  { return customerId; }
    public String getInvoiceNumber(){ return invoiceNumber; }
    public String getStatus()       { return status; }
    public String getIssueDate()    { return issueDate; }
    public String getDueDate()      { return dueDate; }
    public Double getSubtotal()     { return subtotal; }
    public Double getTaxTotal()     { return taxTotal; }
    public Double getTotal()        { return total; }
    public String getCustomerName() { return customerName; }
    public PaidAccountResponse getPaidByAccount() { return paidByAccount; }
    public List<InvoiceItemResponse> getItems()   { return items; }

    public static class PaidAccountResponse {
        @SerializedName("method") public String method;
        @SerializedName("last4")  public String last4;
        public String getMethod() { return method; }
        public String getLast4()  { return last4; }
    }

    public static class InvoiceItemResponse {
        @SerializedName("description")     public String description;
        @SerializedName("quantity")        public Integer quantity;
        @SerializedName("unitPrice")       public Double unitPrice;
        @SerializedName("discountAmount")  public Double discountAmount;
        @SerializedName("lineTotal")       public Double lineTotal;
        @SerializedName("itemType")        public String itemType;
        @SerializedName("serviceType")     public String serviceType;

        public String getDescription()    { return description; }
        public Integer getQuantity()      { return quantity; }
        public Double getUnitPrice()      { return unitPrice; }
        public Double getDiscountAmount() { return discountAmount; }
        public Double getLineTotal()      { return lineTotal; }
        public String getItemType()       { return itemType; }
        public String getServiceType()    { return serviceType; }
    }
}
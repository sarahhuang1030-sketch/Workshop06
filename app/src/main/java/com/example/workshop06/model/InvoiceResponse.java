package com.example.workshop06.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class InvoiceResponse {

    @SerializedName("status")
    public String status;

    @SerializedName("invoiceNumber")
    public String invoiceNumber;

    @SerializedName("customerName")
    public String customerName;

    @SerializedName("issueDate")
    public String issueDate;

    @SerializedName("dueDate")
    public String dueDate;

    @SerializedName("total")
    public Double total;

    @SerializedName("customerId")
    public Integer customerId;
    public Double subtotal;
    public Double taxTotal;
    public PaidAccountResponse paidByAccount;
    public List<InvoiceItemResponse> items;

    public Integer getCustomerId() { return customerId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getStatus() { return status; }
    public String getIssueDate() { return issueDate; }
    public String getDueDate() { return dueDate; }
    public Double getSubtotal() { return subtotal; }
    public Double getTaxTotal() { return taxTotal; }
    public Double getTotal() { return total; }
    public PaidAccountResponse getPaidByAccount() { return paidByAccount; }
    public List<InvoiceItemResponse> getItems() { return items; }
    public String getCustomerName() { return customerName; }

    public static class PaidAccountResponse {
        public String method;
        public String last4;

        public String getMethod() { return method; }
        public String getLast4() { return last4; }
    }

    public static class InvoiceItemResponse {
        public String description;
        public Integer quantity;
        public Double unitPrice;
        public Double discountAmount;
        public Double lineTotal;

        public String getDescription() { return description; }
        public Integer getQuantity() { return quantity; }
        public Double getUnitPrice() { return unitPrice; }
        public Double getDiscountAmount() { return discountAmount; }
        public Double getLineTotal() { return lineTotal; }
    }
}
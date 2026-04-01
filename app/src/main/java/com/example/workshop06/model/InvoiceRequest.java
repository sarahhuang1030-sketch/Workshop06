package com.example.workshop06.model;

public class InvoiceRequest {
    private Integer customerId;
    private String invoiceNumber;
    private String status;
    private String issueDate;
    private String dueDate;
    private Double subtotal;
    private Double taxTotal;
    private Double total;

    public InvoiceRequest(Integer customerId,
                          String invoiceNumber,
                          String status,
                          String issueDate,
                          String dueDate,
                          Double subtotal,
                          Double taxTotal,
                          Double total) {
        this.customerId = customerId;
        this.invoiceNumber = invoiceNumber;
        this.status = status;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.subtotal = subtotal;
        this.taxTotal = taxTotal;
        this.total = total;
    }

    public Integer getCustomerId() { return customerId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getStatus() { return status; }
    public String getIssueDate() { return issueDate; }
    public String getDueDate() { return dueDate; }
    public Double getSubtotal() { return subtotal; }
    public Double getTaxTotal() { return taxTotal; }
    public Double getTotal() { return total; }
}
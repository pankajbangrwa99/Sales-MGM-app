package com.salesmgm.app.models;

import java.util.ArrayList;
import java.util.List;

public class Bill {
    private long id;
    private String invoiceNumber;
    private long customerId;
    private String customerName;
    private String customerPhone;
    private double totalAmount;
    private double paidAmount;
    private double dueAmount;
    private String paymentMode; // "CASH", "ONLINE", "CREDIT"
    private String timestamp;
    private List<BillItem> items = new ArrayList<>();

    public Bill() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getDueAmount() { return dueAmount; }
    public void setDueAmount(double dueAmount) { this.dueAmount = dueAmount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public List<BillItem> getItems() { return items; }
    public void setItems(List<BillItem> items) { this.items = items; }
}

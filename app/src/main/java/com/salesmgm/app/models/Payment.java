package com.salesmgm.app.models;

public class Payment {
    private long id;
    private long customerId;
    private String customerName;
    private double amountPaid;
    private String paymentMode; // CASH, UPI, BANK
    private String note;
    private String timestamp;

    public Payment() {}

    public Payment(long id, long customerId, String customerName, double amountPaid, String paymentMode, String note, String timestamp) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.amountPaid = amountPaid;
        this.paymentMode = paymentMode;
        this.note = note;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

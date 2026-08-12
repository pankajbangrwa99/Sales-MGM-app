package com.salesmgm.app.models;

public class BillItem {
    private long id;
    private long billId;
    private long productId;
    private String productName;
    private double unitPrice;
    private int quantity;
    private double totalPrice;

    public BillItem() {}

    public BillItem(long productId, String productName, double unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = unitPrice * quantity;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getBillId() { return billId; }
    public void setBillId(long billId) { this.billId = billId; }

    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
        this.totalPrice = this.unitPrice * quantity;
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}

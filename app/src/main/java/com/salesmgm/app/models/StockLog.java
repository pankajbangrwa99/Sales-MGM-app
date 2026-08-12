package com.salesmgm.app.models;

public class StockLog {
    private long id;
    private long productId;
    private String productName;
    private String movementType; // "STOCK_IN", "STOCK_OUT", "ADJUSTMENT"
    private int quantity;
    private int previousStock;
    private int newStock;
    private double unitPrice;
    private double totalAmount;
    private String invoiceNumber;
    private String customerName;
    private String paymentMode;
    private String notesSupplier;
    private String timestamp;

    public StockLog() {}

    public StockLog(long id, long productId, String productName, String movementType, int quantity,
                    int previousStock, int newStock, double unitPrice, double totalAmount,
                    String invoiceNumber, String customerName, String paymentMode,
                    String notesSupplier, String timestamp) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.movementType = movementType;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.paymentMode = paymentMode;
        this.notesSupplier = notesSupplier;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getPreviousStock() { return previousStock; }
    public void setPreviousStock(int previousStock) { this.previousStock = previousStock; }

    public int getNewStock() { return newStock; }
    public void setNewStock(int newStock) { this.newStock = newStock; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getNotesSupplier() { return notesSupplier; }
    public void setNotesSupplier(String notesSupplier) { this.notesSupplier = notesSupplier; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}

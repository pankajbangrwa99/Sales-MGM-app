package com.salesmgm.app.models;

public class Product {
    private long id;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private String unit; // e.g. Pcs, Kg, Ltr, Box

    public Product() {}

    public Product(long id, String name, String category, double price, int quantity, String unit) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public boolean isLowStock() {
        return quantity < 4;
    }
}

package com.salesmgm.app.models;

public class Customer {
    private long id;
    private String name;
    private String phone;
    private String address;
    private double creditBalance;

    public Customer() {}

    public Customer(long id, String name, String phone, String address, double creditBalance) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.creditBalance = creditBalance;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getCreditBalance() { return creditBalance; }
    public void setCreditBalance(double creditBalance) { this.creditBalance = creditBalance; }
}

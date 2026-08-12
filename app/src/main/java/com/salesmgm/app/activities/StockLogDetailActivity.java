package com.salesmgm.app.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.salesmgm.app.R;

import java.util.Locale;

public class StockLogDetailActivity extends AppCompatActivity {

    private TextView tvDetailLogInvoice, tvDetailLogTimestamp, tvDetailLogProduct;
    private TextView tvDetailLogQuantity, tvDetailLogUnitPrice, tvDetailLogTotal;
    private TextView tvDetailLogCustomer, tvDetailLogPaymentMode, tvDetailLogUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_log_detail);

        tvDetailLogInvoice = findViewById(R.id.tvDetailLogInvoice);
        tvDetailLogTimestamp = findViewById(R.id.tvDetailLogTimestamp);
        tvDetailLogProduct = findViewById(R.id.tvDetailLogProduct);
        tvDetailLogQuantity = findViewById(R.id.tvDetailLogQuantity);
        tvDetailLogUnitPrice = findViewById(R.id.tvDetailLogUnitPrice);
        tvDetailLogTotal = findViewById(R.id.tvDetailLogTotal);
        tvDetailLogCustomer = findViewById(R.id.tvDetailLogCustomer);
        tvDetailLogPaymentMode = findViewById(R.id.tvDetailLogPaymentMode);
        tvDetailLogUser = findViewById(R.id.tvDetailLogUser);

        if (getIntent() != null) {
            String invoice = getIntent().getStringExtra("INVOICE_NUMBER");
            String timestamp = getIntent().getStringExtra("TIMESTAMP");
            String product = getIntent().getStringExtra("PRODUCT_NAME");
            int qty = getIntent().getIntExtra("QUANTITY", 0);
            double price = getIntent().getDoubleExtra("UNIT_PRICE", 0.0);
            double total = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
            String customer = getIntent().getStringExtra("CUSTOMER_NAME");
            String paymentMode = getIntent().getStringExtra("PAYMENT_MODE");

            tvDetailLogInvoice.setText("Invoice: " + (invoice != null ? invoice : "Stock Movement"));
            tvDetailLogTimestamp.setText("Date & Time: " + (timestamp != null ? timestamp : "N/A"));
            tvDetailLogProduct.setText("Product: " + (product != null ? product : "N/A"));
            tvDetailLogQuantity.setText("Quantity Sold / Moved: " + qty + " Pcs");
            tvDetailLogUnitPrice.setText(String.format(Locale.getDefault(), "Selling Unit Price: ₹%.2f", price));
            tvDetailLogTotal.setText(String.format(Locale.getDefault(), "Total Amount: ₹%.2f", total));
            tvDetailLogCustomer.setText("Customer Name: " + (customer != null ? customer : "Walk-in"));
            tvDetailLogPaymentMode.setText("Payment Method: " + (paymentMode != null ? paymentMode : "CASH"));
            tvDetailLogUser.setText("Salesperson / User: Shop Owner Admin");
        }
    }
}

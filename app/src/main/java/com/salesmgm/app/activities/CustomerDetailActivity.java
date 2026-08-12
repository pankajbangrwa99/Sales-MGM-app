package com.salesmgm.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.salesmgm.app.R;
import com.salesmgm.app.adapters.PaymentAdapter;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.Customer;
import com.salesmgm.app.models.Payment;
import com.salesmgm.app.utils.SmsHelper;

import java.util.List;
import java.util.Locale;

public class CustomerDetailActivity extends AppCompatActivity {

    private TextView tvDetailCustomerName, tvDetailCustomerPhone, tvDetailCustomerAddress, tvDetailCreditBalance;
    private Button btnRecordPayment, btnDetailSendSms;
    private RecyclerView rvCustomerPayments;
    private DatabaseHelper dbHelper;
    private long customerId = -1;
    private Customer customer;
    private PaymentAdapter paymentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        dbHelper = new DatabaseHelper(this);

        tvDetailCustomerName = findViewById(R.id.tvDetailCustomerName);
        tvDetailCustomerPhone = findViewById(R.id.tvDetailCustomerPhone);
        tvDetailCustomerAddress = findViewById(R.id.tvDetailCustomerAddress);
        tvDetailCreditBalance = findViewById(R.id.tvDetailCreditBalance);
        btnRecordPayment = findViewById(R.id.btnRecordPayment);
        btnDetailSendSms = findViewById(R.id.btnDetailSendSms);
        rvCustomerPayments = findViewById(R.id.rvCustomerPayments);

        rvCustomerPayments.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent().hasExtra("CUSTOMER_ID")) {
            customerId = getIntent().getLongExtra("CUSTOMER_ID", -1);
        }

        btnRecordPayment.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDetailActivity.this, AddPaymentActivity.class);
            intent.putExtra("CUSTOMER_ID", customerId);
            startActivity(intent);
        });

        btnDetailSendSms.setOnClickListener(v -> {
            if (customer != null && customer.getCreditBalance() > 0) {
                String msg = SmsHelper.buildPaymentReminderMessage(customer.getName(), customer.getCreditBalance(), "Our Store");
                SmsHelper.sendSms(this, customer.getPhone(), msg);
            } else {
                Toast.makeText(this, "Customer has zero pending due balance.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomerData();
    }

    private void loadCustomerData() {
        if (customerId == -1) return;

        customer = dbHelper.getCustomerById(customerId);
        if (customer != null) {
            tvDetailCustomerName.setText(customer.getName());
            tvDetailCustomerPhone.setText("Phone: " + (customer.getPhone() != null ? customer.getPhone() : "N/A"));
            tvDetailCustomerAddress.setText("Address: " + (customer.getAddress() != null ? customer.getAddress() : "N/A"));
            tvDetailCreditBalance.setText(String.format(Locale.getDefault(), "₹%.2f", customer.getCreditBalance()));

            List<Payment> paymentList = dbHelper.getPaymentsForCustomer(customerId);
            if (paymentAdapter == null) {
                paymentAdapter = new PaymentAdapter(paymentList);
                rvCustomerPayments.setAdapter(paymentAdapter);
            } else {
                paymentAdapter.updateList(paymentList);
            }
        }
    }
}

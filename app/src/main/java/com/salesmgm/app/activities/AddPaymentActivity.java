package com.salesmgm.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.salesmgm.app.R;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.Customer;

import java.util.Locale;

public class AddPaymentActivity extends AppCompatActivity {

    private TextView tvPaymentCustomerInfo, tvPaymentCurrentDue;
    private EditText etPaymentAmount, etPaymentNote;
    private Spinner spinnerRecordPaymentMode;
    private Button btnSubmitPayment;

    private DatabaseHelper dbHelper;
    private long customerId = -1;
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);

        dbHelper = new DatabaseHelper(this);

        tvPaymentCustomerInfo = findViewById(R.id.tvPaymentCustomerInfo);
        tvPaymentCurrentDue = findViewById(R.id.tvPaymentCurrentDue);
        etPaymentAmount = findViewById(R.id.etPaymentAmount);
        etPaymentNote = findViewById(R.id.etPaymentNote);
        spinnerRecordPaymentMode = findViewById(R.id.spinnerRecordPaymentMode);
        btnSubmitPayment = findViewById(R.id.btnSubmitPayment);

        if (getIntent().hasExtra("CUSTOMER_ID")) {
            customerId = getIntent().getLongExtra("CUSTOMER_ID", -1);
        }

        String[] modes = new String[]{"CASH", "UPI / ONLINE", "BANK TRANSFER", "CHEQUE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecordPaymentMode.setAdapter(adapter);

        loadCustomerDetails();

        btnSubmitPayment.setOnClickListener(v -> submitPayment());
    }

    private void loadCustomerDetails() {
        if (customerId == -1) return;

        customer = dbHelper.getCustomerById(customerId);
        if (customer != null) {
            tvPaymentCustomerInfo.setText("Customer: " + customer.getName());
            tvPaymentCurrentDue.setText(String.format(Locale.getDefault(), "Current Outstanding Balance: ₹%.2f", customer.getCreditBalance()));
        }
    }

    private void submitPayment() {
        if (customer == null) {
            Toast.makeText(this, "Customer not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        String amtStr = etPaymentAmount.getText().toString().trim();
        if (amtStr.isEmpty()) {
            Toast.makeText(this, "Please enter payment amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amtStr);
        if (amount <= 0) {
            Toast.makeText(this, "Payment amount must be greater than zero.", Toast.LENGTH_SHORT).show();
            return;
        }

        String mode = spinnerRecordPaymentMode.getSelectedItem().toString();
        String note = etPaymentNote.getText().toString().trim();

        dbHelper.recordPayment(customer.getId(), customer.getName(), amount, mode, note);

        Toast.makeText(this, "Payment entry recorded successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}

package com.salesmgm.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.salesmgm.app.R;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.Customer;

public class AddEditCustomerActivity extends AppCompatActivity {

    private TextView tvCustomerFormTitle;
    private EditText etCustomerName, etCustomerPhone, etCustomerAddress, etCustomerCreditBalance;
    private Button btnSaveCustomer;
    private DatabaseHelper dbHelper;
    private long customerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_customer);

        dbHelper = new DatabaseHelper(this);

        tvCustomerFormTitle = findViewById(R.id.tvCustomerFormTitle);
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerPhone = findViewById(R.id.etCustomerPhone);
        etCustomerAddress = findViewById(R.id.etCustomerAddress);
        etCustomerCreditBalance = findViewById(R.id.etCustomerCreditBalance);
        btnSaveCustomer = findViewById(R.id.btnSaveCustomer);

        if (getIntent().hasExtra("CUSTOMER_ID")) {
            customerId = getIntent().getLongExtra("CUSTOMER_ID", -1);
        }

        if (customerId != -1) {
            tvCustomerFormTitle.setText("Edit Customer Info");
            loadCustomerDetails();
        }

        btnSaveCustomer.setOnClickListener(v -> saveCustomer());
    }

    private void loadCustomerDetails() {
        Customer c = dbHelper.getCustomerById(customerId);
        if (c != null) {
            etCustomerName.setText(c.getName());
            etCustomerPhone.setText(c.getPhone());
            etCustomerAddress.setText(c.getAddress());
            etCustomerCreditBalance.setText(String.valueOf(c.getCreditBalance()));
        }
    }

    private void saveCustomer() {
        String name = etCustomerName.getText().toString().trim();
        String phone = etCustomerPhone.getText().toString().trim();
        String address = etCustomerAddress.getText().toString().trim();
        String creditStr = etCustomerCreditBalance.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Customer name is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        double credit = creditStr.isEmpty() ? 0.0 : Double.parseDouble(creditStr);

        if (customerId == -1) {
            Customer c = new Customer(0, name, phone, address, credit);
            dbHelper.addCustomer(c);
            Toast.makeText(this, "Customer added successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Customer c = new Customer(customerId, name, phone, address, credit);
            dbHelper.updateCustomer(c);
            Toast.makeText(this, "Customer updated successfully!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}

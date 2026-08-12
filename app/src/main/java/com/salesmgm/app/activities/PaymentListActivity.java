package com.salesmgm.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.salesmgm.app.R;
import com.salesmgm.app.adapters.CustomerAdapter;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.Customer;
import com.salesmgm.app.utils.SmsHelper;

import java.util.List;
import java.util.Locale;

public class PaymentListActivity extends AppCompatActivity {

    private TextView tvTotalPendingCreditHeader;
    private RecyclerView rvCustomersWithCredit;
    private DatabaseHelper dbHelper;
    private CustomerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_list);

        dbHelper = new DatabaseHelper(this);

        tvTotalPendingCreditHeader = findViewById(R.id.tvTotalPendingCreditHeader);
        rvCustomersWithCredit = findViewById(R.id.rvCustomersWithCredit);

        rvCustomersWithCredit.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingCredits();
    }

    private void loadPendingCredits() {
        double pending = dbHelper.getTotalPendingCredit();
        tvTotalPendingCreditHeader.setText(String.format(Locale.getDefault(), "₹%.2f", pending));

        List<Customer> dueCustomers = dbHelper.getCustomersWithDueCredit();
        if (adapter == null) {
            adapter = new CustomerAdapter(dueCustomers, new CustomerAdapter.OnCustomerActionListener() {
                @Override
                public void onCustomerClick(Customer customer) {
                    Intent intent = new Intent(PaymentListActivity.this, CustomerDetailActivity.class);
                    intent.putExtra("CUSTOMER_ID", customer.getId());
                    startActivity(intent);
                }

                @Override
                public void onSendSmsClick(Customer customer) {
                    String msg = SmsHelper.buildPaymentReminderMessage(customer.getName(), customer.getCreditBalance(), "Our Store");
                    SmsHelper.sendSms(PaymentListActivity.this, customer.getPhone(), msg);
                }
            });
            rvCustomersWithCredit.setAdapter(adapter);
        } else {
            adapter.updateList(dueCustomers);
        }
    }
}

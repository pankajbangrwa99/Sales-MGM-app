package com.salesmgm.app.activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.salesmgm.app.R;
import com.salesmgm.app.adapters.CustomerAdapter;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.Customer;
import com.salesmgm.app.utils.SmsHelper;
import com.salesmgm.app.utils.ToastUtils;

import java.util.List;

public class CustomerListActivity extends AppCompatActivity {

    private EditText etSearchCustomer;
    private Button btnAddCustomer;
    private RecyclerView rvCustomers;
    private LinearLayout llEmptyState;
    private DatabaseHelper dbHelper;
    private CustomerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        dbHelper = new DatabaseHelper(this);

        etSearchCustomer = findViewById(R.id.etSearchCustomer);
        btnAddCustomer = findViewById(R.id.btnAddCustomer);
        rvCustomers = findViewById(R.id.rvCustomers);
        llEmptyState = findViewById(R.id.llEmptyState);

        rvCustomers.setLayoutManager(new LinearLayoutManager(this));

        btnAddCustomer.setOnClickListener(v -> startActivity(new Intent(this, AddEditCustomerActivity.class)));

        etSearchCustomer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadCustomers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final Drawable deleteIcon = ContextCompat.getDrawable(CustomerListActivity.this, android.R.drawable.ic_menu_delete);
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (adapter == null) return;

                Customer customerToDelete = adapter.getCustomerAt(position);
                if (customerToDelete == null) return;

                boolean deleted = dbHelper.deleteCustomer(customerToDelete.getId());
                if (deleted) {
                    loadCustomers(etSearchCustomer.getText().toString());

                    // Show modern Material Design Snackbar with UNDO action
                    Snackbar snackbar = Snackbar.make(rvCustomers, "Removed " + customerToDelete.getName() + " from ledger", Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", v -> {
                        boolean restored = dbHelper.reinsertCustomer(customerToDelete);
                        if (restored) {
                            loadCustomers(etSearchCustomer.getText().toString());
                            ToastUtils.showToast(CustomerListActivity.this, "Restored customer " + customerToDelete.getName());
                        }
                    });
                    snackbar.setActionTextColor(Color.parseColor("#38BDF8"));
                    snackbar.setBackgroundTint(Color.parseColor("#1D2842"));
                    snackbar.setTextColor(Color.parseColor("#F8FAFC"));
                    snackbar.show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                if (dX < 0) { // Swiping right-to-left
                    paint.setColor(Color.parseColor("#EF4444"));

                    RectF background = new RectF(
                            (float) itemView.getRight() + dX,
                            (float) itemView.getTop() + 10,
                            (float) itemView.getRight() - 10,
                            (float) itemView.getBottom() - 10
                    );
                    c.drawRoundRect(background, 28, 28, paint);

                    if (deleteIcon != null) {
                        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

                        int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                        int iconRight = itemView.getRight() - iconMargin;

                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        deleteIcon.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rvCustomers);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomers(etSearchCustomer.getText().toString());
    }

    private void loadCustomers(String query) {
        List<Customer> customers = dbHelper.getAllCustomers(query);
        if (customers == null || customers.isEmpty()) {
            rvCustomers.setVisibility(View.GONE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvCustomers.setVisibility(View.VISIBLE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new CustomerAdapter(customers, new CustomerAdapter.OnCustomerActionListener() {
                    @Override
                    public void onCustomerClick(Customer customer) {
                        Intent intent = new Intent(CustomerListActivity.this, CustomerDetailActivity.class);
                        intent.putExtra("CUSTOMER_ID", customer.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onSendSmsClick(Customer customer) {
                        String msg = SmsHelper.buildPaymentReminderMessage(customer.getName(), customer.getCreditBalance(), "Our Store");
                        SmsHelper.sendSms(CustomerListActivity.this, customer.getPhone(), msg);
                    }
                });
                rvCustomers.setAdapter(adapter);
            } else {
                adapter.updateList(customers);
            }
        }
    }
}

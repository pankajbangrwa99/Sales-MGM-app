package com.salesmgm.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.salesmgm.app.R;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.utils.PermissionUtils;
import com.salesmgm.app.utils.SessionManager;
import com.salesmgm.app.utils.ShopSettingsManager;

import java.util.Calendar;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeUser, tvTodaySales, tvTotalSales, tvPendingCredit, tvLowStockCount;
    private Button btnLogout, btnSettings, btnNewBilling, btnStockInventory, btnCustomers, btnSalesHistory, btnPaymentsCredit, btnStockMovement;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ShopSettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        settingsManager = new ShopSettingsManager(this);

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvTodaySales = findViewById(R.id.tvTodaySales);
        tvTotalSales = findViewById(R.id.tvTotalSales);
        tvPendingCredit = findViewById(R.id.tvPendingCredit);
        tvLowStockCount = findViewById(R.id.tvLowStockCount);

        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);
        btnNewBilling = findViewById(R.id.btnNewBilling);
        btnStockInventory = findViewById(R.id.btnStockInventory);
        btnCustomers = findViewById(R.id.btnCustomers);
        btnSalesHistory = findViewById(R.id.btnSalesHistory);
        btnPaymentsCredit = findViewById(R.id.btnPaymentsCredit);
        btnStockMovement = findViewById(R.id.btnStockMovement);

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PermissionUtils.checkAndRequestPermissions(this);
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Personalized Dynamic Time-Based Greeting
        String profileName = settingsManager.getProfileFullName();
        if (profileName == null || profileName.trim().isEmpty()) {
            profileName = sessionManager.getFullName();
        }

        if (profileName == null || profileName.trim().isEmpty() || "Shopkeeper".equalsIgnoreCase(profileName.trim())) {
            tvWelcomeUser.setText("Welcome! Please complete your profile.");
        } else {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            String greetingPrefix;
            if (hour >= 5 && hour < 12) {
                greetingPrefix = "🌅 Good Morning";
            } else if (hour >= 12 && hour < 17) {
                greetingPrefix = "☀️ Good Afternoon";
            } else if (hour >= 17 && hour < 21) {
                greetingPrefix = "🌇 Good Evening";
            } else {
                greetingPrefix = "🌙 Good Night";
            }
            tvWelcomeUser.setText(greetingPrefix + ", " + profileName + "!");
        }

        double todaySales = dbHelper.getTodaySalesAmount();
        double totalSales = dbHelper.getTotalSalesAmount();
        double pendingCredit = dbHelper.getTotalPendingCredit();
        int lowStockCount = dbHelper.getLowStockCount();

        tvTodaySales.setText(String.format(Locale.getDefault(), "₹%.2f", todaySales));
        tvTotalSales.setText(String.format(Locale.getDefault(), "₹%.2f", totalSales));
        tvPendingCredit.setText(String.format(Locale.getDefault(), "₹%.2f", pendingCredit));
        tvLowStockCount.setText(String.format(Locale.getDefault(), "%d Items", lowStockCount));
    }

    private void setupListeners() {
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, ShopSettingsActivity.class)));

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnNewBilling.setOnClickListener(v -> startActivity(new Intent(this, CreateBillActivity.class)));
        btnStockInventory.setOnClickListener(v -> startActivity(new Intent(this, ProductListActivity.class)));
        btnCustomers.setOnClickListener(v -> startActivity(new Intent(this, CustomerListActivity.class)));
        btnSalesHistory.setOnClickListener(v -> startActivity(new Intent(this, BillHistoryActivity.class)));
        btnPaymentsCredit.setOnClickListener(v -> startActivity(new Intent(this, PaymentListActivity.class)));
        if (btnStockMovement != null) {
            btnStockMovement.setOnClickListener(v -> startActivity(new Intent(this, StockMovementActivity.class)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

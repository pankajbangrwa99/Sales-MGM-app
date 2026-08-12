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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.salesmgm.app.R;
import com.salesmgm.app.adapters.StockLogAdapter;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.StockLog;
import com.salesmgm.app.utils.ToastUtils;

import java.util.List;
import java.util.Locale;

public class StockMovementActivity extends AppCompatActivity {

    private TextView tvTotalStockIn, tvTotalStockOut;
    private EditText etSearchStockLog;
    private Button btnFilterAll, btnFilterStockOut, btnFilterStockIn;
    private RecyclerView rvStockLogs;
    private LinearLayout llEmptyState;

    private DatabaseHelper dbHelper;
    private StockLogAdapter adapter;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_movement);

        dbHelper = new DatabaseHelper(this);

        tvTotalStockIn = findViewById(R.id.tvTotalStockIn);
        tvTotalStockOut = findViewById(R.id.tvTotalStockOut);
        etSearchStockLog = findViewById(R.id.etSearchStockLog);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterStockOut = findViewById(R.id.btnFilterStockOut);
        btnFilterStockIn = findViewById(R.id.btnFilterStockIn);
        rvStockLogs = findViewById(R.id.rvStockLogs);
        llEmptyState = findViewById(R.id.llEmptyState);

        rvStockLogs.setLayoutManager(new LinearLayoutManager(this));

        setupFilters();

        etSearchStockLog.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadStockLogs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupBidirectionalSwipe();
    }

    private void setupFilters() {
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterButtonStyles();
            loadStockLogs(etSearchStockLog.getText().toString());
        });

        btnFilterStockOut.setOnClickListener(v -> {
            currentFilter = "STOCK_OUT";
            updateFilterButtonStyles();
            loadStockLogs(etSearchStockLog.getText().toString());
        });

        btnFilterStockIn.setOnClickListener(v -> {
            currentFilter = "STOCK_IN";
            updateFilterButtonStyles();
            loadStockLogs(etSearchStockLog.getText().toString());
        });
    }

    private void updateFilterButtonStyles() {
        int activeColor = Color.parseColor("#38BDF8");
        int inactiveColor = Color.parseColor("#1D2842");

        btnFilterAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf("ALL".equals(currentFilter) ? activeColor : inactiveColor));
        btnFilterStockOut.setBackgroundTintList(android.content.res.ColorStateList.valueOf("STOCK_OUT".equals(currentFilter) ? activeColor : inactiveColor));
        btnFilterStockIn.setBackgroundTintList(android.content.res.ColorStateList.valueOf("STOCK_IN".equals(currentFilter) ? activeColor : inactiveColor));
    }

    private void setupBidirectionalSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final Drawable deleteIcon = ContextCompat.getDrawable(StockMovementActivity.this, android.R.drawable.ic_menu_delete);
            private final Drawable infoIcon = ContextCompat.getDrawable(StockMovementActivity.this, android.R.drawable.ic_menu_info_details);
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (adapter == null) return;

                StockLog log = adapter.getLogAt(position);
                if (log == null) return;

                if (direction == ItemTouchHelper.RIGHT) { // Swiping Left-to-Right (Green Action -> View Sold Details)
                    Intent intent = new Intent(StockMovementActivity.this, StockLogDetailActivity.class);
                    intent.putExtra("INVOICE_NUMBER", log.getInvoiceNumber());
                    intent.putExtra("TIMESTAMP", log.getTimestamp());
                    intent.putExtra("PRODUCT_NAME", log.getProductName());
                    intent.putExtra("QUANTITY", log.getQuantity());
                    intent.putExtra("UNIT_PRICE", log.getUnitPrice());
                    intent.putExtra("TOTAL_AMOUNT", log.getTotalAmount());
                    intent.putExtra("CUSTOMER_NAME", log.getCustomerName());
                    intent.putExtra("PAYMENT_MODE", log.getPaymentMode());
                    startActivity(intent);
                    adapter.notifyItemChanged(position); // Restore item position smoothly
                } else if (direction == ItemTouchHelper.LEFT) { // Swiping Right-to-Left (Red Action -> Dismiss/Remove Log)
                    loadStockLogs(etSearchStockLog.getText().toString());
                    Snackbar snackbar = Snackbar.make(rvStockLogs, "Logged event for " + log.getProductName() + " dismissed", Snackbar.LENGTH_SHORT);
                    snackbar.setActionTextColor(Color.parseColor("#38BDF8"));
                    snackbar.setBackgroundTint(Color.parseColor("#1D2842"));
                    snackbar.setTextColor(Color.parseColor("#F8FAFC"));
                    snackbar.show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                if (dX > 0) { // Swiping Left-to-Right (Green YouTube Music style)
                    paint.setColor(Color.parseColor("#10B981")); // Emerald Green Accent

                    RectF background = new RectF(
                            (float) itemView.getLeft() + 10,
                            (float) itemView.getTop() + 10,
                            (float) itemView.getLeft() + dX,
                            (float) itemView.getBottom() - 10
                    );
                    c.drawRoundRect(background, 28, 28, paint);

                    if (infoIcon != null) {
                        int iconMargin = (itemView.getHeight() - infoIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + infoIcon.getIntrinsicHeight();

                        int iconLeft = itemView.getLeft() + iconMargin;
                        int iconRight = iconLeft + infoIcon.getIntrinsicWidth();

                        infoIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        infoIcon.draw(c);
                    }
                } else if (dX < 0) { // Swiping Right-to-Left (Red style)
                    paint.setColor(Color.parseColor("#EF4444")); // Red Accent

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
        itemTouchHelper.attachToRecyclerView(rvStockLogs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStockLogs(etSearchStockLog.getText().toString());
    }

    private void loadStockLogs(String query) {
        dbHelper.syncHistoricalStockLogs();

        int stockIn = dbHelper.getTotalStockInCount();
        int stockOut = dbHelper.getTotalStockOutCount();

        tvTotalStockIn.setText(String.format(Locale.getDefault(), "%d Pcs", stockIn));
        tvTotalStockOut.setText(String.format(Locale.getDefault(), "%d Pcs", stockOut));

        List<StockLog> logs = dbHelper.getAllStockLogs(query, currentFilter);
        if (logs == null || logs.isEmpty()) {
            rvStockLogs.setVisibility(View.GONE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvStockLogs.setVisibility(View.VISIBLE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new StockLogAdapter(logs, log -> {
                    Intent intent = new Intent(StockMovementActivity.this, StockLogDetailActivity.class);
                    intent.putExtra("INVOICE_NUMBER", log.getInvoiceNumber());
                    intent.putExtra("TIMESTAMP", log.getTimestamp());
                    intent.putExtra("PRODUCT_NAME", log.getProductName());
                    intent.putExtra("QUANTITY", log.getQuantity());
                    intent.putExtra("UNIT_PRICE", log.getUnitPrice());
                    intent.putExtra("TOTAL_AMOUNT", log.getTotalAmount());
                    intent.putExtra("CUSTOMER_NAME", log.getCustomerName());
                    intent.putExtra("PAYMENT_MODE", log.getPaymentMode());
                    startActivity(intent);
                });
                rvStockLogs.setAdapter(adapter);
            } else {
                adapter.updateList(logs);
            }
        }
    }
}

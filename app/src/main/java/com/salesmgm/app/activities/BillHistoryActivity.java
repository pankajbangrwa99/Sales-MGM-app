package com.salesmgm.app.activities;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.salesmgm.app.R;
import com.salesmgm.app.adapters.BillAdapter;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.Bill;
import com.salesmgm.app.utils.ToastUtils;

import java.util.List;

public class BillHistoryActivity extends AppCompatActivity {

    private RecyclerView rvBillHistory;
    private LinearLayout llEmptyState;
    private DatabaseHelper dbHelper;
    private BillAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_history);

        dbHelper = new DatabaseHelper(this);

        rvBillHistory = findViewById(R.id.rvBillHistory);
        llEmptyState = findViewById(R.id.llEmptyState);

        rvBillHistory.setLayoutManager(new LinearLayoutManager(this));

        setupSwipeActions();
    }

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final Drawable deleteIcon = ContextCompat.getDrawable(BillHistoryActivity.this, android.R.drawable.ic_menu_delete);
            private final Drawable infoIcon = ContextCompat.getDrawable(BillHistoryActivity.this, android.R.drawable.ic_menu_info_details);
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (adapter == null) return;

                Bill bill = adapter.getBillAt(position);
                if (bill == null) return;

                if (direction == ItemTouchHelper.RIGHT) { // Swiping Left-to-Right (Green YouTube Music style -> View Sold Details)
                    Intent intent = new Intent(BillHistoryActivity.this, BillDetailActivity.class);
                    intent.putExtra("BILL_ID", bill.getId());
                    startActivity(intent);
                    adapter.notifyItemChanged(position); // Restore card position cleanly
                } else if (direction == ItemTouchHelper.LEFT) { // Swiping Right-to-Left (Red style -> Delete & Restore Stock)
                    boolean deleted = dbHelper.deleteBill(bill.getId());
                    if (deleted) {
                        loadBills();

                        Snackbar snackbar = Snackbar.make(rvBillHistory, "Deleted " + bill.getInvoiceNumber() + " (Stock Restored)", Snackbar.LENGTH_LONG);
                        snackbar.setAction("UNDO", v -> {
                            boolean restored = dbHelper.reinsertBill(bill);
                            if (restored) {
                                loadBills();
                                ToastUtils.showToast(BillHistoryActivity.this, "Restored invoice " + bill.getInvoiceNumber());
                            }
                        });
                        snackbar.setActionTextColor(Color.parseColor("#38BDF8"));
                        snackbar.setBackgroundTint(Color.parseColor("#1D2842"));
                        snackbar.setTextColor(Color.parseColor("#F8FAFC"));
                        snackbar.show();
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                if (dX > 0) { // Swiping Left-to-Right (Green Action)
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
                } else if (dX < 0) { // Swiping Right-to-Left (Red Action)
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
        itemTouchHelper.attachToRecyclerView(rvBillHistory);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBills();
    }

    private void loadBills() {
        List<Bill> billList = dbHelper.getAllBills();
        if (billList == null || billList.isEmpty()) {
            rvBillHistory.setVisibility(View.GONE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvBillHistory.setVisibility(View.VISIBLE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new BillAdapter(billList, bill -> {
                    Intent intent = new Intent(BillHistoryActivity.this, BillDetailActivity.class);
                    intent.putExtra("BILL_ID", bill.getId());
                    startActivity(intent);
                });
                rvBillHistory.setAdapter(adapter);
            } else {
                adapter.updateList(billList);
            }
        }
    }
}

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.salesmgm.app.R;
import com.salesmgm.app.adapters.ProductAdapter;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.Product;
import com.salesmgm.app.utils.ToastUtils;

import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private EditText etSearchProduct;
    private Button btnAddProduct;
    private RecyclerView rvProducts;
    private LinearLayout llEmptyState;

    private DatabaseHelper dbHelper;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        dbHelper = new DatabaseHelper(this);

        etSearchProduct = findViewById(R.id.etSearchProduct);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        rvProducts = findViewById(R.id.rvProducts);
        llEmptyState = findViewById(R.id.llEmptyState);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        btnAddProduct.setOnClickListener(v -> startActivity(new Intent(this, AddEditProductActivity.class)));

        etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupSwipeActions();
    }

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final Drawable deleteIcon = ContextCompat.getDrawable(ProductListActivity.this, android.R.drawable.ic_menu_delete);
            private final Drawable infoIcon = ContextCompat.getDrawable(ProductListActivity.this, android.R.drawable.ic_menu_info_details);
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (adapter == null) return;

                Product product = adapter.getProductAt(position);
                if (product == null) return;

                if (direction == ItemTouchHelper.RIGHT) { // Swiping Left-to-Right (Green Action -> Edit Product)
                    Intent intent = new Intent(ProductListActivity.this, AddEditProductActivity.class);
                    intent.putExtra("PRODUCT_ID", product.getId());
                    startActivity(intent);
                    adapter.notifyItemChanged(position); // Restore card position cleanly
                } else if (direction == ItemTouchHelper.LEFT) { // Swiping Right-to-Left (Red Action -> Delete Product)
                    boolean deleted = dbHelper.deleteProduct(product.getId());
                    if (deleted) {
                        loadProducts(etSearchProduct.getText().toString());

                        Snackbar snackbar = Snackbar.make(rvProducts, "Deleted product " + product.getName(), Snackbar.LENGTH_LONG);
                        snackbar.setAction("UNDO", v -> {
                            boolean restored = dbHelper.reinsertProduct(product);
                            if (restored) {
                                loadProducts(etSearchProduct.getText().toString());
                                ToastUtils.showToast(ProductListActivity.this, "Restored product " + product.getName());
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
        itemTouchHelper.attachToRecyclerView(rvProducts);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(etSearchProduct.getText().toString());
    }

    private void loadProducts(String query) {
        List<Product> list = dbHelper.getAllProducts(query);
        if (list == null || list.isEmpty()) {
            rvProducts.setVisibility(View.GONE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvProducts.setVisibility(View.VISIBLE);
            if (llEmptyState != null) llEmptyState.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new ProductAdapter(list, product -> {
                    Intent intent = new Intent(ProductListActivity.this, AddEditProductActivity.class);
                    intent.putExtra("PRODUCT_ID", product.getId());
                    startActivity(intent);
                });
                rvProducts.setAdapter(adapter);
            } else {
                adapter.updateList(list);
            }
        }
    }
}

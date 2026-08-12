package com.salesmgm.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.salesmgm.app.R;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.Product;

import java.util.List;

public class AddEditProductActivity extends AppCompatActivity {

    private TextView tvFormTitle;
    private EditText etProductName, etProductCategory, etProductPrice, etProductQty, etProductUnit;
    private Button btnSaveProduct, btnDeleteProduct;
    private DatabaseHelper dbHelper;
    private long productId = -1;
    private Product existingProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        dbHelper = new DatabaseHelper(this);

        tvFormTitle = findViewById(R.id.tvFormTitle);
        etProductName = findViewById(R.id.etProductName);
        etProductCategory = findViewById(R.id.etProductCategory);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductQty = findViewById(R.id.etProductQty);
        etProductUnit = findViewById(R.id.etProductUnit);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);

        if (getIntent().hasExtra("PRODUCT_ID")) {
            productId = getIntent().getLongExtra("PRODUCT_ID", -1);
        }

        if (productId != -1) {
            tvFormTitle.setText("Edit Product Details");
            btnDeleteProduct.setVisibility(View.VISIBLE);
            loadProductDetails();
        }

        btnSaveProduct.setOnClickListener(v -> saveProduct());
        btnDeleteProduct.setOnClickListener(v -> deleteProduct());
    }

    private void loadProductDetails() {
        List<Product> products = dbHelper.getAllProducts(null);
        for (Product p : products) {
            if (p.getId() == productId) {
                existingProduct = p;
                etProductName.setText(p.getName());
                etProductCategory.setText(p.getCategory());
                etProductPrice.setText(String.valueOf(p.getPrice()));
                etProductQty.setText(String.valueOf(p.getQuantity()));
                etProductUnit.setText(p.getUnit());
                break;
            }
        }
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String category = etProductCategory.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String qtyStr = etProductQty.getText().toString().trim();
        String unit = etProductUnit.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
            Toast.makeText(this, "Product name, price, and quantity are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int qty = Integer.parseInt(qtyStr);
        if (unit.isEmpty()) unit = "Pcs";

        if (productId == -1) {
            Product p = new Product(0, name, category, price, qty, unit);
            dbHelper.addProduct(p);
            Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Product p = new Product(productId, name, category, price, qty, unit);
            dbHelper.updateProduct(p);
            Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void deleteProduct() {
        if (productId != -1) {
            dbHelper.deleteProduct(productId);
            Toast.makeText(this, "Product deleted.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

package com.salesmgm.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.salesmgm.app.R;
import com.salesmgm.app.adapters.CartAdapter;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.Bill;
import com.salesmgm.app.models.BillItem;
import com.salesmgm.app.models.Customer;
import com.salesmgm.app.models.Product;
import com.salesmgm.app.utils.PdfGenerator;
import com.salesmgm.app.utils.SmsHelper;
import com.salesmgm.app.utils.ToastUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateBillActivity extends AppCompatActivity {

    private Spinner spinnerCustomer, spinnerProduct, spinnerPaymentMode;
    private AutoCompleteTextView actvSearchProduct;
    private EditText etBillCustomerPhone, etBillPaidAmount;
    private CheckBox cbSendSms;
    private Button btnAddToCart, btnFinalizeBill;
    private RecyclerView rvCartItems;
    private TextView tvBillGrandTotal, tvBillDueBalance, tvCreateBillHeaderTitle;

    private DatabaseHelper dbHelper;
    private List<Customer> customerList;
    private List<Product> productList;
    private List<BillItem> cartItems = new ArrayList<>();
    private CartAdapter cartAdapter;

    private Customer selectedCustomer = null;
    private Product selectedProduct = null;
    private String selectedPaymentMode = "CASH";
    private boolean isSelfUpdatingPaid = false;
    private boolean isInitializingEditBill = false;

    private long editBillId = -1;
    private Bill editBill = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bill);

        dbHelper = new DatabaseHelper(this);

        tvCreateBillHeaderTitle = findViewById(R.id.tvCreateBillHeaderTitle);
        spinnerCustomer = findViewById(R.id.spinnerCustomer);
        spinnerProduct = findViewById(R.id.spinnerProduct);
        spinnerPaymentMode = findViewById(R.id.spinnerPaymentMode);
        actvSearchProduct = findViewById(R.id.actvSearchProduct);
        etBillCustomerPhone = findViewById(R.id.etBillCustomerPhone);
        cbSendSms = findViewById(R.id.cbSendSms);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnFinalizeBill = findViewById(R.id.btnFinalizeBill);
        rvCartItems = findViewById(R.id.rvCartItems);
        tvBillGrandTotal = findViewById(R.id.tvBillGrandTotal);
        tvBillDueBalance = findViewById(R.id.tvBillDueBalance);
        etBillPaidAmount = findViewById(R.id.etBillPaidAmount);

        // Load local preference for SMS sending (Default: Unchecked/false)
        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean sendSmsDefault = prefs.getBoolean("PREF_SEND_SMS_ENABLED", false);
        if (cbSendSms != null) {
            cbSendSms.setChecked(sendSmsDefault);
            cbSendSms.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("PREF_SEND_SMS_ENABLED", isChecked).apply();
            });
        }

        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public boolean onIncreaseRequested(BillItem item, int position) {
                int availableStock = getAvailableStockForProduct(item.getProductId());
                if (item.getQuantity() + 1 > availableStock) {
                    ToastUtils.showToast(CreateBillActivity.this, "Stock limit reached for '" + item.getProductName() + "'. Available in Inventory: " + availableStock);
                    return false;
                }
                return true;
            }

            @Override
            public void onQuantityChanged() {
                updateBillTotals();
            }

            @Override
            public void onItemRemoved(int position) {
                updateBillTotals();
            }
        });
        rvCartItems.setAdapter(cartAdapter);

        loadSpinnersData();

        btnAddToCart.setOnClickListener(v -> addSelectedProductToCart());

        etBillPaidAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isSelfUpdatingPaid && !isInitializingEditBill) {
                    calculateAndDisplayDueBalance(calculateCartTotal());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (getIntent().hasExtra("EDIT_BILL_ID")) {
            editBillId = getIntent().getLongExtra("EDIT_BILL_ID", -1);
            loadBillForEditing(editBillId);
        }

        btnFinalizeBill.setOnClickListener(v -> finalizeBillAndGenerateInvoice());
    }

    private int getAvailableStockForProduct(long productId) {
        Product dbProduct = dbHelper.getProductById(productId);
        int available = dbProduct != null ? dbProduct.getQuantity() : 0;

        // If editing an existing bill, add back the quantity of this item that was originally in editBill
        if (editBill != null && editBill.getItems() != null) {
            for (BillItem origItem : editBill.getItems()) {
                if (origItem.getProductId() == productId) {
                    available += origItem.getQuantity();
                    break;
                }
            }
        }
        return available;
    }

    private void loadBillForEditing(long billId) {
        editBill = dbHelper.getBillById(billId);
        if (editBill == null) return;

        isInitializingEditBill = true;

        if (tvCreateBillHeaderTitle != null) {
            tvCreateBillHeaderTitle.setText("✏️ Edit Invoice: " + editBill.getInvoiceNumber());
        }
        btnFinalizeBill.setText("UPDATE INVOICE & SAVE");

        // Preload all line items
        cartItems.clear();
        if (editBill.getItems() != null) {
            cartItems.addAll(editBill.getItems());
        }
        cartAdapter.notifyDataSetChanged();

        // Preload customer phone
        if (editBill.getCustomerPhone() != null && !editBill.getCustomerPhone().isEmpty()) {
            etBillCustomerPhone.setText(editBill.getCustomerPhone());
        }

        // Preselect Customer
        boolean foundCustomer = false;
        if (editBill.getCustomerId() > 0 && customerList != null) {
            for (int i = 0; i < customerList.size(); i++) {
                if (customerList.get(i).getId() == editBill.getCustomerId()) {
                    spinnerCustomer.setSelection(i + 1);
                    selectedCustomer = customerList.get(i);
                    foundCustomer = true;
                    break;
                }
            }
        }
        if (!foundCustomer && editBill.getCustomerId() > 0) {
            selectedCustomer = dbHelper.getCustomerById(editBill.getCustomerId());
        }

        // Preselect Payment Mode
        if (editBill.getPaymentMode() != null) {
            String modeStr = editBill.getPaymentMode().toUpperCase();
            if (modeStr.contains("CREDIT") || modeStr.contains("KHATA")) {
                spinnerPaymentMode.setSelection(2);
                selectedPaymentMode = "CREDIT (KHATA)";
            } else if (modeStr.contains("ONLINE") || modeStr.contains("UPI")) {
                spinnerPaymentMode.setSelection(1);
                selectedPaymentMode = "ONLINE / UPI";
            } else {
                spinnerPaymentMode.setSelection(0);
                selectedPaymentMode = "CASH";
            }
        }

        // Preload Paid Amount
        isSelfUpdatingPaid = true;
        etBillPaidAmount.setText(String.valueOf((int) Math.round(editBill.getPaidAmount())));
        isSelfUpdatingPaid = false;

        calculateAndDisplayDueBalance(calculateCartTotal());

        isInitializingEditBill = false;
    }

    private void loadSpinnersData() {
        // Load Customers
        customerList = dbHelper.getAllCustomers(null);
        List<String> customerNames = new ArrayList<>();
        customerNames.add("Walk-in Customer (Cash)");
        for (Customer c : customerList) {
            customerNames.add(c.getName() + " (" + c.getPhone() + ")");
        }

        ArrayAdapter<String> customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, customerNames);
        customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCustomer.setAdapter(customerAdapter);

        spinnerCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitializingEditBill) return;
                if (position == 0) {
                    selectedCustomer = null;
                    if (editBill == null) {
                        etBillCustomerPhone.setText("");
                    }
                } else {
                    selectedCustomer = customerList.get(position - 1);
                    if (selectedCustomer != null && selectedCustomer.getPhone() != null) {
                        etBillCustomerPhone.setText(selectedCustomer.getPhone());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Load Products
        productList = dbHelper.getAllProducts(null);
        List<String> productNames = new ArrayList<>();
        List<String> searchSuggestions = new ArrayList<>();

        for (Product p : productList) {
            String label = p.getName() + " - ₹" + p.getPrice() + " (Stock: " + p.getQuantity() + ")";
            productNames.add(label);
            searchSuggestions.add(p.getName());
        }

        ArrayAdapter<String> productSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productNames);
        productSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(productSpinnerAdapter);

        spinnerProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < productList.size()) {
                    selectedProduct = productList.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Instant AutoComplete Product Search Bar
        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, searchSuggestions);
        actvSearchProduct.setAdapter(searchAdapter);

        actvSearchProduct.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            for (int i = 0; i < productList.size(); i++) {
                if (productList.get(i).getName().equalsIgnoreCase(selectedName)) {
                    spinnerProduct.setSelection(i);
                    selectedProduct = productList.get(i);
                    addSelectedProductToCart();
                    actvSearchProduct.setText("");
                    break;
                }
            }
        });

        // Payment Modes
        String[] modes = new String[]{"CASH", "ONLINE / UPI", "CREDIT (KHATA)"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modes);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMode.setAdapter(modeAdapter);

        spinnerPaymentMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitializingEditBill) return;
                selectedPaymentMode = modes[position];

                if (editBill != null) {
                    isSelfUpdatingPaid = true;
                    if (selectedPaymentMode.startsWith("CREDIT")) {
                        etBillPaidAmount.setText("0");
                    } else if (selectedPaymentMode.equals("CASH") || selectedPaymentMode.startsWith("ONLINE")) {
                        etBillPaidAmount.setText(String.valueOf((int) Math.round(calculateCartTotal())));
                    }
                    isSelfUpdatingPaid = false;
                }

                updateBillTotals();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void addSelectedProductToCart() {
        if (selectedProduct == null) {
            ToastUtils.showToast(this, "Please select a product first.");
            return;
        }

        int availableStock = getAvailableStockForProduct(selectedProduct.getId());
        if (availableStock <= 0) {
            ToastUtils.showToast(this, "Product '" + selectedProduct.getName() + "' is OUT OF STOCK!");
            return;
        }

        // Check if item already exists in cart
        for (BillItem item : cartItems) {
            if (item.getProductId() == selectedProduct.getId()) {
                if (item.getQuantity() + 1 > availableStock) {
                    ToastUtils.showToast(this, "Cannot add more. Available stock for this bill: " + availableStock + " Pcs.");
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                cartAdapter.notifyDataSetChanged();
                updateBillTotals();
                return;
            }
        }

        BillItem newItem = new BillItem(selectedProduct.getId(), selectedProduct.getName(), selectedProduct.getPrice(), 1);
        cartItems.add(newItem);
        cartAdapter.notifyDataSetChanged();
        updateBillTotals();
    }

    private double calculateCartTotal() {
        double total = 0;
        for (BillItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    private void updateBillTotals() {
        double total = calculateCartTotal();
        tvBillGrandTotal.setText(String.format(Locale.getDefault(), "₹%.2f", total));

        if (!isInitializingEditBill && editBill == null && selectedPaymentMode != null) {
            isSelfUpdatingPaid = true;
            if (selectedPaymentMode.startsWith("CREDIT")) {
                etBillPaidAmount.setText("0");
            } else if (selectedPaymentMode.equals("CASH") || selectedPaymentMode.startsWith("ONLINE")) {
                etBillPaidAmount.setText(String.valueOf((int) Math.round(total)));
            }
            isSelfUpdatingPaid = false;
        }

        calculateAndDisplayDueBalance(total);
    }

    private void calculateAndDisplayDueBalance(double total) {
        String paidStr = etBillPaidAmount.getText().toString().trim();
        double paid = paidStr.isEmpty() ? 0.0 : Double.parseDouble(paidStr);
        double due = Math.max(0, total - paid);
        tvBillDueBalance.setText(String.format(Locale.getDefault(), "₹%.2f", due));
    }

    private void finalizeBillAndGenerateInvoice() {
        if (cartItems.isEmpty()) {
            ToastUtils.showToast(this, "Cart is empty. Add products before creating a bill.");
            return;
        }

        // Re-verify stock for all items in cart against available stock before generating bill
        for (BillItem item : cartItems) {
            int availableStock = getAvailableStockForProduct(item.getProductId());
            if (item.getQuantity() > availableStock) {
                ToastUtils.showToast(this, "Cannot update bill! Stock insufficient for '" + item.getProductName() + "'. Available: " + availableStock + ", Cart: " + item.getQuantity());
                return;
            }
        }

        double total = calculateCartTotal();
        String paidStr = etBillPaidAmount.getText().toString().trim();
        double paid = paidStr.isEmpty() ? 0.0 : Double.parseDouble(paidStr);
        double due = Math.max(0, total - paid);

        if (due > 0 && selectedCustomer == null && (editBill == null || editBill.getCustomerId() <= 0)) {
            ToastUtils.showToast(this, "Partial payment / Credit balance requires selecting a registered Customer.");
            return;
        }

        String invoiceNo;
        String timestamp;
        if (editBill != null) {
            invoiceNo = editBill.getInvoiceNumber();
            timestamp = editBill.getTimestamp();
        } else {
            invoiceNo = dbHelper.getNextInvoiceNumber();
            timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        }

        String customerName = selectedCustomer != null ? selectedCustomer.getName() : (editBill != null && editBill.getCustomerName() != null ? editBill.getCustomerName() : "Walk-in Customer");
        String customerPhone = etBillCustomerPhone.getText().toString().trim();
        if (customerPhone.isEmpty() && editBill != null && editBill.getCustomerPhone() != null) {
            customerPhone = editBill.getCustomerPhone();
        } else if (customerPhone.isEmpty() && selectedCustomer != null && selectedCustomer.getPhone() != null) {
            customerPhone = selectedCustomer.getPhone();
        }

        Bill bill = new Bill();
        if (editBill != null) {
            bill.setId(editBill.getId());
        }
        bill.setInvoiceNumber(invoiceNo);
        bill.setCustomerId(selectedCustomer != null ? selectedCustomer.getId() : (editBill != null ? editBill.getCustomerId() : 0));
        bill.setCustomerName(customerName);
        bill.setCustomerPhone(customerPhone);
        bill.setTotalAmount(total);
        bill.setPaidAmount(paid);
        bill.setDueAmount(due);
        bill.setPaymentMode(selectedPaymentMode);
        bill.setTimestamp(timestamp);
        bill.setItems(cartItems);

        long billId;
        if (editBill != null) {
            dbHelper.updateBill(bill);
            billId = editBill.getId();
            ToastUtils.showToast(this, "Invoice " + invoiceNo + " updated successfully!");
        } else {
            billId = dbHelper.createBill(bill);
            bill.setId(billId);
            ToastUtils.showToast(this, "Bill created successfully! Invoice: " + invoiceNo);
        }

        // Generate PDF Invoice
        try {
            File pdfFile = PdfGenerator.generateInvoicePdf(this, bill);
            ToastUtils.showToast(this, "Invoice PDF saved: " + pdfFile.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send SIM SMS Receipt ONLY if enabled by user and customer phone exists
        if (cbSendSms != null && cbSendSms.isChecked() && !customerPhone.isEmpty()) {
            String smsMsg = SmsHelper.buildBillConfirmationMessage(customerName, invoiceNo, total, due);
            SmsHelper.sendSms(this, customerPhone, smsMsg);
        }

        Intent intent = new Intent(this, BillDetailActivity.class);
        intent.putExtra("BILL_ID", billId);
        startActivity(intent);
        finish();
    }
}

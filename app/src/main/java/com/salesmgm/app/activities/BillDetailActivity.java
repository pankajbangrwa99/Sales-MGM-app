package com.salesmgm.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.salesmgm.app.R;
import com.salesmgm.app.adapters.CartAdapter;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.Bill;
import com.salesmgm.app.utils.PdfGenerator;
import com.salesmgm.app.utils.SmsHelper;
import com.salesmgm.app.utils.ToastUtils;

import java.io.File;
import java.util.Locale;

public class BillDetailActivity extends AppCompatActivity {

    private TextView tvDetailInvoiceNo, tvDetailBillDate, tvDetailBillCustomer, tvDetailBillMode;
    private TextView tvDetailBillTotal, tvDetailBillPaid, tvDetailBillDue;
    private Button btnGeneratePdf, btnSendBillSms, btnEditBill, btnDeleteBill;
    private RecyclerView rvBillDetailItems;

    private DatabaseHelper dbHelper;
    private long billId = -1;
    private Bill bill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        dbHelper = new DatabaseHelper(this);

        tvDetailInvoiceNo = findViewById(R.id.tvDetailInvoiceNo);
        tvDetailBillDate = findViewById(R.id.tvDetailBillDate);
        tvDetailBillCustomer = findViewById(R.id.tvDetailBillCustomer);
        tvDetailBillMode = findViewById(R.id.tvDetailBillMode);
        tvDetailBillTotal = findViewById(R.id.tvDetailBillTotal);
        tvDetailBillPaid = findViewById(R.id.tvDetailBillPaid);
        tvDetailBillDue = findViewById(R.id.tvDetailBillDue);
        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);
        btnSendBillSms = findViewById(R.id.btnSendBillSms);
        btnEditBill = findViewById(R.id.btnEditBill);
        btnDeleteBill = findViewById(R.id.btnDeleteBill);
        rvBillDetailItems = findViewById(R.id.rvBillDetailItems);

        rvBillDetailItems.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent().hasExtra("BILL_ID")) {
            billId = getIntent().getLongExtra("BILL_ID", -1);
        }

        loadBillDetails();

        btnGeneratePdf.setOnClickListener(v -> generateAndSharePdf());
        btnSendBillSms.setOnClickListener(v -> sendBillSms());
        btnEditBill.setOnClickListener(v -> editInvoice());
        btnDeleteBill.setOnClickListener(v -> deleteInvoiceWithConfirmation());
    }

    private void loadBillDetails() {
        if (billId == -1) return;

        bill = dbHelper.getBillById(billId);
        if (bill != null) {
            tvDetailInvoiceNo.setText("Invoice: " + bill.getInvoiceNumber());
            tvDetailBillDate.setText("Date: " + bill.getTimestamp());
            tvDetailBillCustomer.setText("Customer: " + (bill.getCustomerName() != null ? bill.getCustomerName() : "Walk-in"));
            tvDetailBillMode.setText("Payment Mode: " + bill.getPaymentMode());
            tvDetailBillTotal.setText(String.format(Locale.getDefault(), "Total Amount: ₹%.2f", bill.getTotalAmount()));
            tvDetailBillPaid.setText(String.format(Locale.getDefault(), "Paid Amount: ₹%.2f", bill.getPaidAmount()));
            tvDetailBillDue.setText(String.format(Locale.getDefault(), "Due Balance: ₹%.2f", bill.getDueAmount()));

            CartAdapter adapter = new CartAdapter(bill.getItems(), null);
            rvBillDetailItems.setAdapter(adapter);
        }
    }

    private void editInvoice() {
        if (bill == null) return;
        Intent intent = new Intent(this, CreateBillActivity.class);
        intent.putExtra("EDIT_BILL_ID", bill.getId());
        startActivity(intent);
        finish();
    }

    private void deleteInvoiceWithConfirmation() {
        if (bill == null) return;
        new AlertDialog.Builder(this)
                .setTitle("🗑️ Delete Invoice")
                .setMessage("Are you sure you want to delete invoice '" + bill.getInvoiceNumber() + "'? Inventory stock will be automatically restored.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = dbHelper.deleteBill(bill.getId());
                    if (deleted) {
                        ToastUtils.showToast(BillDetailActivity.this, "Invoice deleted & stock restored!");
                        finish();
                    } else {
                        ToastUtils.showToast(BillDetailActivity.this, "Failed to delete invoice.");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void generateAndSharePdf() {
        if (bill == null) return;
        try {
            File pdfFile = PdfGenerator.generateInvoicePdf(this, bill);
            ToastUtils.showToast(this, "PDF generated: " + pdfFile.getName());

            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
            Intent shareIntent = new Intent(Intent.ACTION_VIEW);
            shareIntent.setDataAndType(contentUri, "application/pdf");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Open Invoice PDF"));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showToast(this, "Failed to generate PDF: " + e.getMessage());
        }
    }

    private void sendBillSms() {
        if (bill == null) return;
        if (bill.getCustomerPhone() == null || bill.getCustomerPhone().isEmpty()) {
            ToastUtils.showToast(this, "No customer mobile number linked to this invoice.");
            return;
        }

        String msg = SmsHelper.buildBillConfirmationMessage(bill.getCustomerName(), bill.getInvoiceNumber(), bill.getTotalAmount(), bill.getDueAmount());
        SmsHelper.sendSms(this, bill.getCustomerPhone(), msg);
    }
}

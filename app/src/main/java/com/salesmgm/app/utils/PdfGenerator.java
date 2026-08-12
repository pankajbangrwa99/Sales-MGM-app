package com.salesmgm.app.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.salesmgm.app.models.Bill;
import com.salesmgm.app.models.BillItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class PdfGenerator {

    public static File generateInvoicePdf(Context context, Bill bill) throws IOException {
        ShopSettingsManager settingsManager = new ShopSettingsManager(context);
        String shopName = settingsManager.getShopName();
        String billingDesc = settingsManager.getBillingDescription();
        String bankDetails = settingsManager.getBankDetails();

        PdfDocument pdfDocument = new PdfDocument();

        // Standard A4 width: 595 pt, height: 842 pt
        int pageWidth = 595;
        int pageHeight = 842;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint tableHeaderPaint = new Paint();

        // Header Background
        paint.setColor(Color.parseColor("#0F172A"));
        canvas.drawRect(0, 0, pageWidth, 95, paint);

        // Header Title (Shop Name)
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(20);
        titlePaint.setFakeBoldText(true);
        canvas.drawText(shopName != null && !shopName.isEmpty() ? shopName.toUpperCase() : "RETAIL SALES INVOICE", 30, 42, titlePaint);

        // Subtitle (Offline Billing Description)
        titlePaint.setTextSize(11);
        titlePaint.setFakeBoldText(false);
        canvas.drawText(billingDesc != null && !billingDesc.isEmpty() ? billingDesc : "Offline Billing & Management System", 30, 68, titlePaint);

        // Invoice Meta Info Box
        paint.setColor(Color.parseColor("#0F172A"));
        paint.setTextSize(11);
        paint.setFakeBoldText(true);

        canvas.drawText("Invoice No: " + bill.getInvoiceNumber(), 30, 120, paint);
        canvas.drawText("Date: " + bill.getTimestamp(), 350, 120, paint);

        paint.setFakeBoldText(false);
        canvas.drawText("Customer Name: " + (bill.getCustomerName() != null ? bill.getCustomerName() : "Walk-in Customer"), 30, 140, paint);
        canvas.drawText("Customer Phone: " + (bill.getCustomerPhone() != null ? bill.getCustomerPhone() : "N/A"), 30, 158, paint);
        canvas.drawText("Payment Mode: " + bill.getPaymentMode(), 350, 140, paint);

        // Divider Line
        paint.setColor(Color.parseColor("#CBD5E1"));
        paint.setStrokeWidth(1);
        canvas.drawLine(30, 175, pageWidth - 30, 175, paint);

        // Table Headers
        tableHeaderPaint.setColor(Color.parseColor("#334155"));
        tableHeaderPaint.setTextSize(11);
        tableHeaderPaint.setFakeBoldText(true);

        int startY = 200;
        canvas.drawText("#", 30, startY, tableHeaderPaint);
        canvas.drawText("Item Name", 65, startY, tableHeaderPaint);
        canvas.drawText("Unit Price", 320, startY, tableHeaderPaint);
        canvas.drawText("Qty", 420, startY, tableHeaderPaint);
        canvas.drawText("Total (₹)", 490, startY, tableHeaderPaint);

        canvas.drawLine(30, startY + 8, pageWidth - 30, startY + 8, paint);

        // Table Rows
        paint.setColor(Color.BLACK);
        paint.setTextSize(10);
        int currentY = startY + 30;
        int itemNumber = 1;

        for (BillItem item : bill.getItems()) {
            canvas.drawText(String.valueOf(itemNumber++), 30, currentY, paint);

            String pName = item.getProductName();
            if (pName.length() > 32) pName = pName.substring(0, 29) + "...";
            canvas.drawText(pName, 65, currentY, paint);

            canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", item.getUnitPrice()), 320, currentY, paint);
            canvas.drawText(String.valueOf(item.getQuantity()), 420, currentY, paint);
            canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", item.getTotalPrice()), 490, currentY, paint);

            currentY += 22;
            if (currentY > pageHeight - 200) break;
        }

        canvas.drawLine(30, currentY + 5, pageWidth - 30, currentY + 5, paint);

        // Totals Summary Box
        int summaryY = currentY + 30;
        paint.setFakeBoldText(true);
        paint.setTextSize(11);

        canvas.drawText("Grand Total:", 320, summaryY, paint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", bill.getTotalAmount()), 490, summaryY, paint);

        summaryY += 18;
        canvas.drawText("Amount Paid:", 320, summaryY, paint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", bill.getPaidAmount()), 490, summaryY, paint);

        summaryY += 18;
        paint.setColor(bill.getDueAmount() > 0 ? Color.parseColor("#DC2626") : Color.parseColor("#059669"));
        canvas.drawText("Due Balance:", 320, summaryY, paint);
        canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", bill.getDueAmount()), 490, summaryY, paint);

        // Bank Details & Payment Info Footer Box
        int bankBoxY = pageHeight - 110;
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#F8FAFC"));
        canvas.drawRect(30, bankBoxY, pageWidth - 30, pageHeight - 45, bgPaint);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#E2E8F0"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);
        canvas.drawRect(30, bankBoxY, pageWidth - 30, pageHeight - 45, borderPaint);

        Paint footerTextPaint = new Paint();
        footerTextPaint.setColor(Color.parseColor("#0F172A"));
        footerTextPaint.setTextSize(9);
        footerTextPaint.setFakeBoldText(true);
        canvas.drawText("BANK & CREDIT PAYMENT DETAILS:", 40, bankBoxY + 18, footerTextPaint);

        footerTextPaint.setFakeBoldText(false);
        footerTextPaint.setColor(Color.parseColor("#334155"));
        String bDetails = bankDetails != null && !bankDetails.isEmpty() ? bankDetails : "Bank: HDFC Bank | A/C: 987654321012 | IFSC: HDFC0001234 | UPI: shopkeeper@upi";
        canvas.drawText(bDetails, 40, bankBoxY + 34, footerTextPaint);

        footerTextPaint.setColor(Color.parseColor("#64748B"));
        footerTextPaint.setTextSize(8);
        canvas.drawText("Thank you for your business! For queries, contact store administration.", 40, bankBoxY + 52, footerTextPaint);

        pdfDocument.finishPage(page);

        // Output File
        File folder = new File(context.getExternalFilesDir(null), "Invoices");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, "Invoice_" + bill.getInvoiceNumber() + ".pdf");
        FileOutputStream fos = new FileOutputStream(file);
        pdfDocument.writeTo(fos);
        pdfDocument.close();
        fos.close();

        return file;
    }
}

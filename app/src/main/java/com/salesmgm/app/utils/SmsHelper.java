package com.salesmgm.app.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class SmsHelper {

    public static boolean checkSmsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean sendSms(Context context, String phoneNumber, String message) {
        if (!checkSmsPermission(context)) {
            ToastUtils.showToast(context, "SMS permission is required to send payment receipts.");
            return false;
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            ToastUtils.showToast(context, "Invalid customer phone number.");
            return false;
        }

        try {
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                smsManager = context.getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }
            if (smsManager == null) {
                smsManager = SmsManager.getDefault();
            }

            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber.trim(), null, parts, null, null);
            ToastUtils.showToast(context, "📱 SMS Receipt sent to " + phoneNumber.trim());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showToast(context, "Failed to send SMS: " + e.getMessage());
            return false;
        }
    }

    public static String buildPaymentReminderMessage(String customerName, double dueBalance, String shopName) {
        return String.format(Locale.getDefault(),
                "Dear %s, your total pending balance at %s is ₹%.2f. Kindly arrange the payment at your earliest. Thank you!",
                customerName, (shopName != null ? shopName : "our shop"), dueBalance);
    }

    public static String buildBillConfirmationMessage(String customerName, String invoiceNo, double totalAmount, double dueAmount) {
        if (dueAmount > 0) {
            return String.format(Locale.getDefault(),
                    "Dear %s, thank you for shopping! Invoice: %s. Total: ₹%.2f. Remaining due: ₹%.2f.",
                    customerName, invoiceNo, totalAmount, dueAmount);
        } else {
            return String.format(Locale.getDefault(),
                    "Dear %s, thank you for shopping! Invoice: %s. Total: ₹%.2f. Paid in full.",
                    customerName, invoiceNo, totalAmount);
        }
    }
}

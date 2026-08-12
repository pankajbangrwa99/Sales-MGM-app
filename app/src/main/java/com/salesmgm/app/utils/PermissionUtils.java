package com.salesmgm.app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    public static final int REQUEST_CODE_PERMISSIONS = 1001;

    public static boolean checkAndRequestPermissions(Activity activity) {
        if (activity == null) return false;

        List<String> permissionsNeeded = new ArrayList<>();

        // Notification permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // SMS permission for sending SIM SMS billing confirmations & credit reminders
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        }

        // Phone state permission
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
            return false; // Prompt dialog shown to user
        }

        return true; // All permissions already granted
    }
}

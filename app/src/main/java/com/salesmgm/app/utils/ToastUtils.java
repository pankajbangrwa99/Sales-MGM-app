package com.salesmgm.app.utils;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Toast;

public class ToastUtils {

    private static Toast currentToast = null;
    private static String lastMessage = "";
    private static long lastToastTime = 0;
    private static final long DEBOUNCE_INTERVAL_MS = 1500; // Suppress duplicate warnings within 1.5s

    public static void showToast(Context context, String message) {
        if (context == null || message == null || message.trim().isEmpty()) return;

        long now = SystemClock.elapsedRealtime();

        // Suppress exact duplicate warnings shown within the debounce interval
        if (message.equals(lastMessage) && (now - lastToastTime < DEBOUNCE_INTERVAL_MS)) {
            return;
        }

        // Immediately cancel any currently displaying toast so messages do not queue up
        if (currentToast != null) {
            currentToast.cancel();
        }

        currentToast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
        lastMessage = message;
        lastToastTime = now;
        currentToast.show();
    }
}

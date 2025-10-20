package com.tomtomkenya.africanludo.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Handles Android 13+ POST_NOTIFICATIONS permission prompts without leaking activity logic everywhere.
 */
public class NotificationPermissionHelper {

    private final Activity activity;
    private final ActivityResultLauncher<String> launcher;

    public NotificationPermissionHelper(@NonNull Activity activity, @NonNull Runnable onGranted) {
        this.activity = activity;
        this.launcher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                onGranted.run();
            }
        });
    }

    /**
     * Ask the user for notification permission when we expect to deliver real-time match updates.
     */
    public void ensurePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
    }
}

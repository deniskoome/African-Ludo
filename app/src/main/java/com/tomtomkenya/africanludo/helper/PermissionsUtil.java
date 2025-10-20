package com.tomtomkenya.africanludo.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class PermissionsUtil {

    private PermissionsUtil() {
        // Utility class
    }

    public static boolean hasImageReadPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestImageReadPermission(@NonNull Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, getImagePermissions(), requestCode);
    }

    @NonNull
    private static String[] getImagePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }
}

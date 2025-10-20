package com.tomtomkenya.africanludo.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import java.util.List;

public class NotificationUtils {

    public NotificationUtils() {
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }

        return isInBackground;
    }

    public void playNotificationSound(Context context) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility invoked by the modern messaging service to map RemoteMessage data into a serializable Map.
     */
    public static Map<String, String> mapFromBundle(@NonNull Map<String, String> payload) {
        return new HashMap<>(payload);
    }

    /**
     * Simplified helper name for foreground playback when handling data messages.
     */
    public static void playInAppTone(@NonNull Context context) {
        new NotificationUtils().playNotificationSound(context);
    }

}

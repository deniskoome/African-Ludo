package com.tomtomkenya.africanludo.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.activity.MainActivity;
import com.tomtomkenya.africanludo.helper.AppConstant;
import com.tomtomkenya.africanludo.utils.NotificationUtils;
import com.tomtomkenya.africanludo.services.PushTokenManager;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles modern FCM callbacks using data-only messages so that gameplay payloads arrive
 * consistently in both foreground and background states.
 */
public class LudoFcmService extends FirebaseMessagingService {

    private static final String CHANNEL_GAMEPLAY = "gameplay";
    private static final AtomicInteger notificationId = new AtomicInteger(1000);

    /**
     * Called by FCM whenever Google refreshes the registration token. We immediately
     * forward the new value to our backend and persist it locally for logout handling.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        new PushTokenManager(this).registerToken(token);
    }

    /**
     * Receives gameplay data messages and routes them to the UI or notification tray.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        if (data.isEmpty()) {
            return;
        }
        String type = data.get("type");
        String title = data.get("title");
        String body = data.get("body");

        Intent updateIntent = new Intent(AppConstant.PUSH_NOTIFICATION);
        updateIntent.putExtra("type", type);
        updateIntent.putExtra("title", title);
        updateIntent.putExtra("body", body);
        updateIntent.putExtra("metadata", NotificationUtils.mapFromBundle(data));

        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
            if (!TextUtils.isEmpty(body)) {
                NotificationUtils.playInAppTone(this);
            }
        } else {
            showSystemNotification(type, title, body, data);
        }
    }

    /**
     * Build a heads-up notification so players see match updates even when the app is backgrounded.
     */
    private void showSystemNotification(String type, String title, String body, Map<String, String> data) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        ensureChannel(manager);
        PendingIntent pendingIntent = buildPendingIntent(data);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_GAMEPLAY)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(!TextUtils.isEmpty(title) ? title : getString(R.string.app_name))
                .setContentText(body)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);
        if (isHeadsUpType(type)) {
            builder.setFullScreenIntent(pendingIntent, true);
        }
        manager.notify(notificationId.incrementAndGet(), builder.build());
    }

    /**
     * Create the PendingIntent that deep links back into the gameplay flow using immutable flags.
     */
    private PendingIntent buildPendingIntent(Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("click_action", data.get("click_action"));
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(this, 0, intent, flags);
    }

    /**
     * Ensure the gameplay channel exists so notifications follow Android 8+ requirements.
     */
    private void ensureChannel(NotificationManagerCompat manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_GAMEPLAY,
                    getString(R.string.notification_channel_gameplay),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(getString(R.string.notification_channel_gameplay_description));
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Heads-up notifications are shown for urgent gameplay events such as match start.
     */
    private boolean isHeadsUpType(String type) {
        return "match_found".equals(type) || "countdown_sync".equals(type) || "wallet_credit".equals(type);
    }
}

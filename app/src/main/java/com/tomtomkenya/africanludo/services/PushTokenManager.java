package com.tomtomkenya.africanludo.services;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.tomtomkenya.africanludo.MyApplication;
import com.tomtomkenya.africanludo.api.ApiCalling;
import com.tomtomkenya.africanludo.helper.Preferences;
import com.tomtomkenya.africanludo.model.UserModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Centralizes token registration so the messaging service and activities all follow the
 * new backend contract introduced for FCM v1. This keeps the migration in one place.
 */
public class PushTokenManager {

    private final ApiCalling api;
    private final Preferences preferences;

    public PushTokenManager(@NonNull Context context) {
        this.api = MyApplication.getRetrofit().create(ApiCalling.class);
        this.preferences = Preferences.getInstance(context);
    }

    /**
     * Persist the token locally and inform the backend so it can send gameplay pushes.
     */
    public void registerToken(@NonNull String token) {
        preferences.setString(Preferences.KEY_DEVICE_TOKEN, token);
        String userId = preferences.getString(Preferences.KEY_USER_ID);
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        Call<UserModel> call = api.registerDeviceToken(userId, token);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                // No-op: backend acknowledgement is not required for foreground UX.
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                // Intentionally left blank: retry handled when token is reused.
            }
        });
    }

    /**
     * Remove the token from backend storage when the user signs out or the device is reset.
     */
    public void unregisterToken() {
        String token = preferences.getString(Preferences.KEY_DEVICE_TOKEN);
        String userId = preferences.getString(Preferences.KEY_USER_ID);
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(token)) {
            return;
        }
        Call<UserModel> call = api.deleteDeviceToken(userId, token);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                // Token removed successfully.
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                // Failure will be retried when a new token is generated.
            }
        });
    }
}

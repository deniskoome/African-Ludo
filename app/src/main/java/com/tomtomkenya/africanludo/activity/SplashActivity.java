package com.tomtomkenya.africanludo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.tomtomkenya.africanludo.BuildConfig;
import com.tomtomkenya.africanludo.MyApplication;
import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.api.ApiCalling;
import com.tomtomkenya.africanludo.helper.AppConstant;
import com.tomtomkenya.africanludo.helper.Function;
import com.tomtomkenya.africanludo.helper.Preferences;
import com.tomtomkenya.africanludo.model.AppModel;
import com.tomtomkenya.africanludo.model.UserModel;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ApiCalling api;
    private TextView statusTv;
    private String forceUpdate, whatsNew, updateDate, latestVersionName, latestVersionCode, updateUrl;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        api = MyApplication.getRetrofit().create(ApiCalling.class);

        changeStatusBarColor();
        printHashKey();

        statusTv = findViewById(R.id.statusTv);

        if(Function.checkNetworkConnection(SplashActivity.this)) {
            if (Preferences.getInstance(SplashActivity.this).getString(Preferences.KEY_USER_ID) != null) {
                updateUserProfileFCM();
            }

            getAppDetails();
        }
        else {
            statusTv.setText("No internet Connection, please try again later.");
        }

    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorAccent));
    }

    private void printHashKey() {
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void updateUserProfileFCM() {
        if (Function.checkNetworkConnection(SplashActivity.this)) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Get new FCM registration token
                    String token = task.getResult();

                    Call<UserModel> callToken = api.updateUserProfileToken(Preferences.getInstance(SplashActivity.this).getString(Preferences.KEY_USER_ID), token);
                    callToken.enqueue(new Callback<UserModel>() {
                        @Override
                        public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {

                        }

                        @Override
                        public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {

                        }
                    });
                }
            });
        }
    }

    private void getAppDetails() {
        Call<AppModel> call = api.getAppDetails();
        call.enqueue(new Callback<AppModel>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<AppModel> call, @NonNull Response<AppModel> response) {
                if (response.isSuccessful()) {
                    AppModel legalData = response.body();
                    if (legalData != null) {
                        List<AppModel.Result> res = legalData.getResult();
                        if (res != null && !res.isEmpty()) {
                            AppModel.Result result = res.get(0);
                            if (result.getSuccess() == 1) {
                                AppConstant.COUNTRY_CODE = result.getCountry_code();
                                AppConstant.CURRENCY_CODE = result.getCurrency_code();
                                AppConstant.CURRENCY_SIGN = result.getCurrency_sign();
                                AppConstant.PAYTM_M_ID = result.getPaytm_mer_id();
                                AppConstant.PAYU_M_ID = result.getPayu_id();
                                AppConstant.PAYU_M_KEY = result.getPayu_key();

                                String mpesaShortcode = result.getMpesa_shortcode();
                                String mpesaPasskey = result.getMpesa_passkey();
                                String mpesaCallbackUrl = result.getMpesa_callback_url();
                                String mastercardMerchantId = result.getMastercard_merchant_id();
                                String mastercardMerchantKey = result.getMastercard_merchant_key();

                                AppConstant.MPESA_SHORTCODE = TextUtils.isEmpty(mpesaShortcode) ? null : mpesaShortcode;
                                AppConstant.MPESA_PASSKEY = TextUtils.isEmpty(mpesaPasskey) ? null : mpesaPasskey;
                                AppConstant.MPESA_CALLBACK_URL = TextUtils.isEmpty(mpesaCallbackUrl) ? null : mpesaCallbackUrl;
                                AppConstant.MASTERCARD_MERCHANT_ID = TextUtils.isEmpty(mastercardMerchantId) ? null : mastercardMerchantId;
                                AppConstant.MASTERCARD_MERCHANT_KEY = TextUtils.isEmpty(mastercardMerchantKey) ? null : mastercardMerchantKey;

                                AppConstant.MIN_JOIN_LIMIT = result.getMin_entry_fee();
                                AppConstant.REFERRAL_PERCENTAGE = result.getRefer_percentage();
                                AppConstant.MAINTENANCE_MODE = result.getMaintenance_mode();
                                AppConstant.MODE_OF_PAYMENT = result.getMop();
                                AppConstant.WALLET_MODE = result.getWallet_mode();
                                AppConstant.MIN_WITHDRAW_LIMIT = result.getMin_withdraw();
                                AppConstant.MAX_WITHDRAW_LIMIT = result.getMax_withdraw();
                                AppConstant.MIN_DEPOSIT_LIMIT = result.getMin_deposit();
                                AppConstant.MAX_DEPOSIT_LIMIT = result.getMax_deposit();
                                AppConstant.GAME_NAME = result.getGame_name();
                                AppConstant.PACKAGE_NAME = result.getPackage_name();
                                AppConstant.HOW_TO_PLAY = result.getHow_to_play();
                                AppConstant.SUPPORT_EMAIL = result.getCus_support_email();
                                AppConstant.SUPPORT_MOBILE = result.getCus_support_mobile();

                                forceUpdate = result.getForce_update();
                                whatsNew = result.getWhats_new();
                                updateDate = result.getUpdate_date();
                                latestVersionName = result.getLatest_version_name();
                                latestVersionCode = result.getLatest_version_code();
                                updateUrl = result.getUpdate_url();

                                try {
                                    if (BuildConfig.VERSION_CODE < Integer.parseInt(latestVersionCode)) {
                                        if (forceUpdate.equals("1")) {
                                            Intent intent = new Intent(SplashActivity.this, UpdateAppActivity.class);
                                            intent.putExtra("forceUpdate", forceUpdate);
                                            intent.putExtra("whatsNew", whatsNew);
                                            intent.putExtra("updateDate", updateDate);
                                            intent.putExtra("latestVersionName", latestVersionName);
                                            intent.putExtra("updateURL", updateUrl);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        } else if (forceUpdate.equals("0")) {
                                            Intent intent = new Intent(SplashActivity.this, UpdateAppActivity.class);
                                            intent.putExtra("forceUpdate", forceUpdate);
                                            intent.putExtra("whatsNew", whatsNew);
                                            intent.putExtra("updateDate", updateDate);
                                            intent.putExtra("latestVersionName", latestVersionName);
                                            intent.putExtra("updateURL", updateUrl);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    }
                                    else if (AppConstant.MAINTENANCE_MODE == 0) {
                                        new Handler().postDelayed(() -> {
                                            if (Preferences.getInstance(SplashActivity.this).getString(Preferences.KEY_IS_AUTO_LOGIN).equals("1")) {
                                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                                intent.putExtra("finish", true);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            } else {
                                                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                                intent.putExtra("finish", true);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                            finish();
                                        },1000);
                                    }
                                    else {
                                        statusTv.setText("App is under maintenance, please try again later.");
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AppModel> call, @NonNull Throwable t) {

            }
        });
    }
}
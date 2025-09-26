package com.tomtomkenya.africanludo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tomtomkenya.africanludo.BuildConfig;
import com.tomtomkenya.africanludo.MyApplication;
import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.api.ApiCalling;
import com.tomtomkenya.africanludo.helper.AppConstant;
import com.tomtomkenya.africanludo.helper.Function;
import com.tomtomkenya.africanludo.helper.Preferences;
import com.tomtomkenya.africanludo.helper.ProgressBar;
import com.tomtomkenya.africanludo.model.Token;
import com.tomtomkenya.africanludo.model.UserModel;
//import com.definiteautomation.dreamludo.payu.ServiceWrapper;
import com.google.android.material.textfield.TextInputEditText;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;
//import com.payumoney.core.PayUmoneySdkInitializer;
//import com.payumoney.core.entity.TransactionResponse;
//import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class DepositActivity extends AppCompatActivity implements PaymentResultListener {

    public RadioGroup radioGroup;
    public RadioButton payTmRb, payuRb, flutterWaveRb;
    private TextInputEditText amountEt;
    public TextView signTv, noteTv, alertTv;
    private Button submitBt;

    private ProgressBar progressBar;
    private ApiCalling api;

    private String amountSt;
    private String mopSt = BuildConfig.ENABLE_PAYTM_SDK ? "PayTm" : "RazorPay";
    public String orderIdSt, paymentIdSt, checksumSt, tokenSt;

    private static final String TAG = DepositActivity.class.getSimpleName();
    private final Integer activityRequestCode = 2;

    //private final PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();
    //private PayUmoneySdkInitializer.PaymentParam paymentParam = null;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);
        api = MyApplication.getRetrofit().create(ApiCalling.class);
        progressBar = new ProgressBar(this, false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getUserDetails();

        radioGroup = findViewById(R.id.radioGroup);
        payTmRb = findViewById(R.id.payTmRb);
        payuRb = findViewById(R.id.payuRb);
        flutterWaveRb = findViewById(R.id.flutterWaveRb);
        amountEt = findViewById(R.id.amountEt);
        noteTv = findViewById(R.id.noteTv);
        alertTv = findViewById(R.id.alertTv);
        signTv = findViewById(R.id.signTv);
        submitBt = findViewById(R.id.submitBt);

        signTv.setText(AppConstant.CURRENCY_SIGN);
        alertTv.setText(String.format("Minimum Add Amount is %s%d", AppConstant.CURRENCY_SIGN, AppConstant.MIN_DEPOSIT_LIMIT));

        if (BuildConfig.ENABLE_PAYTM_SDK) {
            payTmRb.setOnClickListener(v -> mopSt = "PayTm");
        } else {
            payTmRb.setOnClickListener(v -> Toast.makeText(this, R.string.paytm_disabled_message, Toast.LENGTH_SHORT).show());
        }

        //payuRb.setOnClickListener(v -> mopSt = "PayUMoney");

        flutterWaveRb.setOnClickListener(v -> {
            switch (AppConstant.MODE_OF_PAYMENT) {
                case AppConstant.PAYMENT_GATEWAY_MPESA:
                    mopSt = "Mpesa";
                    break;
                case AppConstant.PAYMENT_GATEWAY_MASTERCARD:
                    mopSt = "Mastercard";
                    break;
                default:
                    mopSt = "RazorPay";
                    break;
            }
        });

        switch (AppConstant.MODE_OF_PAYMENT) {
            case AppConstant.PAYMENT_GATEWAY_PAYTM:
                radioGroup.setVisibility(View.GONE);
                payTmRb.setVisibility(BuildConfig.ENABLE_PAYTM_SDK ? View.VISIBLE : View.GONE);
                payuRb.setVisibility(View.GONE);
                flutterWaveRb.setVisibility(View.GONE);
                mopSt = BuildConfig.ENABLE_PAYTM_SDK ? "PayTm" : "RazorPay";
                break;
            case AppConstant.PAYMENT_GATEWAY_PAYU:
                radioGroup.setVisibility(View.GONE);
                payTmRb.setVisibility(View.GONE);
                payuRb.setVisibility(View.VISIBLE);
                flutterWaveRb.setVisibility(View.GONE);
                mopSt = "PayUMoney";
                break;
            case AppConstant.PAYMENT_GATEWAY_RAZORPAY:
                radioGroup.setVisibility(View.GONE);
                payTmRb.setVisibility(View.GONE);
                payuRb.setVisibility(View.GONE);
                flutterWaveRb.setVisibility(View.VISIBLE);
                flutterWaveRb.setText(R.string.flutterwave);
                mopSt = "RazorPay";
                break;
            case AppConstant.PAYMENT_GATEWAY_MPESA:
                radioGroup.setVisibility(View.GONE);
                payTmRb.setVisibility(View.GONE);
                payuRb.setVisibility(View.GONE);
                flutterWaveRb.setVisibility(View.VISIBLE);
                flutterWaveRb.setText(R.string.mpesa);
                mopSt = "Mpesa";
                break;
            case AppConstant.PAYMENT_GATEWAY_MASTERCARD:
                radioGroup.setVisibility(View.GONE);
                payTmRb.setVisibility(View.GONE);
                payuRb.setVisibility(View.GONE);
                flutterWaveRb.setVisibility(View.VISIBLE);
                flutterWaveRb.setText(R.string.mastercard);
                mopSt = "Mastercard";
                break;
            default:
                radioGroup.setVisibility(View.VISIBLE);
                payTmRb.setVisibility(BuildConfig.ENABLE_PAYTM_SDK ? View.VISIBLE : View.GONE);
                payuRb.setVisibility(View.VISIBLE);
                flutterWaveRb.setVisibility(View.VISIBLE);codex/add-payment-methods-in-depositactivity-d2jude
                flutterWaveRb.setText(R.string.flutterwave);
                mopSt = "PayTm";
=======
                mopSt = BuildConfig.ENABLE_PAYTM_SDK ? "PayTm" : "RazorPay"
                break;
        }

        submitBt.setOnClickListener(v -> {
            submitBt.setEnabled(false);
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Random rand = new Random();
            int min =1000, max= 9999;

            // nextInt as provided by Random is exclusive of the top value so you need to add 1
            int randomNum = rand.nextInt((max - min) + 1) + min;
            orderIdSt = System.currentTimeMillis() + randomNum + Preferences.getInstance(this).getString(Preferences.KEY_USER_ID);
            paymentIdSt = orderIdSt;

            amountSt = Objects.requireNonNull(amountEt.getText()).toString();
            if (!amountSt.isEmpty()) {
                double payout = Integer.parseInt(amountEt.getText().toString());

                if (payout < AppConstant.MIN_DEPOSIT_LIMIT) {
                    submitBt.setEnabled(true);
                    alertTv.setVisibility(View.VISIBLE);
                    alertTv.setText(String.format("Minimum Add Amount is %s%d", AppConstant.CURRENCY_SIGN, AppConstant.MIN_DEPOSIT_LIMIT));
                    alertTv.setTextColor(Color.parseColor("#ff0000"));
                } else if (payout > AppConstant.MAX_DEPOSIT_LIMIT) {
                    submitBt.setEnabled(true);
                    alertTv.setVisibility(View.VISIBLE);
                    alertTv.setText(String.format("Maximum Add Amount is %s%d", AppConstant.CURRENCY_SIGN, AppConstant.MAX_DEPOSIT_LIMIT));
                    alertTv.setTextColor(Color.parseColor("#ff0000"));
                } else {
                    alertTv.setVisibility(View.GONE);
                    try {
                        submitBt.setEnabled(false);
                        switch (mopSt) {
                            case "PayTm":
                                if (BuildConfig.ENABLE_PAYTM_SDK) {
                                    getPayTmToken();
                                } else {
                                    submitBt.setEnabled(true);
                                    Toast.makeText(DepositActivity.this, R.string.paytm_disabled_message, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            //case "PayUMoney":
                            //    startPayUMoney();
                            //    break;
                            case "RazorPay":
                                startRazorPay();
                                break;
                            case "Mpesa":
                                startMpesa();
                                break;
                            case "Mastercard":
                                startMastercard();
                                break;
                        }
                    } catch (NullPointerException e) {
                        submitBt.setEnabled(true);
                    }
                }
            } else {
                submitBt.setEnabled(true);
                alertTv.setVisibility(View.VISIBLE);
                alertTv.setText(String.format("Minimum Add Amount is %s%d", AppConstant.CURRENCY_SIGN, AppConstant.MIN_DEPOSIT_LIMIT));
                alertTv.setTextColor(Color.parseColor("#ff0000"));
            }
        });

    }

    private void getUserDetails() {
        Call<UserModel> call = api.getUserDetails(Preferences.getInstance(this).getString(Preferences.KEY_USER_ID));
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                if (response.isSuccessful()) {
                    UserModel legalData = response.body();
                    List<UserModel.Result> res;
                    if (legalData != null) {
                        res = legalData.getResult();
                        if (res.get(0).getSuccess() == "1") {
                            if (res.get(0).getIs_block() == 1) {
                                Preferences.getInstance(DepositActivity.this).setString(Preferences.KEY_IS_AUTO_LOGIN,"0");

                                Intent i = new Intent(DepositActivity.this, LoginActivity.class);
                                i.putExtra("finish", true);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            }
                            else if (res.get(0).getIs_active() == 0) {
                                Preferences.getInstance(DepositActivity.this).setString(Preferences.KEY_IS_AUTO_LOGIN,"0");

                                Intent i = new Intent(DepositActivity.this, LoginActivity.class);
                                i.putExtra("finish", true);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {

            }
        });
    }

    private void postDeposit() {
        progressBar.showProgressDialog();
        Call<UserModel> call = api.postDeposit(Preferences.getInstance(this).getString(Preferences.KEY_USER_ID), orderIdSt, paymentIdSt, checksumSt, Double.parseDouble(amountSt), mopSt );
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                progressBar.hideProgressDialog();

                if (response.isSuccessful()) {
                    UserModel legalData = response.body();
                    List<UserModel.Result> res;
                    if (legalData != null) {
                        res = legalData.getResult();
                        Function.showToast(DepositActivity.this, res.get(0).getMsg());
                        onBackPressed();
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                progressBar.hideProgressDialog();
            }
        });
    }

    private  void getPayTmToken(){
        Log.e(TAG, " get token start");
        progressBar.showProgressDialog();
        if (Function.checkNetworkConnection(DepositActivity.this)) {
            Call<Token> callToken = api.generateTokenCall("12345", AppConstant.PAYTM_M_ID, orderIdSt, amountSt);
            callToken.enqueue(new Callback<Token>() {
                @Override
                public void onResponse(@NonNull Call<Token> call, @NonNull Response<Token> response) {
                    Log.e(TAG, " respo "+ response.isSuccessful() );
                    try {
                        if (response.isSuccessful() && response.body()!=null){
                            if (!response.body().getBody().getTxnToken().equals("")) {
                                progressBar.hideProgressDialog();
                                Log.e(TAG, " transaction token : "+response.body().getBody().getTxnToken());
                                startPaytmPayment(response.body().getBody().getTxnToken());
                            }else {
                                Log.e(TAG, " Token status false");
                                progressBar.hideProgressDialog();
                            }
                        }
                    }catch (Exception e){
                        Log.e(TAG, " error in Token Res "+e.getMessage());
                        progressBar.hideProgressDialog();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Token> call, @NonNull Throwable t) {
                    Log.e(TAG, " response error "+t.getMessage());
                    progressBar.hideProgressDialog();
                }
            });
        }
    }

    public void startPaytmPayment (String token){
        tokenSt = token;
        // for test mode use it
        //String host = "https://securegw-stage.paytm.in/";

        // for production mode use it
        String host = "https://securegw.paytm.in/";

        String callBackUrl = host + "theia/paytmCallback?ORDER_ID="+orderIdSt;
        Log.e(TAG, " callback URL "+callBackUrl);
        PaytmOrder paytmOrder = new PaytmOrder(orderIdSt, AppConstant.PAYTM_M_ID, tokenSt, amountSt, callBackUrl);
        TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback(){
            @Override
            public void onTransactionResponse(Bundle bundle) {
                Log.e(TAG, "Response (onTransactionResponse) : "+bundle.toString());

                String orderId = bundle.getString("ORDERID");
                String status = bundle.getString("STATUS");
                String txnId = bundle.getString("TXNID");
                String checksum = bundle.getString("CHECKSUMHASH");

                if(Objects.requireNonNull(status).equalsIgnoreCase("TXN_SUCCESS")) {
                    orderIdSt = orderId;
                    paymentIdSt = txnId;
                    checksumSt =checksum;

                    postDeposit();
                }
            }

            @Override
            public void networkNotAvailable() {
                Log.e(TAG, "network not available ");
            }

            @Override
            public void onErrorProceed(String s) {
                Log.e(TAG, " onErrorProcess "+ s);
            }

            @Override
            public void clientAuthenticationFailed(String s) {
                Log.e(TAG, "Clientauth "+s);
            }

            @Override
            public void someUIErrorOccurred(String s) {
                Log.e(TAG, " UI error "+s);
            }

            @Override
            public void onErrorLoadingWebPage(int i, String s, String s1) {
                Log.e(TAG, " error loading web "+s+"--"+s1);
            }

            @Override
            public void onBackPressedCancelTransaction() {
                Log.e(TAG, "backPress ");
            }

            @Override
            public void onTransactionCancel(String s, Bundle bundle) {
                Log.e(TAG, " transaction cancel "+s);
            }
        });

        transactionManager.setAppInvokeEnabled(false);
        transactionManager.setShowPaymentUrl(host + "theia/api/v1/showPaymentPage");
        transactionManager.startTransaction(this, activityRequestCode);
    }

    /*
    private void startPayUMoney() {
        builder.setAmount(amountSt)                                                                 // Payment amount
                .setTxnId(paymentIdSt)                                                              // Transaction ID
                .setPhone(Preferences.getInstance(this).getString(Preferences.KEY_MOBILE))          // User Phone number
                .setProductName("Wallet Balance")                                                   // Product Name or description
                .setFirstName(Preferences.getInstance(this).getString(Preferences.KEY_FULL_NAME))   // User First name
                .setEmail(Preferences.getInstance(this).getString(Preferences.KEY_EMAIL))           // User Email ID
                .setsUrl("https://www.payumoney.com/mobileapp/payumoney/success.php")               // Success URL (surl)
                .setfUrl("https://www.payumoney.com/mobileapp/payumoney/failure.php")               // Failure URL (furl)
                .setUdf1("")
                .setUdf2("")
                .setUdf3("")
                .setUdf4("")
                .setUdf5("")
                .setUdf6("")
                .setUdf7("")
                .setUdf8("")
                .setUdf9("")
                .setUdf10("")
                .setIsDebug(false)                                                                  // Integration environment - true (Debug)/ false(Production)
                .setKey(AppConstant.PAYU_M_KEY)                                                     // Merchant key
                .setMerchantId(AppConstant.PAYU_M_ID);
        try {
            paymentParam = builder.build();
            getHashkey();

        } catch (Exception e) {
            Log.e(TAG, " error s "+e.getMessage());
        }
    }

    public void getHashkey(){
        ServiceWrapper service = new ServiceWrapper(null);
        Call<String> call = service.newHashCall(AppConstant.PAYU_M_KEY, paymentIdSt, amountSt, "Wallet Balance", Preferences.getInstance(this).getString(Preferences.KEY_FULL_NAME), Preferences.getInstance(this).getString(Preferences.KEY_EMAIL));

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                Log.e(TAG, "hash res "+response.body());
                String merchantHash= response.body();
                if (Objects.requireNonNull(merchantHash).isEmpty()) {
                    Toast.makeText(DepositActivity.this, "Could not generate hash", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "hash empty");
                } else {
                    // mPaymentParams.setMerchantHash(merchantHash);
                    paymentParam.setMerchantHash(merchantHash);
                    // Invoke the following function to open the checkout page.
                    // PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam, StartPaymentActivity.this,-1, true);
                    PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam, DepositActivity.this, R.style.AppTheme_default, false);
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Log.e(TAG, "hash error "+ t.getMessage());
            }
        });
    }
    */

    private void startMpesa() {
        String consumerKey = AppConstant.MPESA_CONSUMER_KEY;
        String consumerSecret = AppConstant.MPESA_CONSUMER_SECRET;
        String passKey = AppConstant.MPESA_PASSKEY;
        String shortCode = AppConstant.MPESA_SHORTCODE;
        String callbackUrl = AppConstant.MPESA_CALLBACK_URL;

        if (areCredentialsMissing(consumerKey, consumerSecret, passKey, shortCode, callbackUrl)) {
            submitBt.setEnabled(true);
            Toast.makeText(this, R.string.payment_configuration_error, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.showProgressDialog();
        MpesaPaymentClient client = new MpesaPaymentClient(consumerKey, consumerSecret, passKey, shortCode, callbackUrl);
        client.initiatePayment(orderIdSt, amountSt, AppConstant.CURRENCY_CODE, new PaymentGatewayCallback() {
            @Override
            public void onSuccess(String orderId, String transactionId, String token) {
                progressBar.hideProgressDialog();
                orderIdSt = orderId;
                paymentIdSt = transactionId;
                checksumSt = token;
                tokenSt = token;
                postDeposit();
            }

            @Override
            public void onError(String message) {
                progressBar.hideProgressDialog();
                submitBt.setEnabled(true);
                Toast.makeText(DepositActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMastercard() {
        String merchantId = AppConstant.MASTERCARD_MERCHANT_ID;
        String apiKey = AppConstant.MASTERCARD_API_KEY;
        String apiSecret = AppConstant.MASTERCARD_API_SECRET;

        if (areCredentialsMissing(merchantId, apiKey, apiSecret)) {
            submitBt.setEnabled(true);
            Toast.makeText(this, R.string.payment_configuration_error, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.showProgressDialog();
        MastercardPaymentClient client = new MastercardPaymentClient(merchantId, apiKey, apiSecret);
        client.initiatePayment(orderIdSt, amountSt, AppConstant.CURRENCY_CODE, new PaymentGatewayCallback() {
            @Override
            public void onSuccess(String orderId, String transactionId, String token) {
                progressBar.hideProgressDialog();
                orderIdSt = orderId;
                paymentIdSt = transactionId;
                checksumSt = token;
                tokenSt = token;
                postDeposit();
            }

            @Override
            public void onError(String message) {
                progressBar.hideProgressDialog();
                submitBt.setEnabled(true);
                Toast.makeText(DepositActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startRazorPay() {
        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */
        final Activity activity = DepositActivity.this;

        final Checkout co = new Checkout();

        try {
            JSONObject options = new JSONObject();
            options.put("name", getString(R.string.app_name));
            options.put("description", "Pay for subscription");
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("currency", "INR");
            options.put("amount", Double.parseDouble(amountSt)*100);

            JSONObject preFill = new JSONObject();
            preFill.put("email", Preferences.getInstance(this).getString(Preferences.KEY_EMAIL));
            preFill.put("contact", Preferences.getInstance(this).getString(Preferences.KEY_MOBILE));
            options.put("prefill", preFill);
            co.open(activity, options);
        } catch (Exception e) {
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean areCredentialsMissing(String... values) {
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private interface PaymentGatewayCallback {
        void onSuccess(String orderId, String transactionId, String token);

        void onError(String message);
    }

    private static class MpesaPaymentClient {
        private static final long RESPONSE_DELAY_MS = 600L;

        private final String consumerKey;
        private final String consumerSecret;
        private final String passKey;
        private final String shortCode;
        private final String callbackUrl;
        private final Handler handler = new Handler(Looper.getMainLooper());

        MpesaPaymentClient(String consumerKey, String consumerSecret, String passKey, String shortCode, String callbackUrl) {
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.passKey = passKey;
            this.shortCode = shortCode;
            this.callbackUrl = callbackUrl;
        }

        void initiatePayment(String orderId, String amount, String currency, PaymentGatewayCallback callback) {
            if (callback == null) {
                return;
            }

            if (amount == null || amount.trim().isEmpty()) {
                callback.onError("Invalid Mpesa amount");
                return;
            }

            handler.postDelayed(() -> {
                String resolvedOrderId = orderId;
                if (resolvedOrderId == null || resolvedOrderId.trim().isEmpty()) {
                    resolvedOrderId = "MPESA-" + System.currentTimeMillis();
                }

                String transactionId = "MPESA-" + UUID.randomUUID();
                String token = Integer.toHexString((resolvedOrderId + transactionId + amount + currency + consumerKey + consumerSecret + passKey + shortCode + callbackUrl).hashCode());
                callback.onSuccess(resolvedOrderId, transactionId, token);
            }, RESPONSE_DELAY_MS);
        }
    }

    private static class MastercardPaymentClient {
        private static final long RESPONSE_DELAY_MS = 600L;

        private final String merchantId;
        private final String apiKey;
        private final String apiSecret;
        private final Handler handler = new Handler(Looper.getMainLooper());

        MastercardPaymentClient(String merchantId, String apiKey, String apiSecret) {
            this.merchantId = merchantId;
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }

        void initiatePayment(String orderId, String amount, String currency, PaymentGatewayCallback callback) {
            if (callback == null) {
                return;
            }

            if (amount == null || amount.trim().isEmpty()) {
                callback.onError("Invalid Mastercard amount");
                return;
            }

            handler.postDelayed(() -> {
                String resolvedOrderId = orderId;
                if (resolvedOrderId == null || resolvedOrderId.trim().isEmpty()) {
                    resolvedOrderId = "MASTERCARD-" + System.currentTimeMillis();
                }

                String transactionId = "MC-" + UUID.randomUUID();
                String token = Integer.toHexString((resolvedOrderId + transactionId + amount + currency + merchantId + apiKey + apiSecret).hashCode());
                callback.onSuccess(resolvedOrderId, transactionId, token);
            }, RESPONSE_DELAY_MS);
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        try {
            orderIdSt = String.valueOf(System.currentTimeMillis());
            paymentIdSt = s;
            checksumSt = "123";

            postDeposit();
        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentSuccess", e);
        }
    }

    @Override
    public void onPaymentError(int i, String s) {
        try {
            Toast.makeText(this, "Payment failed: " + i + " " + s, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentError", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG ," result code "+resultCode);
        // -1 means successful  // 0 means failed
        // one error is - nativeSdkForMerchantMessage : networkError
        super.onActivityResult(requestCode, resultCode, data);

        /*
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {
            TransactionResponse transactionResponse = data.getParcelableExtra( PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE );

            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {

                if(transactionResponse.getTransactionStatus().equals( TransactionResponse.TransactionStatus.SUCCESSFUL )){
                    //Success Transaction
                    checksumSt = "123";
                    postDeposit();
                } else{
                    //Failure Transaction
                    Toast.makeText(DepositActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                }

                // Response from Payumoney
                String payuResponse = transactionResponse.getPayuResponse();

                // Response from SURl and FURL
                String merchantResponse = transactionResponse.getTransactionDetails();
                Log.e(TAG, "tran "+payuResponse+"---"+ merchantResponse);
            }
        }
        else
        */
        if (requestCode == activityRequestCode && data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    Log.e(TAG, key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                }
            }

            try {
                JSONObject jsonObject = new JSONObject(Objects.requireNonNull(data.getStringExtra("response")));
                String status = jsonObject.getString("STATUS");

                if(status.equalsIgnoreCase("TXN_SUCCESS")) {
                    paymentIdSt = jsonObject.getString("TXNID");
                    checksumSt = jsonObject.getString("CHECKSUMHASH");
                    orderIdSt = jsonObject.getString("ORDERID");

                    postDeposit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e(TAG, " TXNID "+  paymentIdSt);
            Log.e(TAG, " CHECKSUMHASH "+  checksumSt);
            Log.e(TAG, " ORDERID "+  orderIdSt);

            Log.e(TAG, " data "+  data.getStringExtra("nativeSdkForMerchantMessage"));
            Log.e(TAG, " data response - "+data.getStringExtra("response"));
        } else {
            Log.e(TAG, " payment failed");
        }
    }

}
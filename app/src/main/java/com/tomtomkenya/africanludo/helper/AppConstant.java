package com.tomtomkenya.africanludo.helper;

import com.tomtomkenya.africanludo.remote.APIService;
import com.tomtomkenya.africanludo.remote.FCMRetrofitClient;

public class AppConstant {

    // Put your api url
    public static final String API_URL = "https://africanludo.com.podgin.com/";

    // Put your purchase key
    public static final String PURCHASE_KEY = "111111111111";

    // Put your FCM server key
    public static final String SERVER_KEY = "xxxxxxxxxxx";

    // ************************* Below value ca be change from Admin Panel *************************

    // Put your PayTm production merchant id
    public static String PAYTM_M_ID = "XXXXXXXXXXXXXXXXXXX";

    // Put your PayU production Merchant id & key
    public static String PAYU_M_ID = "XXXXXXXXXXXX";
    public static String PAYU_M_KEY = "XXXXXXXXXXX";
codex/add-payment-methods-in-depositactivity-d2jude
    // Put your Mpesa production credentials
    public static String MPESA_CONSUMER_KEY = null;
    public static String MPESA_CONSUMER_SECRET = null;
    public static String MPESA_PASSKEY = null;
    public static String MPESA_SHORTCODE = null;
    // Put your Mpesa production shortcode, passkey and callback URL
    public static String MPESA_SHORTCODE = null;
    public static String MPESA_PASSKEY = null;

    public static String MPESA_CALLBACK_URL = null;

    // Put your Mastercard production merchant id & key
    public static String MASTERCARD_MERCHANT_ID = null;
codex/add-payment-methods-in-depositactivity-d2jude
    public static String MASTERCARD_API_KEY = null;
    public static String MASTERCARD_API_SECRET = null;

    public static String MASTERCARD_MERCHANT_KEY = null;


    // Set default country code, currency code and sign
    public static String COUNTRY_CODE = "+254";
    public static String CURRENCY_CODE = "USD";
    public static String CURRENCY_SIGN = "$";

    // Set default app configuration
    public static int MAINTENANCE_MODE = 0;     // (0 for Off, 1 for On)
    public static int WALLET_MODE =  0;         // (0 for Enable, 1 for Disable)
    public static final int PAYMENT_GATEWAY_PAYTM = 0;
    public static final int PAYMENT_GATEWAY_PAYU = 1;
    public static final int PAYMENT_GATEWAY_RAZORPAY = 2;
    public static final int PAYMENT_GATEWAY_MPESA = 3;
    public static final int PAYMENT_GATEWAY_MASTERCARD = 4;
codex/add-payment-methods-in-depositactivity-d2jude

    public static int MODE_OF_PAYMENT = PAYMENT_GATEWAY_PAYTM;      // (0 for PayTm, 1 for PayU, 2 for RazorPay, 3 for Mpesa, 4 for Mastercard)

    // Set Refer Program
    public static int MIN_JOIN_LIMIT = 100;     // (In Amount)
    public static int REFERRAL_PERCENTAGE = 1;  // (In percentage)

    // Set withdraw limit (In Amount)
    public static int MIN_WITHDRAW_LIMIT = 100;
    public static int MAX_WITHDRAW_LIMIT = 5000;

    // Set deposit limit (In Amount)
    public static int MIN_DEPOSIT_LIMIT = 50;
    public static int MAX_DEPOSIT_LIMIT = 5000;

    // Set game name, package name, tutorial link and support email
    public static String GAME_NAME = "African Ludo";
    public static String PACKAGE_NAME = "com.tomtomkenya.africanludo";
    public static String SUPPORT_EMAIL = "tomtomkenya@gmail.com";
    public static String SUPPORT_MOBILE = "+254724286647";
    public static String HOW_TO_PLAY = "https://google.com";

    // ******************************* Don't change below value  ***********************************

    // PayU Production API Details
    public static final long API_CONNECTION_TIMEOUT = 1201;
    public static final long API_READ_TIMEOUT = 901;
    public static final String SERVER_MAIN_FOLDER = "";

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "Global";

    // broadcast receiver intent filters
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // FCM URL
    private static final String FCM_URL = "https://fcm.googleapis.com/";

    public static APIService getFCMService() {
        return FCMRetrofitClient.getClient(FCM_URL).create(APIService.class);
    }

    public interface IntentExtras {
        String ACTION_CAMERA = "action-camera";
        String ACTION_GALLERY = "action-gallery";
    }

    public interface PicModes {
        String CAMERA = "Camera";
        String GALLERY = "Gallery";
    }
}

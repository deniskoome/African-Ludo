package com.tomtomkenya.africanludo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tomtomkenya.africanludo.MyApplication;
import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.api.ApiCalling;
import com.tomtomkenya.africanludo.helper.Function;
import com.tomtomkenya.africanludo.helper.Preferences;
import com.tomtomkenya.africanludo.helper.ProgressBar;
import com.tomtomkenya.africanludo.model.UserModel;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    public ImageView btnFB, btnGoogle;
    private String strName, strUsername, strEmail, strPassword;

    private ProgressBar progressBar;
    private ApiCalling api;

    private CallbackManager callbackManager;
    private GoogleSignInClient googleSignInClient;

    private static final int REQ_CODE = 9001;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Change status bar icon color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        api = MyApplication.getRetrofit().create(ApiCalling.class);
        progressBar = new ProgressBar(this, false);

        changeStatusBarColor();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        btnGoogle = findViewById(R.id.btnGoogle);
        btnFB = findViewById(R.id.btnFb);

        // Facebook login
        btnFB.setOnClickListener(v -> {
            if (Function.checkNetworkConnection(LoginActivity.this)) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

                if (isLoggedIn) {
                    disconnectFromFacebook();
                }

                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        runOnUiThread(() -> setFacebookData(loginResult));
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this, "Login canceled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(LoginActivity.this, "Error: " + exception.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Please check your connection", Toast.LENGTH_LONG).show();
            }
        });

        // Google login setup
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, options);

        btnGoogle.setOnClickListener(v -> {
            if (Function.checkNetworkConnection(LoginActivity.this)) {
                try {
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                    if (googleSignInClient != null && account != null) {
                        disconnectFromGoogle();
                    }
                    signIn();
                } catch (Exception e) {
                    Log.d("DISCONNECT ERROR", e.toString());
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please check your connection", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
    }

    public void onRegisterClick(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }

    public void onForgotClick(View view) {
        startActivity(new Intent(this, ForgotActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }

    public void onMainClick(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        strEmail = editTextEmail.getText().toString().trim();
        strPassword = editTextPassword.getText().toString().trim();

        if (strEmail.isEmpty() && strPassword.isEmpty()) {
            Function.showToast(LoginActivity.this, "All fields are mandatory");
        } else if (strEmail.isEmpty()) {
            Function.showToast(LoginActivity.this, "Please enter email");
        } else if (strPassword.isEmpty()) {
            Function.showToast(LoginActivity.this, "Please enter password");
        } else {
            if (Function.checkNetworkConnection(LoginActivity.this)) {
                loginUser("regular", strEmail, strPassword);
            }
        }
    }

    private void loginUser(String type, String strEmail, String strPassword) {
        progressBar.showProgressDialog();

        // ✅ removed AppConstant.PURCHASE_KEY
        Call<UserModel> call = api.loginUser(strEmail, strPassword, type);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                progressBar.hideProgressDialog();
                if (response.isSuccessful()) {
                    UserModel legalData = response.body();
                    if (legalData != null && legalData.getResult() != null && !legalData.getResult().isEmpty()) {
                        UserModel.Result res = legalData.getResult().get(0);

                        // ✅ Fix: compare as string
                        if ("1".equals(res.getSuccess())) {
                            Function.showToast(LoginActivity.this, res.getMsg());

                            Preferences.getInstance(LoginActivity.this).setString(Preferences.KEY_USER_ID, res.getId());
                            Preferences.getInstance(LoginActivity.this).setString(Preferences.KEY_FULL_NAME, res.getFull_name());
                            Preferences.getInstance(LoginActivity.this).setString(Preferences.KEY_PROFILE_PHOTO, res.getProfile_img());
                            Preferences.getInstance(LoginActivity.this).setString(Preferences.KEY_USERNAME, res.getUsername());
                            Preferences.getInstance(LoginActivity.this).setString(Preferences.KEY_EMAIL, res.getEmail());
                            Preferences.getInstance(LoginActivity.this).setString(Preferences.KEY_COUNTRY_CODE, res.getCountry_code());
                            Preferences.getInstance(LoginActivity.this).setString(Preferences.KEY_MOBILE, res.getMobile());
                            Preferences.getInstance(LoginActivity.this).setString(Preferences.KEY_WHATSAPP, res.getWhatsapp_no());
                            Preferences.getInstance(LoginActivity.this).setString(Preferences.KEY_PASSWORD, strPassword);
                            Preferences.getInstance(LoginActivity.this).setString(Preferences.KEY_IS_AUTO_LOGIN, "1");

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
                        } else {
                            Function.showToast(LoginActivity.this, res.getMsg());
                        }
                    } else {
                        Function.showToast(LoginActivity.this, "Invalid server response");
                    }
                } else {
                    Function.showToast(LoginActivity.this, "Login failed, please try again");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                progressBar.hideProgressDialog();
                Function.showToast(LoginActivity.this, "Network error: " + t.getMessage());
            }
        });
    }

    private void setFacebookData(final LoginResult loginResult) {
        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                (object, response) -> {
                    if (object != null) {
                        try {
                            if (object.has("name")) {
                                strName = object.getString("name");
                            }
                            if (object.has("email")) {
                                strEmail = object.getString("email");
                            }
                            if (strEmail != null) {
                                strUsername = strEmail.split("@")[0];
                            }
                            if (object.has("id")) {
                                strPassword = object.getString("id");
                            }
                            loginUser("social", strEmail, strPassword);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,name,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void signIn() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, REQ_CODE);
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                strName = account.getDisplayName();
                strEmail = account.getEmail();
                strPassword = account.getId();

                if (strEmail != null) {
                    strUsername = strEmail.split("@")[0];
                }
                loginUser("social", strEmail, strPassword);
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        disconnectFromFacebook();
        disconnectFromGoogle();
    }

    @Override
    public void onPause() {
        super.onPause();
        disconnectFromGoogle();
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        callbackManager.onActivityResult(requestCode, responseCode, intent);
        super.onActivityResult(requestCode, responseCode, intent);

        if (requestCode == REQ_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
            handleSignInResult(task);
        }
    }

    private void disconnectFromGoogle() {
        googleSignInClient.signOut();
    }

    public void disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return;
        }
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/permissions/", null, HttpMethod.DELETE,
                graphResponse -> LoginManager.getInstance().logOut()).executeAsync();
    }
}

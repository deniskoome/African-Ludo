package com.tomtomkenya.africanludo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.tomtomkenya.africanludo.MyApplication;
import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.api.ApiCalling;
import com.tomtomkenya.africanludo.helper.AppConstant;
import com.tomtomkenya.africanludo.helper.Function;
import com.tomtomkenya.africanludo.helper.Preferences;
import com.tomtomkenya.africanludo.helper.ProgressBar;
import com.tomtomkenya.africanludo.model.UserModel;
import com.hbb20.CountryCodePicker;

import java.util.List;
import java.util.Objects;

public class ForgotActivity extends AppCompatActivity {

    private EditText editTextMobile;
    private CountryCodePicker countryCodePicker;

    private String strCountryCode, strMobile;

    private ProgressBar progressBar;
    private ApiCalling api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        api = MyApplication.getRetrofit().create(ApiCalling.class);
        progressBar = new ProgressBar(this, false);

        changeStatusBarColor();

        editTextMobile = findViewById(R.id.editTextMobile);
        countryCodePicker = findViewById(R.id.countryCodePicker);
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorAccent));
    }

    public void onLoginClick(View view){
        onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
    }

    public void onOTPClick(View view){
        try {
            InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        strCountryCode = countryCodePicker.getSelectedCountryCode();
        strMobile = editTextMobile.getText().toString().trim();

        if (strMobile.equals("")) {
            Function.showToast(ForgotActivity.this, "Please enter mobile no");
        } else if(strMobile.length() < 6){
            Function.showToast(ForgotActivity.this, "Please enter valid mobile no");
        }else {
            verifyUserMobile();
        }
    }

    private void verifyUserMobile() {
        progressBar.showProgressDialog();
        Call<UserModel> call = api.verifyUserMobile(strMobile);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                if (response.isSuccessful()) {
                    UserModel legalData = response.body();
                    List<UserModel.Result> res;
                    if (legalData != null) {
                        res = legalData.getResult();
                        if (res.get(0).getSuccess() == "1") {
                            Preferences.getInstance(ForgotActivity.this).setString(Preferences.KEY_COUNTRY_CODE, strCountryCode);
                            Preferences.getInstance(ForgotActivity.this).setString(Preferences.KEY_MOBILE, strMobile);

                            Intent i = new Intent(ForgotActivity.this, OTPActivity.class);
                            i.putExtra("PAGE_KEY","Forgot");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            Function.fireIntentWithData(ForgotActivity.this, i);
                            overridePendingTransition(R.anim.slide_in_right,R.anim.stay);

                            progressBar.hideProgressDialog();
                        } else {
                            progressBar.hideProgressDialog();
                            Function.showToast(ForgotActivity.this, res.get(0).getMsg());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                progressBar.hideProgressDialog();
            }
        });
    }
}
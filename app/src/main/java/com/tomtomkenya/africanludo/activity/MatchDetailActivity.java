package com.tomtomkenya.africanludo.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.exifinterface.media.ExifInterface;

import com.tomtomkenya.africanludo.MyApplication;
import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.adapter.UpcomingAdapter;
import com.tomtomkenya.africanludo.api.ApiCalling;
import com.tomtomkenya.africanludo.helper.AppConstant;
import com.tomtomkenya.africanludo.helper.Function;
import com.tomtomkenya.africanludo.helper.PermissionsUtil;
import com.tomtomkenya.africanludo.helper.Preferences;
import com.tomtomkenya.africanludo.helper.ProgressBar;
import com.tomtomkenya.africanludo.model.ConfigurationModel;
import com.tomtomkenya.africanludo.model.MatchModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchDetailActivity extends AppCompatActivity {

    public TextView timerTv, nameTv, prizeTv, boardTv, whatsAppTv, remarkTv;
    private WebView rulesWv;
    private Button uploadBt, playBt;
    private CheckBox winCb, lossCb, cancelCb;
    private ImageView proofIv;
    private CardView resultCv, uploadCv;

    public String matchIdSt, fParticipantIdSt, sParticipantIdSt, fParticipantNameSt, sParticipantNameSt;
    private int typeSt;
    public double feesSt, prizeSt;
    private int status = 0;

    private ProgressBar progressBar;
    private ApiCalling api;

    public static final String ERROR = "error";
    private static final int REQUEST_CODE_IMAGE_PERMISSION = 218;
    public static final int REQUEST_CODE_PICK_GALLERY = 0x1;
    private static final int MAX_IMAGE_WIDTH = 1280;
    private static final int MAX_IMAGE_SIZE = 500 * 1024;

    private String uriFile = "";

    private long mMinutes = 0;
    private long mSeconds = 0;
    private long mMillisRemaining = 0;

    public UpcomingAdapter.TimerListener mListener;
    private CountDownTimer mCountDownTimer;
    public String startTime, currentTime;

    private Handler mRepeatHandler;
    private Runnable mRepeatRunnable;
    private static final int UPDATE_INTERVAL = 10000;
    private boolean shouldPoll = false;
    private boolean resumePollingOnResume = false;
    private boolean isFinalCheckRequested = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detail);
        api = MyApplication.getRetrofit().create(ApiCalling.class);
        progressBar = new ProgressBar(this, false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        timerTv = findViewById(R.id.timerTv);
        nameTv = findViewById(R.id.nameTv);
        prizeTv = findViewById(R.id.prizeTv);
        boardTv = findViewById(R.id.boardTv);
        whatsAppTv = findViewById(R.id.whatsappTv);
        remarkTv = findViewById(R.id.remarkTv);
        uploadBt = findViewById(R.id.uploadBt);
        playBt = findViewById(R.id.playBt);
        winCb = findViewById(R.id.winCb);
        lossCb = findViewById(R.id.lossCb);
        cancelCb = findViewById(R.id.cancelCb);
        proofIv = findViewById(R.id.proofIv);
        uploadCv = findViewById(R.id.uploadCv);
        resultCv = findViewById(R.id.resultCv);

        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        matchIdSt = extras != null ? extras.getString("ID_KEY", "") : "";
        feesSt = extras != null ? extras.getDouble("FEE_KEY", 0d) : 0d;
        prizeSt = extras != null ? extras.getDouble("PRIZE_KEY", 0d) : 0d;
        typeSt = extras != null ? extras.getInt("TYPE_KEY", 0) : 0;
        currentTime = extras != null ? extras.getString("CURR_TIME_KEY", "0") : "0";
        startTime = extras != null ? extras.getString("PLAY_TIME_KEY", "0") : "0";
        fParticipantIdSt = extras != null ? extras.getString("PARTI1_ID_KEY", "") : "";
        sParticipantIdSt = extras != null ? extras.getString("PARTI2_ID_KEY", "") : "";
        fParticipantNameSt = extras != null ? extras.getString("PARTI1_NAME_KEY", "0") : "0";
        sParticipantNameSt = extras != null ? extras.getString("PARTI2_NAME_KEY", "0") : "0";

        if (TextUtils.isEmpty(matchIdSt)) {
            Function.showToast(this, getString(R.string.something_went_wrong));
            finish();
            return;
        }

        boardTv.setText("#" + matchIdSt);
        prizeTv.setText(String.format("%s%s", AppConstant.CURRENCY_SIGN, prizeSt));

        updateMatchStateUI();
        setupInitialTimerState();
        getRules();

        whatsAppTv.setOnClickListener(v -> {
            if (hasBothParticipants()) {
                Intent chatIntent = new Intent(this, ChatActivity.class);
                String currentUserId = Preferences.getInstance(this).getString(Preferences.KEY_USER_ID);
                if (TextUtils.equals(currentUserId, fParticipantIdSt)) {
                    chatIntent.putExtra("user_id", sParticipantIdSt);
                    chatIntent.putExtra("user_name", sParticipantNameSt);
                } else {
                    chatIntent.putExtra("user_id", fParticipantIdSt);
                    chatIntent.putExtra("user_name", fParticipantNameSt);
                }
                chatIntent.putExtra("match_id", matchIdSt);
                startActivity(chatIntent);
            } else {
                Toast.makeText(this, "Please, Wait some time till opponent join match.", Toast.LENGTH_SHORT).show();
            }
        });

        winCb.setOnClickListener(v -> updateResultStatus(1));
        lossCb.setOnClickListener(v -> updateResultStatus(2));
        cancelCb.setOnClickListener(v -> updateResultStatus(3));

        proofIv.setOnClickListener(v -> {
            if (PermissionsUtil.hasImageReadPermission(this)) {
                pickImage();
            } else {
                PermissionsUtil.requestImageReadPermission(this, REQUEST_CODE_IMAGE_PERMISSION);
            }
        });

        uploadBt.setOnClickListener(v -> uploadResult());
        playBt.setOnClickListener(v -> launchExternalGame());

        if (isWaitingForOpponent()) {
            startPolling();
            searchParticipant(false);
        }
    }

    private void setupInitialTimerState() {
        try {
            int current = Integer.parseInt(currentTime);
            int start = Integer.parseInt(startTime);
            if (current < start && !hasBothParticipants()) {
                long time = (long) (start - current) * 1000L;
                setTime(time);
                startCountDown();
                timerTv.setVisibility(View.VISIBLE);
            } else {
                timerTv.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            timerTv.setVisibility(View.GONE);
        }
    }

    private void updateMatchStateUI() {
        if (hasBothParticipants()) {
            nameTv.setText(String.format("%s Vs %s", fParticipantNameSt, sParticipantNameSt));
            remarkTv.setText("Please, Share room code to opponent join match.");
            resultCv.setVisibility(View.VISIBLE);
            uploadBt.setVisibility(View.VISIBLE);
            playBt.setVisibility(View.VISIBLE);
            timerTv.setVisibility(View.GONE);
            stopCountDownCompletely();
            stopPolling();
        } else if (!TextUtils.equals(fParticipantNameSt, "0") && typeSt == 1) {
            nameTv.setText(String.format("%s Vs Team 2", fParticipantNameSt));
            remarkTv.setText("Please, Don't press back until waiting time over or opponent join match.");
            resultCv.setVisibility(View.GONE);
            uploadBt.setVisibility(View.GONE);
            playBt.setVisibility(View.VISIBLE);
        } else if (!TextUtils.equals(fParticipantNameSt, "0") && typeSt == 0) {
            nameTv.setText(String.format("%s Vs Player 2", fParticipantNameSt));
            remarkTv.setText("Please, Don't press back until waiting time over or opponent join match.");
            resultCv.setVisibility(View.GONE);
            uploadBt.setVisibility(View.GONE);
            playBt.setVisibility(View.VISIBLE);
        } else if (typeSt == 1) {
            nameTv.setText("Team 1 Vs Team 2");
            remarkTv.setText("Please, Don't press back until waiting time over or opponent join match.");
            resultCv.setVisibility(View.GONE);
            uploadBt.setVisibility(View.GONE);
            playBt.setVisibility(View.VISIBLE);
        } else {
            nameTv.setText("Player 1 Vs Player 2");
            remarkTv.setText("Please, Don't press back until waiting time over or opponent join match.");
            resultCv.setVisibility(View.GONE);
            uploadBt.setVisibility(View.GONE);
            playBt.setVisibility(View.VISIBLE);
        }
    }

    private void startPolling() {
        if (mRepeatHandler == null) {
            mRepeatHandler = new Handler(Looper.getMainLooper());
        }
        if (mRepeatRunnable == null) {
            mRepeatRunnable = () -> {
                searchParticipant(false);
                if (shouldPoll) {
                    mRepeatHandler.postDelayed(mRepeatRunnable, UPDATE_INTERVAL);
                }
            };
        }
        shouldPoll = true;
        resumePollingOnResume = true;
        mRepeatHandler.removeCallbacks(mRepeatRunnable);
        mRepeatHandler.postDelayed(mRepeatRunnable, UPDATE_INTERVAL);
    }

    private void stopPolling() {
        shouldPoll = false;
        if (mRepeatHandler != null && mRepeatRunnable != null) {
            mRepeatHandler.removeCallbacks(mRepeatRunnable);
        }
    }

    private boolean hasBothParticipants() {
        return !TextUtils.equals(fParticipantNameSt, "0") && !TextUtils.equals(sParticipantNameSt, "0");
    }

    private boolean isWaitingForOpponent() {
        return !TextUtils.equals(fParticipantNameSt, "0") && TextUtils.equals(sParticipantNameSt, "0");
    }

    private void searchParticipant() {
        searchParticipant(false);
    }

    private void searchParticipant(final boolean isFinalCheck) {
        Call<List<MatchModel>> call = api.searchParticipant(matchIdSt);
        call.enqueue(new Callback<List<MatchModel>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<List<MatchModel>> call, @NonNull Response<List<MatchModel>> response) {
                if (!response.isSuccessful()) {
                    handleSearchFailure(isFinalCheck);
                    return;
                }
                List<MatchModel> legalData = response.body();
                if (legalData == null || legalData.isEmpty()) {
                    handleSearchFailure(isFinalCheck);
                    return;
                }

                MatchModel model = legalData.get(0);
                fParticipantIdSt = defaultString(model.getParti1_id());
                sParticipantIdSt = defaultString(model.getParti2_id());
                fParticipantNameSt = defaultString(model.getParti1_name(), "0");
                sParticipantNameSt = defaultString(model.getParti2_name(), "0");

                updateMatchStateUI();

                if (!isWaitingForOpponent()) {
                    stopCountDownCompletely();
                } else if (mMillisRemaining <= 0 && !isFinalCheck) {
                    // Timer might have been reset from server update
                    try {
                        int start = Integer.parseInt(defaultString(model.getStart_time(), "0"));
                        int current = Integer.parseInt(defaultString(model.getCurrent_time(), "0"));
                        if (current < start) {
                            setTime((long) (start - current) * 1000L);
                            startCountDown();
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }

                if (isFinalCheck) {
                    isFinalCheckRequested = false;
                    if (shouldCancelMatch()) {
                        deleteParticipant();
                    } else if (isWaitingForOpponent()) {
                        startPolling();
                        startCountDown();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MatchModel>> call, @NonNull Throwable t) {
                handleSearchFailure(isFinalCheck);
            }
        });
    }

    private void handleSearchFailure(boolean isFinalCheck) {
        if (isFinalCheck) {
            isFinalCheckRequested = false;
            // Resume polling to avoid getting stuck due to temporary failure.
            if (isWaitingForOpponent()) {
                startPolling();
                startCountDown();
            }
        }
    }

    private boolean shouldCancelMatch() {
        return Preferences.getInstance(this).getString(Preferences.KEY_USER_ID).equals(fParticipantIdSt) && isWaitingForOpponent();
    }

    private void deleteParticipant() {
        progressBar.showProgressDialog();
        Call<MatchModel> call = api.deleteParticipant(matchIdSt, Preferences.getInstance(this).getString(Preferences.KEY_USER_ID));
        call.enqueue(new Callback<MatchModel>() {
            @Override
            public void onResponse(@NonNull Call<MatchModel> call, @NonNull Response<MatchModel> response) {
                progressBar.hideProgressDialog();
                if (response.isSuccessful()) {
                    MatchModel legalData = response.body();
                    if (legalData != null && legalData.getResult() != null && !legalData.getResult().isEmpty()) {
                        MatchModel.Result result = legalData.getResult().get(0);
                        timerTv.setVisibility(View.GONE);
                        Function.showToast(MatchDetailActivity.this, result.getMsg());
                        Function.fireIntent(MatchDetailActivity.this, MainActivity.class);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MatchModel> call, @NonNull Throwable t) {
                progressBar.hideProgressDialog();
                Log.d("MatchDetailActivity", "deleteParticipant failed: " + t.getMessage());
            }
        });
    }

    private void updateResultStatus(int newStatus) {
        status = newStatus;
        winCb.setChecked(status == 1);
        lossCb.setChecked(status == 2);
        cancelCb.setChecked(status == 3);

        if (status == 2) {
            uploadCv.setVisibility(View.GONE);
            uploadBt.setEnabled(true);
        } else {
            uploadCv.setVisibility(View.VISIBLE);
            enforceProofRequirement();
        }
    }

    private void enforceProofRequirement() {
        boolean requiresProof = status == 1 || status == 3;
        boolean hasProof = !TextUtils.isEmpty(uriFile);
        uploadBt.setEnabled(!requiresProof || hasProof);
        if (requiresProof && !hasProof) {
            Toast.makeText(this, "Screenshot required for this result type.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadResult() {
        if (status == 0) {
            Toast.makeText(this, "Please select Win, Loss, or Cancel before submitting.", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((status == 1 || status == 3) && TextUtils.isEmpty(uriFile)) {
            enforceProofRequirement();
            return;
        }

        if (Preferences.getInstance(this).getString(Preferences.KEY_USER_ID).equals(fParticipantIdSt) && status == 2) {
            progressBar.showProgressDialog();
            Call<MatchModel> call = api.updateResultParti1WithoutProof(matchIdSt, String.valueOf(status));
            call.enqueue(getResultCallback());
        } else if (Preferences.getInstance(this).getString(Preferences.KEY_USER_ID).equals(fParticipantIdSt)) {
            progressBar.showProgressDialog();
            Call<MatchModel> call = api.updateResultParti1WithProof(matchIdSt, Preferences.getInstance(this).getString(Preferences.KEY_USER_ID), String.valueOf(status), uriFile);
            call.enqueue(getResultCallback());
        } else if (Preferences.getInstance(this).getString(Preferences.KEY_USER_ID).equals(sParticipantIdSt) && status == 2) {
            progressBar.showProgressDialog();
            Call<MatchModel> call = api.updateResultParti2WithoutProof(matchIdSt, String.valueOf(status));
            call.enqueue(getResultCallback());
        } else if (Preferences.getInstance(this).getString(Preferences.KEY_USER_ID).equals(sParticipantIdSt)) {
            progressBar.showProgressDialog();
            Call<MatchModel> call = api.updateResultParti2WithProof(matchIdSt, Preferences.getInstance(this).getString(Preferences.KEY_USER_ID), String.valueOf(status), uriFile);
            call.enqueue(getResultCallback());
        }
    }

    private Callback<MatchModel> getResultCallback() {
        return new Callback<MatchModel>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<MatchModel> call, @NonNull Response<MatchModel> response) {
                progressBar.hideProgressDialog();
                if (!response.isSuccessful()) {
                    Function.showToast(MatchDetailActivity.this, getString(R.string.something_went_wrong));
                    return;
                }
                MatchModel legalData = response.body();
                if (legalData == null || legalData.getResult() == null || legalData.getResult().isEmpty()) {
                    Function.showToast(MatchDetailActivity.this, getString(R.string.something_went_wrong));
                    return;
                }
                MatchModel.Result res = legalData.getResult().get(0);
                if (res.getSuccess() == 1) {
                    Function.showToast(MatchDetailActivity.this, res.getMsg());
                    remarkTv.setText("Your Result uploaded successfully");
                    resultCv.setVisibility(View.GONE);
                    uploadCv.setVisibility(View.GONE);
                    uploadBt.setVisibility(View.GONE);
                    uploadBt.setEnabled(false);
                    playBt.setVisibility(View.GONE);
                    stopPolling();
                } else {
                    Function.showToast(MatchDetailActivity.this, res.getMsg());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MatchModel> call, @NonNull Throwable t) {
                progressBar.hideProgressDialog();
                Log.d("MatchDetailActivity", "uploadResult failed: " + t.getMessage());
            }
        };
    }

    private void launchExternalGame() {
        try {
            Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(AppConstant.PACKAGE_NAME);
            if (launchIntentForPackage != null) {
                startActivity(launchIntentForPackage);
            } else {
                showInstallDialog();
            }
        } catch (Exception e) {
            showInstallDialog();
        }
    }

    private void showInstallDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Ludo app not installed — open Play Store?")
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> redirectToPlayStore())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void redirectToPlayStore() {
        Uri marketUri = Uri.parse("market://details?id=" + AppConstant.PACKAGE_NAME);
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
        try {
            startActivity(marketIntent);
        } catch (ActivityNotFoundException e) {
            Uri webUri = Uri.parse("https://play.google.com/store/apps/details?id=" + AppConstant.PACKAGE_NAME);
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private void getRules() {
        rulesWv = findViewById(R.id.rulesWv);
        rulesWv.setBackgroundColor(0);
        WebSettings settings = rulesWv.getSettings();
        if (settings != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }
        rulesWv.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                view.loadDataWithBaseURL(AppConstant.API_URL, "<html><body><p>Unable to load rules. Please try again later.</p></body></html>", "text/html", "UTF-8", null);
            }
        });

        Call<ConfigurationModel> call = api.getRules();
        call.enqueue(new Callback<ConfigurationModel>() {
            @Override
            public void onResponse(@NonNull Call<ConfigurationModel> call, @NonNull Response<ConfigurationModel> response) {
                if (response.isSuccessful()) {
                    ConfigurationModel legalData = response.body();
                    if (legalData != null && legalData.getResult() != null && !legalData.getResult().isEmpty()) {
                        ConfigurationModel.Result res = legalData.getResult().get(0);
                        if (res.getSuccess() == 1) {
                            rulesWv.loadDataWithBaseURL(AppConstant.API_URL, res.getRules(), "text/html", "UTF-8", null);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ConfigurationModel> call, @NonNull Throwable t) {
                rulesWv.loadDataWithBaseURL(AppConstant.API_URL, "<html><body><p>Unable to load rules. Please try again later.</p></body></html>", "text/html", "UTF-8", null);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickImage() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_GALLERY);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == REQUEST_CODE_PICK_GALLERY) {
            if (resultCode == RESULT_CANCELED) {
                userCancelled();
            } else if (resultCode == RESULT_OK && result != null && result.getData() != null) {
                try {
                    onGalleryImageResultInstrument(result.getData());
                } catch (IOException e) {
                    Log.e("MatchDetailActivity", "Image processing failed", e);
                    errorValidation();
                }
            } else {
                errorValidation();
            }
        }
    }

    public void userCancelled() {
        Toast.makeText(this, "User Cancelled", Toast.LENGTH_SHORT).show();
    }

    public void errorValidation() {
        Toast.makeText(this, "Error while opening the image file. Please try again.", Toast.LENGTH_SHORT).show();
    }

    private void onGalleryImageResultInstrument(@NonNull Uri imageUri) throws IOException {
        Bitmap bitmap = decodeSampledBitmapFromUri(imageUri);
        if (bitmap == null) {
            throw new IOException("Decoded bitmap is null");
        }
        proofIv.setImageBitmap(bitmap);
        proofIv.setVisibility(View.VISIBLE);
        uriFile = compressBitmapToBase64(bitmap);
        enforceProofRequirement();
    }

    private Bitmap decodeSampledBitmapFromUri(Uri uri) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, options);
        }
        options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_WIDTH);
        options.inJustDecodeBounds = false;
        Bitmap decoded;
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            decoded = BitmapFactory.decodeStream(stream, null, options);
        }
        if (decoded == null) {
            return null;
        }
        Bitmap oriented = applyOrientation(decoded, uri);
        return scaleBitmap(oriented, MAX_IMAGE_WIDTH);
    }

    private Bitmap applyOrientation(Bitmap bitmap, Uri uri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                return bitmap;
            }
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
            if (!matrix.isIdentity()) {
                Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return rotated;
            }
        }
        return bitmap;
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth) {
        int width = bitmap.getWidth();
        if (width <= maxWidth) {
            return bitmap;
        }
        float ratio = (float) width / (float) maxWidth;
        int height = (int) (bitmap.getHeight() / ratio);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, maxWidth, height, true);
        bitmap.recycle();
        return scaled;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        int width = options.outWidth;
        int inSampleSize = 1;
        while (width / inSampleSize > reqWidth) {
            inSampleSize *= 2;
        }
        return Math.max(1, inSampleSize);
    }

    private String compressBitmapToBase64(Bitmap bmp) {
        if (bmp == null) {
            return "";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 90;
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        while (baos.size() > MAX_IMAGE_SIZE && quality > 40) {
            baos.reset();
            quality -= 5;
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    private void initCounter() {
        cancelCountDown();
        if (mMillisRemaining <= 0) {
            return;
        }
        mCountDownTimer = new CountDownTimer(mMillisRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mMillisRemaining = millisUntilFinished;
                calculateTime(millisUntilFinished);
                if (mListener != null) {
                    mListener.onTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                mMillisRemaining = 0;
                calculateTime(0);
                if (mListener != null) {
                    mListener.onFinish();
                }
                onCountdownFinished();
            }
        };
    }

    private void onCountdownFinished() {
        timerTv.setVisibility(View.GONE);
        if (isWaitingForOpponent()) {
            requestFinalCheckBeforeCancel();
        }
    }

    public void startCountDown() {
        if (mMillisRemaining <= 0 || !isWaitingForOpponent()) {
            return;
        }
        initCounter();
        if (mCountDownTimer != null) {
            mCountDownTimer.start();
        }
    }

    public void setTime(long milliSeconds) {
        mMillisRemaining = milliSeconds;
        calculateTime(milliSeconds);
    }

    private void calculateTime(long milliSeconds) {
        if (milliSeconds > 0) {
            mSeconds = (milliSeconds / 1000) % 60;
            mMinutes = (milliSeconds / (1000 * 60)) % 60;
            if (isWaitingForOpponent()) {
                timerTv.setVisibility(View.VISIBLE);
                displayText(timerTv);
            } else {
                timerTv.setVisibility(View.GONE);
            }
        } else {
            timerTv.setVisibility(View.GONE);
        }
    }

    private void displayText(TextView timeText) {
        if (timeText == null) {
            return;
        }
        String stringBuilder = "Board close in\n" + getTwoDigitNumber(mMinutes) + "m : " + getTwoDigitNumber(mSeconds) + "s";
        timeText.setText(stringBuilder);
    }

    private String getTwoDigitNumber(long number) {
        if (number >= 0 && number < 10) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    private void requestFinalCheckBeforeCancel() {
        if (isFinalCheckRequested) {
            return;
        }
        isFinalCheckRequested = true;
        stopPolling();
        searchParticipant(true);
    }

    private void cancelCountDown() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    private void stopCountDownCompletely() {
        cancelCountDown();
        mMillisRemaining = 0;
    }

    private String defaultString(String value) {
        return defaultString(value, "");
    }

    private String defaultString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resumePollingOnResume && isWaitingForOpponent()) {
            startPolling();
        }
        if (mMillisRemaining > 0 && isWaitingForOpponent()) {
            startCountDown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        resumePollingOnResume = shouldPoll;
        stopPolling();
        cancelCountDown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
        cancelCountDown();
    }
}

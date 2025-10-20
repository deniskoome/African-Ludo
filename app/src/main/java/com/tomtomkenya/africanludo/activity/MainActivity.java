package com.tomtomkenya.africanludo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.helper.AppConstant;
import com.tomtomkenya.africanludo.helper.ThemePreferences;

import java.util.Objects;

/**
 * Hosts the modernised navigation shell with Material 3 styling and theming support.
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics firebaseAnalytics;
    private NavigationBarView navigationBarView;
    private FloatingActionButton quickPlayFab;
    private MaterialToolbar toolbar;
    private boolean doubleBackToExitPressedOnce = false;
    private String clickAction = "default";
    private SharedPreferences notificationPreferences;
    private NavController navController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemePreferences.applyStoredTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle analyticsBundle = new Bundle();
        analyticsBundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainActivity");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, analyticsBundle);
        notificationPreferences = getSharedPreferences("Setting", Context.MODE_PRIVATE);
        clickAction = extractClickAction();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this::onToolbarMenuItemClick);

        navigationBarView = findViewById(R.id.navigationView);
        quickPlayFab = findViewById(R.id.fabQuickPlay);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_main);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            setupNavigation();
        }

        quickPlayFab.setOnClickListener(view -> navigateToPlay("MainActivity"));
        initNotificationPreference();
    }

    private void setupNavigation() {
        navigationBarView.setOnItemSelectedListener(item -> {
            if (navController == null) {
                return false;
            }
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == item.getItemId()) {
                return true;
            }
            if (item.getItemId() == R.id.playFragment) {
                navigateToPlay(clickAction);
                return true;
            }
            navController.navigate(item.getItemId(), null, defaultNavOptions());
            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> syncBottomNavigation(destination));

        if (!TextUtils.isEmpty(clickAction) && !"default".equalsIgnoreCase(clickAction)) {
            navigateToPlay(clickAction);
            navigationBarView.setSelectedItemId(R.id.playFragment);
        } else {
            navigationBarView.setSelectedItemId(R.id.homeFragment);
        }
    }

    private void syncBottomNavigation(@NonNull NavDestination destination) {
        if (destination.getId() == R.id.playFragment) {
            navigationBarView.setSelectedItemId(R.id.playFragment);
        } else if (destination.getId() == R.id.walletFragment) {
            navigationBarView.setSelectedItemId(R.id.walletFragment);
        } else if (destination.getId() == R.id.profileFragment) {
            navigationBarView.setSelectedItemId(R.id.profileFragment);
        } else if (destination.getId() == R.id.leaderboardFragment) {
            navigationBarView.setSelectedItemId(R.id.leaderboardFragment);
        } else {
            navigationBarView.setSelectedItemId(R.id.homeFragment);
        }
    }

    private void navigateToPlay(String action) {
        if (navController == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("click_action", action);
        navController.navigate(R.id.playFragment, bundle, defaultNavOptions());
        clickAction = "default";
    }

    private NavOptions defaultNavOptions() {
        return new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                .build();
    }

    private boolean onToolbarMenuItemClick(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            showNotificationDialog();
            return true;
        } else if (item.getItemId() == R.id.action_theme) {
            ThemePreferences.toggleTheme(this);
            recreate();
            return true;
        }
        return false;
    }

    private void showNotificationDialog() {
        boolean subscribed = notificationPreferences.getString("SUB_STATUS", "true").equals("true");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notification_preferences, null);
        MaterialSwitch notificationSwitch = dialogView.findViewById(R.id.switchNotificationDialog);
        notificationSwitch.setChecked(subscribed);
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateNotificationSubscription(isChecked));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.notifications)
                .setView(dialogView)
                .setPositiveButton(R.string.open_notifications, (dialog, which) -> {
                    startActivity(new Intent(this, NotificationActivity.class));
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void initNotificationPreference() {
        boolean subscribed = notificationPreferences.getString("SUB_STATUS", "true").equals("true");
        updateNotificationSubscription(subscribed);
    }

    private void updateNotificationSubscription(boolean enable) {
        if (enable) {
            FirebaseMessaging.getInstance().subscribeToTopic(AppConstant.TOPIC_GLOBAL);
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(AppConstant.TOPIC_GLOBAL);
        }
        notificationPreferences.edit().putString("SUB_STATUS", enable ? "true" : "false").apply();
    }

    private String extractClickAction() {
        try {
            return Objects.requireNonNull(getIntent().getExtras()).getString("click_action", "default");
        } catch (Exception exception) {
            return "default";
        }
    }

    @Override
    public void onBackPressed() {
        if (navController != null && navController.popBackStack()) {
            return;
        }
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1500);
    }
}

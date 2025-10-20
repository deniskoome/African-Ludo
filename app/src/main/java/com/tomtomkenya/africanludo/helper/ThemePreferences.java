package com.tomtomkenya.africanludo.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Persisted theme helper allowing users to toggle between light and dark modes.
 */
public final class ThemePreferences {

    private static final String PREFS_NAME = "ludo_theme_pref";
    private static final String KEY_MODE = "theme_mode";

    private ThemePreferences() {
    }

    public static void applyStoredTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(readMode(context));
    }

    public static void toggleTheme(Context context) {
        int current = readMode(context);
        int next;
        if (current == AppCompatDelegate.MODE_NIGHT_YES) {
            next = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (current == AppCompatDelegate.MODE_NIGHT_NO) {
            next = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        } else {
            next = AppCompatDelegate.MODE_NIGHT_YES;
        }
        saveMode(context, next);
        AppCompatDelegate.setDefaultNightMode(next);
    }

    private static int readMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private static void saveMode(Context context, int mode) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(KEY_MODE, mode).apply();
    }
}

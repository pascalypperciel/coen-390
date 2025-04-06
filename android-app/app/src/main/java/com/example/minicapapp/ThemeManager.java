package com.example.minicapapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class ThemeManager {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";

    public enum Theme {
        DEFAULT,
        DARK,
        MODERN
    }

    public static void setTheme(Context context, Theme theme) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_THEME, theme.name());
        editor.apply();
    }

    public static Theme getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String name = prefs.getString(KEY_THEME, Theme.DEFAULT.name());

        try {
            return Theme.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Theme.DEFAULT;
        }
    }

    public static int getTextColor(Context context) {
        switch (getTheme(context)) {
            case DARK: return Color.WHITE;
            case MODERN: return Color.parseColor("#2C3E50");
            default: return Color.parseColor("#2B313B"); // DEFAULT
        }
    }

    public static int getButtonColor(Context context) {
        switch (getTheme(context)) {
            case DARK: return Color.parseColor("#6666FF");
            case MODERN: return Color.parseColor("#3498DB");
            default: return Color.parseColor("#F39B53"); // DEFAULT
        }
    }

    public static int getNavbarColor(Context context) {
        switch (getTheme(context)) {
            case DARK: return Color.parseColor("#333333");
            case MODERN: return Color.parseColor("#1A252F");
            default: return Color.parseColor("#157A5D"); // DEFAULT
        }
    }

    public static int getBackgroundColor(Context context) {
        switch (getTheme(context)) {
            case DARK: return Color.parseColor("#121212");
            case MODERN: return Color.parseColor("#ECEFF1");
            default: return Color.parseColor("#DCF9F1"); // DEFAULT
        }
    }
}

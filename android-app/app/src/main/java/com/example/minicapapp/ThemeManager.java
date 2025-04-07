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
        MODERN,
        MODERN_COASTAL,
        SUNRISE_BLUSH,
        FOREST_TECH
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
            case MODERN: return Color.parseColor("#1A1A1A");
            case MODERN_COASTAL: return Color.parseColor("#2C3E50");
            case SUNRISE_BLUSH: return Color.parseColor("#4E342E");
            case FOREST_TECH: return Color.parseColor("#1B5E20");
            default: return Color.parseColor("#2B313B"); // DEFAULT
        }
    }

    public static int getButtonColor(Context context) {
        switch (getTheme(context)) {
            case DARK: return Color.parseColor("#750E21");
            case MODERN: return Color.parseColor("#5E5DF0");
            case MODERN_COASTAL: return Color.parseColor("#3498DB");
            case SUNRISE_BLUSH: return Color.parseColor("#FF7043");
            case FOREST_TECH: return Color.parseColor("#66BB6A");
            default: return Color.parseColor("#DA537B"); // DEFAULT
        }
    }

    public static int getNavbarColor(Context context) {
        switch (getTheme(context)) {
            case DARK: return Color.parseColor("#121212");
            case MODERN: return Color.parseColor("#FFFFFF");
            case MODERN_COASTAL: return Color.parseColor("#1A252F");
            case SUNRISE_BLUSH: return Color.parseColor("#BF360C");
            case FOREST_TECH: return Color.parseColor("#2E7D32");
            default: return Color.parseColor("#157A5D"); // DEFAULT
        }
    }

    public static int getBackgroundColor(Context context) {
        switch (getTheme(context)) {
            case DARK: return Color.parseColor("#1E1E1E");
            case MODERN: return Color.parseColor("#F9F9F9");
            case MODERN_COASTAL: return Color.parseColor("#ECEFF1");
            case SUNRISE_BLUSH: return Color.parseColor("#FFF3E0");
            case FOREST_TECH: return Color.parseColor("#E8F5E9");
            default: return Color.parseColor("#DCF9F1"); // DEFAULT
        }
    }
}

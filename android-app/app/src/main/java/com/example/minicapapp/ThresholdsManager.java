package com.example.minicapapp;

import android.content.Context;
import android.content.SharedPreferences;

public class ThresholdsManager {
    private static final String PREFS_NAME = "ThresholdPrefs";
    private static final String MAX_PRESSURE = "max_pressure";
    private static final String MAX_DISTANCE = "max_distance";
    private static final String MIN_DISTANCE = "min_distance";
    private static final String YOUNG_MODULUS = "young_modulus";

    public static float getMaxPressure(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getFloat(MAX_PRESSURE, 1000.0f);
    }

    public static float getMaxDistance(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getFloat(MAX_DISTANCE, 15.0f);
    }

    public static float getMinDistance(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getFloat(MIN_DISTANCE, -10f);
    }

    public static double getYoungModulus(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getFloat(YOUNG_MODULUS, 1000.0f);
    }

    public static void setThresholds(Context context, float maxPressure, float maxDistance, float minDistance, double youngModulus) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putFloat(MAX_PRESSURE, maxPressure);
        editor.putFloat(MAX_DISTANCE, maxDistance);
        editor.putFloat(MIN_DISTANCE, minDistance);
        editor.putFloat(YOUNG_MODULUS, (float) youngModulus);
        editor.apply();
    }
}

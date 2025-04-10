package com.example.minicapapp;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationBarActivity extends AppCompatActivity {
    // The bottom navigation bar for centralized and persistent navigation
    protected BottomNavigationView bottomNavigationViewPersistentNavbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Universal Android Theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navbar_colour));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_colour));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation_bar);

        // Set the orientation of the application to Portrait, exclusively
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Load the persistent navbar into the local BottomNavigationView attribute by its ID
        bottomNavigationViewPersistentNavbar = findViewById(R.id.bottomNavigationViewPersistentNavbar);

        // Theme
        setAppTheme();
        
        // Set an invalid menu ID to clear the initial selection
        if(savedInstanceState == null) {
            replaceFragment(new ControllerFragment(), false);
        }

        // Handle all of the possible input when the different icons of the persistent navbar are pressed
        bottomNavigationViewPersistentNavbar.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.action_load_controller) {
                replaceFragment(new ControllerFragment(), true);
                return true;
            } else if (item.getItemId() == R.id.action_load_recorded_data) {
                replaceFragment(new RecordedDataFragment(), true);
                return true;
            } else if (item.getItemId() == R.id.action_load_settings) {
                replaceFragment(new SettingsFragment(), true);
                return true;
            } else if (item.getItemId() == R.id.action_load_materials_information_page) {
                replaceFragment(new MaterialsInformationFragment(), true);
                return true;
            } else {
                return false;
            }
        });

        setupBottomNavListener();
        
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayoutActivityContent);
            if (currentFragment instanceof ControllerFragment) {
                setBottomNavSelectedItemWithoutTriggering(R.id.action_load_controller);
            } else if (currentFragment instanceof RecordedDataFragment) {
                setBottomNavSelectedItemWithoutTriggering(R.id.action_load_recorded_data);
            } else if (currentFragment instanceof SettingsFragment) {
                setBottomNavSelectedItemWithoutTriggering(R.id.action_load_settings);
            } else if (currentFragment instanceof MaterialsInformationFragment) {
                setBottomNavSelectedItemWithoutTriggering(R.id.action_load_materials_information_page);
            } else if (currentFragment instanceof BluetoothFragment) {
                setBottomNavSelectedItemWithoutTriggering(R.id.action_load_settings);
            }
        });
    }

    private void setAppTheme() {
        int backgroundColor = ThemeManager.getBackgroundColor(this);
        int navbarColor = ThemeManager.getNavbarColor(this);
        int textColor = ThemeManager.getTextColor(this);
        int buttonColor = ThemeManager.getButtonColor(this);

        getWindow().getDecorView().setBackgroundColor(backgroundColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(navbarColor);
            getWindow().setStatusBarColor(backgroundColor);
        }

        if (bottomNavigationViewPersistentNavbar != null) {
            bottomNavigationViewPersistentNavbar.setBackgroundColor(navbarColor);
            bottomNavigationViewPersistentNavbar.setItemIconTintList(ColorStateList.valueOf(textColor));
            bottomNavigationViewPersistentNavbar.setItemTextColor(ColorStateList.valueOf(textColor));
            bottomNavigationViewPersistentNavbar.setItemActiveIndicatorColor(ColorStateList.valueOf(buttonColor));
        }
    }


    // Internal method that will allow the persistent navbar to switch between fragment easily
    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutActivityContent, fragment);
        if (addToBackStack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void setupBottomNavListener() {
        bottomNavigationViewPersistentNavbar.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.action_load_controller) {
                replaceFragment(new ControllerFragment(), true);
                return true;
            } else if (item.getItemId() == R.id.action_load_recorded_data) {
                replaceFragment(new RecordedDataFragment(), true);
                return true;
            } else if (item.getItemId() == R.id.action_load_settings) {
                replaceFragment(new SettingsFragment(), true);
                return true;
            } else if (item.getItemId() == R.id.action_load_materials_information_page) {
                replaceFragment(new MaterialsInformationFragment(), true);
                return true;
            } else {
                return false;
            }
        });
    }

    public void setBottomNavSelectedItemWithoutTriggering(int itemId) {
        bottomNavigationViewPersistentNavbar.setOnItemSelectedListener(null);
        bottomNavigationViewPersistentNavbar.setSelectedItemId(itemId);
        setupBottomNavListener();
    }
}
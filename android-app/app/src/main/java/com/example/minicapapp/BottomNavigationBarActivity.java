package com.example.minicapapp;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationBarActivity extends AppCompatActivity {
    // The bottom navigation bar for centralized and persistent navigation
    protected BottomNavigationView bottomNavigationViewPersistentNavbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation_bar);

        // Set the orientation of the application to Portrait, exclusively
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Load the persistent navbar into the local BottomNavigationView attribute by its ID
        bottomNavigationViewPersistentNavbar = findViewById(R.id.bottomNavigationViewPersistentNavbar);

        // Set an invalid menu ID to clear the initial selection
        replaceFragment(new ControllerFragment(), false);

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
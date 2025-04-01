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
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationBarActivity extends AppCompatActivity {
    // The bottom navigation bar for centralized and persistent navigation
    protected BottomNavigationView bottomNavigationViewPersistentNavbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bottom_navigation_bar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load the persistent navbar into the local BottomNavigationView attribute by its ID
        bottomNavigationViewPersistentNavbar = findViewById(R.id.bottomNavigationViewPersistentNavbar);

        // Set an invalid menu ID to clear the initial selection
        replaceFragment(new ControllerFragment());

        // Handle all of the possible input when the different icons of the persistent navbar are pressed
        bottomNavigationViewPersistentNavbar.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.action_load_controller) {
                replaceFragment(new ControllerFragment());
                return true;
            } else if (item.getItemId() == R.id.action_load_recorded_data) {
                replaceFragment(new RecordedDataFragment());
                return true;
            } else if (item.getItemId() == R.id.action_load_settings) {
                replaceFragment(new SettingsFragment());
                return true;
            } else if (item.getItemId() == R.id.action_load_materials_information_page) {
                replaceFragment(new MaterialsInformationFragment());
                return true;
            } else {
                return false;
            }
        });

    }

    // Internal method that will allow the persistent navbar to switch between fragment easily
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutActivityContent, fragment);
        fragmentTransaction.commit();
    }

}
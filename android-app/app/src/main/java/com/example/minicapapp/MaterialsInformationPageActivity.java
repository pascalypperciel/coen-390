package com.example.minicapapp;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MaterialsInformationPageActivity extends AppCompatActivity {
    // The UI elements present on the Materials Information Page Activity.
    protected Toolbar toolbarMaterialsInformationPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_materials_information_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // This method will be used to set up all of the UI elements in the Main Activity
        setupUI();
    }

    // Setup Functions for the Appbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Load the main_appbar_resource as an object
        getMenuInflater().inflate(R.menu.menu_appbar_resource, menu);

        // Define the Toolbar Items and change their colour
        MenuItem helpItem = menu.findItem(R.id.action_help);
        helpItem.getIcon().setColorFilter(getResources().getColor(R.color.white, null), PorterDuff.Mode.SRC_IN);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.action_help == item.getItemId()) {
            HelpFragment helpDialogueFragment = new HelpFragment();
            helpDialogueFragment.show(getSupportFragmentManager(), "Help");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupUI() {
        // Toolbar
        toolbarMaterialsInformationPage = findViewById(R.id.toolbarMaterialsInformationPage);
        setSupportActionBar(toolbarMaterialsInformationPage);
        getSupportActionBar().setTitle("Materials Information");
    }
}
package com.example.minicapapp;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    // The UI elements present on the Main Activity.
    // TODO: Create Behaviour for the Main Activity appbar
    protected Toolbar toolbarMain;
    protected Button btnRecordedData;
    // TODO: Inject the behaviour of the Help and Settings button into the appbar
    protected Button btnController;
    protected Button btnMaterialsInformationPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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
        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.getIcon().setColorFilter(getResources().getColor(R.color.white, null), PorterDuff.Mode.SRC_IN);

        MenuItem helpItem = menu.findItem(R.id.action_help);
        helpItem.getIcon().setColorFilter(getResources().getColor(R.color.white, null), PorterDuff.Mode.SRC_IN);

        return true;
    }

    private void setupUI() {
        // Toolbar
        toolbarMain = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbarMain);
        getSupportActionBar().setTitle("Main Activity");
        // Change the title colour
        toolbarMain.setTitleTextColor(getResources().getColor(R.color.lightOrange, null));

        // This button will navigate to the Recorded Data Class.
        btnRecordedData =findViewById(R.id.buttonRecordedData);
        btnRecordedData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRecordedDataActivity();
            }
        });

        // This button will navigate to the Controller Class.
        btnController =findViewById(R.id.buttonController);
        btnController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToControllerActivity();
            }
        });

        // This button will open a dialogue fragment to the Materials Information page.
        btnMaterialsInformationPage =findViewById(R.id.buttonMaterialsInformationPage);
        btnMaterialsInformationPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"add profile", Toast.LENGTH_LONG).show();
                getSupportFragmentManager().beginTransaction().add(R.id.container, new InfoFrag()).commit();

            }
        });
    }

    private void goToControllerActivity() {
        Intent controllerIntent= new Intent(this, ControllerActivity.class);
        startActivity(controllerIntent);
    }

    private void goToRecordedDataActivity() {
        Intent recordedDataIntent= new Intent(this, RecordedDataActivity.class);
        startActivity(recordedDataIntent);
    }

    private void goToSettingsActivity() {
        Intent settingsIntent= new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }
}
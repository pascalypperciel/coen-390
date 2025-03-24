package com.example.minicapapp;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
    protected Toolbar toolbarMain;
    protected Button buttonController;
    protected Button buttonBluetooth;
    protected Button buttonRecordedData;
    protected Button buttonMaterialsInformationPage;

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(R.id.action_settings == item.getItemId()) {
            goToSettingsActivity();
            return true;
        } else if(R.id.action_help == item.getItemId()) {
            HelpFrag helpDialogueFragment = new HelpFrag();
            helpDialogueFragment.show(getSupportFragmentManager(), "Help");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupUI() {
        // Toolbar
        toolbarMain = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbarMain);
        getSupportActionBar().setTitle("Main Menu");
        // Change the title colour
        toolbarMain.setTitleTextColor(getResources().getColor(R.color.white, null));

        // This button will navigate to the Controller Activity.
        buttonController = findViewById(R.id.buttonController);
        buttonController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToControllerActivity();
            }
        });

        // This button will navigate to the Bluetooth Activity.
        buttonBluetooth = findViewById(R.id.buttonBluetooth);
        buttonBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToBluetoothActivity();
            }
        });

        // This button will navigate to the Recorded Data Activity.
        buttonRecordedData = findViewById(R.id.buttonRecordedData);
        buttonRecordedData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRecordedDataActivity();
            }
        });

        // This button will open a dialogue fragment to the Materials Information Page Activity.
        buttonMaterialsInformationPage = findViewById(R.id.buttonMaterialsInformationPage);
        buttonMaterialsInformationPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMaterialsInformationPageActivity();
            }
        });
    }



    // Internal methods to create and start the intents linked to the Main Activity
    private void goToSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void goToControllerActivity() {
        Intent controllerIntent = new Intent(this, ControllerActivity.class);
        startActivity(controllerIntent);
    }

    private void goToBluetoothActivity() {
        Intent bluetoothIntent = new Intent(this, BluetoothActivity.class);
        startActivity(bluetoothIntent);
    }

    private void goToRecordedDataActivity() {
        Intent recordedDataIntent = new Intent(this, RecordedDataActivity.class);
        startActivity(recordedDataIntent);
    }

    private void goToMaterialsInformationPageActivity() {
        Intent materialsInformationPageIntent = new Intent(this, MaterialsInformationPageActivity.class);
        startActivity(materialsInformationPageIntent);
    }
}
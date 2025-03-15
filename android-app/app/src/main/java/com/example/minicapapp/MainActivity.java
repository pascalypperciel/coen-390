package com.example.minicapapp;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    // The UI elements present on the Main Activity.
    protected Button datab;
    protected Button controlsb;
    protected Button helpb;
    protected Button settingsb;
    protected Button infob;

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

    private void setupUI() {
        // This button will navigate to the Recorded Data Class.
        datab=findViewById(R.id.gotodata);
        datab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDataClass();
            }
        });

        // This button will navigate to the Controller Class.
        controlsb=findViewById(R.id.controls);
        controlsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToControlsClass();
            }
        });

        // This button will open a dialogue fragment to explain the Activity's functionality.
        helpb=findViewById(R.id.help);
        helpb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"add profile", Toast.LENGTH_LONG).show();
                getSupportFragmentManager().beginTransaction().add(R.id.container, new HelpFrag()).commit();

            }
        });

        // This button will navigate to the Settings Activity.
        settingsb=findViewById(R.id.settings);
        settingsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettingsClass();
            }
        });

        // This button will open a dialogue fragment to the Materials Information page.
        infob=findViewById(R.id.infob);
        infob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"add profile", Toast.LENGTH_LONG).show();
                getSupportFragmentManager().beginTransaction().add(R.id.container, new InfoFrag()).commit();

            }
        });
    }

    private void goToControlsClass() {
        Intent cintent= new Intent(this, Controls.class);
        startActivity(cintent);
    }

    private void goToDataClass() {
        Intent dintent= new Intent(this, Data.class);
        startActivity(dintent);
    }

    private void goToSettingsClass() {
        Intent sintent= new Intent(this, Settings.class);
        startActivity(sintent);
    }
}
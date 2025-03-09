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
    protected Button datab, controlsb, helpb, settingsb, infob;

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

        helpb=findViewById(R.id.help);
        settingsb=findViewById(R.id.settings);
        datab=findViewById(R.id.gotodata);
        controlsb=findViewById(R.id.controls);
        infob=findViewById(R.id.infob);

        datab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDataClass();
            }
        });
        controlsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToControlsClass();
            }
        });
        settingsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettingsClass();
            }
        });

        helpb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"add profile", Toast.LENGTH_LONG).show();
                getSupportFragmentManager().beginTransaction().add(R.id.container, new HelpFrag()).commit();

            }
        });
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
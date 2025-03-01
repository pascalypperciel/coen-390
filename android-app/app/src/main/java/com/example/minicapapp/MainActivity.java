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
import android.widget.TextView;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
                gotodataclass();
            }
        });
      /*  controlsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotocontrolsclass();
            }
        });
        settingsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotosettingsclass();
            }
        });*/

       /* helpb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"add profile", Toast.LENGTH_LONG).show();
                getSupportFragmentManager().beginTransaction().add(R.id.container, new helpfrag()).commit();

            }
        });
        infob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"add profile", Toast.LENGTH_LONG).show();
                getSupportFragmentManager().beginTransaction().add(R.id.container, new infofrag()).commit();

            }
        });*/

    }

    /*private void gotocontrolsclass() {
        Intent cintent= new Intent(this, controls.class);
        startActivity(cintent);
    }*/

    private void gotodataclass() {
        Intent dintent= new Intent(this, data.class);
        startActivity(dintent);
    }

    /*private void gotosettingsclass() {
        Intent sintent= new Intent(this, settings.class);
        startActivity(sintent);
    }*/
}
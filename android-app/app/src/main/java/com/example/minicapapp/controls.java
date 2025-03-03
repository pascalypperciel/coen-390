package com.example.minicapapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationBarView;

public class controls extends AppCompatActivity {

    protected Button bmotorfwd, bmotorbwd,stopb, recordb;
    protected Toolbar toolbar;

    protected TextView  statusbth;
    protected Spinner cspinner, tspinner;
    protected EditText thinput;

    protected String selectedtest, selectedmaterial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controls);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bmotorbwd = findViewById(R.id.motorbwd);
        bmotorfwd = findViewById(R.id.motorfwd);
        stopb = findViewById(R.id.stop);
        recordb= findViewById(R.id.record);

        thinput= findViewById(R.id.thinput);
        thinput.setVisibility(View.INVISIBLE);

        statusbth= findViewById(R.id.connectionstatus);

        cspinner= findViewById(R.id.cspinner);
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.materials, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        cspinner.setAdapter(adapter);

        tspinner= findViewById(R.id.tspinner);
        ArrayAdapter<CharSequence>testadapter=ArrayAdapter.createFromResource(this, R.array.tests, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        tspinner.setAdapter(testadapter);

        //selectedmaterial= cspinner.getSelectedItem().toString();
        //Toast.makeText(getApplicationContext(), "you selected: " + selectedmaterial, Toast.LENGTH_LONG).show();

        cspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                selectedmaterial= cspinner.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), "you selected: " + selectedmaterial, Toast.LENGTH_LONG).show();
                if("New material(manually set threshold)".equals(selectedmaterial)){
                    Toast.makeText(getApplicationContext(), "HAPPY BIRTHDAY TO NEW MATERIAL", Toast.LENGTH_LONG).show();
                    thinput.setVisibility(View.VISIBLE);

                }else{
                    thinput.setVisibility(View.INVISIBLE);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        tspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                selectedtest= tspinner.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), "you selected: " + selectedtest, Toast.LENGTH_LONG).show();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });


        bmotorbwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "motor turning bwd", Toast.LENGTH_LONG).show();
            }
        });
        bmotorfwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "motor turning fwd", Toast.LENGTH_LONG).show();
            }
        });
        stopb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "STOP", Toast.LENGTH_LONG).show();
            }
        });
        recordb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "recording", Toast.LENGTH_LONG).show();
            }
        });
    }


    //for menu icon in the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Menu menu=toolbar.getMenu();

        MenuItem mbhelp = menu.findItem(R.id.mbhelp);
        MenuItem mbsettings = menu.findItem(R.id.mbsettings);
        int id=item.getItemId();
        if (mbhelp.getItemId()==id) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new helpfrag()).commit();
            //Toast.makeText(getApplicationContext(), "clicked on go to help", Toast.LENGTH_LONG).show();

        }
        else if(mbsettings.getItemId()==id) {
            Intent sintent= new Intent(this, settings.class);
            startActivity(sintent);
            //Toast.makeText(getApplicationContext(), "clicked on go to settings", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
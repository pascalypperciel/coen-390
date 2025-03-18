package com.example.minicapapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //for menu icon in the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.settingsmenu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Menu smenu=toolbar.getMenu();

        MenuItem mbhelp = smenu.findItem(R.id.mbhelp);
        int id=item.getItemId();
        if (mbhelp.getItemId()==id) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new HelpFrag()).commit();

        }else if(android.R.id.home==id){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
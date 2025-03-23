package com.example.minicapapp;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class RecordedDataActivity extends AppCompatActivity {
    // The UI elements present on the Recorded Data Activity.
    protected Toolbar toolbarRecordedData;
    protected Spinner spinnerFilter;
    protected TextView textViewSummary;
    protected RecyclerView recyclerViewRecordedDataList;
    protected RecordedDataListRecyclerViewAdapter recordedDataListRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // This method will be used to set up all of the UI elements in the Main Activity
        setupUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // The "finish()" will navigate back to the previous activity.
        return true;
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
        toolbarRecordedData = findViewById(R.id.toolbarRecordedData);
        setSupportActionBar(toolbarRecordedData);
        getSupportActionBar().setTitle("Recorded Data Activity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Filter
        spinnerFilter = findViewById(R.id.spinnerFilter);
        // TODO: Figure out how to handle the spinner

        // Recorded Data List
        setupRecyclerView();

        // Summary of the Recorded Data
        textViewSummary = findViewById(R.id.textViewSummary);
        textViewSummary.setText(Integer.toString(recordedDataListRecyclerViewAdapter.getItemCount()) + " Sessions, filtered by...");
        // TODO: Change this summary once filtering has been integrated.
    }

    // Create and set up the list of profiles
    // TODO: Adapt this method to the context-specific
    protected void setupRecyclerView() {
        // Retrieve the recorded data list stored in the database.
        // TODO: Retrieve the information from the PB and create a list of RecordedDataItem objects
//        List<RecordedDataItem> recordedDataList;

        // Bind and organize the profile list items.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        recordedDataListRecyclerViewAdapter = new RecordedDataListRecyclerViewAdapter(this, recordedDataList);

        // Define and initialize the Recycler View.
        recyclerViewRecordedDataList = findViewById(R.id.recyclerViewRecordedDataList);
        recyclerViewRecordedDataList.setLayoutManager(linearLayoutManager);
//        recyclerViewRecordedDataList.setAdapter(recordedDataListRecyclerViewAdapter);

        // Adding a border around each item.
        DividerItemDecoration border = new DividerItemDecoration(recyclerViewRecordedDataList.getContext(), linearLayoutManager.getOrientation());
        recyclerViewRecordedDataList.addItemDecoration(border);
    }

    // This method will allow the Settings Activity to be accessed from the Recorded Data Activity
    private void goToSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    // This method will update the UI
    protected void updateUI() {
        // Retrieve the Recorded Data List Stored in the database.
        // TODO: Retrieve the information from the PB and create a list of RecordedDataItem objects
        List<RecordedDataItem> recordedDataItemList;

//        recordedDataListRecyclerViewAdapter.updateList(recordedDataItemList);

        textViewSummary.setText(Integer.toString(recordedDataListRecyclerViewAdapter.getItemCount()) + " Sessions, filtered by...");
        // TODO: Change this summary once filtering has been integrated.
    }
}
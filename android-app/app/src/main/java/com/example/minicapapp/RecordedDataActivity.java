package com.example.minicapapp;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class RecordedDataActivity extends AppCompatActivity {
    // The UI elements present on the Recorded Data Activity.
    protected Toolbar toolbarRecordedData;
    protected Spinner spinnerFilter;
    protected TextView textViewSummary;
    protected RecyclerView recyclerViewRecordedDataList;
    protected RecordedDataListRecyclerViewAdapter recordedDataListRecyclerViewAdapter;
    private List<RecordedDataItem> allSessions = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recorded_data);
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
        toolbarRecordedData = findViewById(R.id.toolbarRecordedData);
        setSupportActionBar(toolbarRecordedData);
        getSupportActionBar().setTitle("Recorded Data");

        // Recorded Data
        spinnerFilter = findViewById(R.id.spinnerFilter);
        textViewSummary = findViewById(R.id.textViewSummary);
        recyclerViewRecordedDataList = findViewById(R.id.recyclerViewRecordedDataList);

        getData(sessions -> {
            allSessions = sessions;

            // Setup spinner
            List<String> sessionNames = new ArrayList<>();
            for (RecordedDataItem session : sessions) {
                sessionNames.add(session.getSessionName());
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sessionNames);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFilter.setAdapter(spinnerAdapter);

            // Setup RecyclerView
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recordedDataListRecyclerViewAdapter = new RecordedDataListRecyclerViewAdapter(this, sessions);
            recyclerViewRecordedDataList.setLayoutManager(linearLayoutManager);
            recyclerViewRecordedDataList.setAdapter(recordedDataListRecyclerViewAdapter);
            recyclerViewRecordedDataList.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));

            textViewSummary.setText(sessions.size() + " Sessions, filtered by...");

            // Spinner filtering logic
            spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedSessionName = sessionNames.get(position);
                    List<RecordedDataItem> filteredList = new ArrayList<>();
                    for (RecordedDataItem item : allSessions) {
                        if (item.getSessionName().equals(selectedSessionName)) {
                            filteredList.add(item);
                        }
                    }
                    recordedDataListRecyclerViewAdapter.updateList(filteredList);
                    textViewSummary.setText("1 Session: " + selectedSessionName);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    recordedDataListRecyclerViewAdapter.updateList(allSessions);
                    textViewSummary.setText(allSessions.size() + " Sessions");
                }
            });
        });
    }

    // This method will update the UI
    protected void updateUI() {
        getData(sessions -> {
            allSessions = sessions;
            recordedDataListRecyclerViewAdapter.updateList(sessions);
            textViewSummary.setText(sessions.size() + " Sessions, filtered by...");
        });
    }

    void getData(DataCallback callback) {
        List<RecordedDataItem> sessionList = new ArrayList<>();

        new Thread(() -> {
            try {
                URL url = new URL("https://cat-tester-api.azurewebsites.net/get-all-sessions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject responseJson = new JSONObject(result.toString());
                JSONArray records = responseJson.getJSONArray("list of tests");
                // Loop through each record in the JSON array
                for (int i = 0; i < records.length(); i++) {
                    JSONObject record = records.getJSONObject(i);
                    RecordedDataItem item = new RecordedDataItem(
                            record.getLong("sessionid"),
                            record.getString("sessionname"),
                            (float) record.getDouble("initiallength"),
                            (float) record.getDouble("initialarea")
                    );
                    sessionList.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> callback.onDataFetched(sessionList));
        }).start();
    }

    interface DataCallback {
        void onDataFetched(List<RecordedDataItem> sessionList);
    }
}
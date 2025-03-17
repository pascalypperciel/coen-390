package com.example.minicapapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.TextView;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class RecordedDataActivity extends AppCompatActivity {
    private TextView txtResponse;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button btnFetch;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        txtResponse = findViewById(R.id.txtResponse);
        btnFetch = findViewById(R.id.btnFetch);

        btnFetch.setOnClickListener(v -> new FetchDataTask().execute());

        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        //enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private class FetchDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://10.0.2.2:5000/get-all");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                return result.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            txtResponse.setText(result);
        }
    }

    //for menu icon in the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_appbar_resource, menu);

        return true;
    }
}
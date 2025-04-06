package com.example.minicapapp;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class RecordedDataFragment extends Fragment {
    // Internal Attributes
    private List<RecordedDataItem> allSessions = new ArrayList<>();
    private List<String> filteringOptions = new ArrayList<>();

    // The UI elements present in the Recorded Data Fragment
    protected ImageView imageViewLogo;
    protected ImageButton imageButtonHelpRecordedData;
    protected Spinner spinnerDataFilter;
    protected RecyclerView recyclerViewRecordedData;
    protected RecordedDataRecyclerViewAdapter recordedDataRecyclerViewAdapter;



    public RecordedDataFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recorded_data, container, false);

        // Theme
        int backgroundColor = ThemeManager.getBackgroundColor(requireContext());
        int textColor = ThemeManager.getTextColor(requireContext());
        int buttonColor = ThemeManager.getButtonColor(requireContext());

        view.setBackgroundColor(backgroundColor);

        TextView sortLabel = view.findViewById(R.id.textViewSortLabel);
        sortLabel.setTextColor(textColor);

        // Define and set the behaviour of the UI elements in ths fragment
        // Logo
        imageViewLogo = view.findViewById(R.id.imageViewLogo);
        imageViewLogo.setBackgroundColor(backgroundColor);

        // Help Button
        imageButtonHelpRecordedData = view.findViewById(R.id.imageButtonHelpRecordedData);
        imageButtonHelpRecordedData.setColorFilter(buttonColor);
        imageButtonHelpRecordedData.getBackground().setTint(buttonColor);
        imageButtonHelpRecordedData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpFragment helpFragment = new HelpFragment();
                helpFragment.show(getActivity().getSupportFragmentManager(), "HelpDialogue");
            }
        });

        // Recycler View
        getData(sessions -> {
            allSessions = sessions;

            // Setup RecyclerView
            // Bind and organize the sessions items.
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
            recordedDataRecyclerViewAdapter = new RecordedDataRecyclerViewAdapter(requireActivity(), sessions);

            // Define and initialize the RecyclerView.
            recyclerViewRecordedData = view.findViewById(R.id.recyclerViewRecordedData);
            recyclerViewRecordedData.setBackgroundColor(backgroundColor);
            recyclerViewRecordedData.setLayoutManager(linearLayoutManager);
            recyclerViewRecordedData.setAdapter(recordedDataRecyclerViewAdapter);

            // Adding a border around each item.
            DividerItemDecoration border = new DividerItemDecoration(recyclerViewRecordedData.getContext(), linearLayoutManager.getOrientation());
            recyclerViewRecordedData.addItemDecoration(border);
        });

        // Spinner
        // Define the spinner logic
        spinnerDataFilter = view.findViewById(R.id.spinnerDataFilter);
        GradientDrawable spinnerBackground = (GradientDrawable) spinnerDataFilter.getBackground();
        spinnerBackground.setStroke(2, ThemeManager.getButtonColor(requireContext()));
        spinnerDataFilter.setPopupBackgroundDrawable(new ColorDrawable(backgroundColor));
        spinnerDataFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();

                if(!item.equals("None")) {
                    Toast.makeText(getActivity().getBaseContext(), "Filtering Method Selected: " + item, Toast.LENGTH_SHORT).show();

                    // Sort the Recycler View items based on the chosen sorting method.
                    if(recordedDataRecyclerViewAdapter != null) {
                        recordedDataRecyclerViewAdapter.sortData(item);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This rarely triggers.
            }
        });

        // Create the different filtering options available to the user
        filteringOptions.add("None");
        filteringOptions.add("1. Chronologically (Newest to Oldest)");
        filteringOptions.add("2. Chronologically (Oldest to Newest)");
        filteringOptions.add("3. Alphabetically (A to Z)");
        filteringOptions.add("4. Alphabetically (Z to A)");

        // Set up the drop-down view
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity().getBaseContext(), android.R.layout.simple_spinner_item, filteringOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinnerDataFilter.setAdapter(spinnerAdapter);

        return view;
    }

    void getData(RecordedDataFragment.DataCallback callback) {
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
                            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(record.getString("datecreated")),
                            (float) record.getDouble("initiallength"),
                            (float) record.getDouble("initialarea"),
                            (float) record.optDouble("yieldstrain", Double.NaN),
                            (float) record.optDouble("yieldstress", Double.NaN)
                    );
                    if (item.getSessionID() > 0) { //remove test sessions that have negative sessionId
                        sessionList.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> callback.onDataFetched(sessionList));
            }
        }).start();
    }

    interface DataCallback {
        void onDataFetched(List<RecordedDataItem> sessionList);
    }
}
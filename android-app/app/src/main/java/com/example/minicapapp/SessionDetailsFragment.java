package com.example.minicapapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;

import android.content.res.ColorStateList;

import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionDetailsFragment extends Fragment {

    private long sessionId;
    private LinearLayout graphContainer;
    private TableLayout tableLayout;
    private ProgressBar progressBar;
    private Button toggleTableButton;
    private Button backButton;

    private boolean isTableVisible = false;

    public SessionDetailsFragment() {
        // Required empty public constructor
    }

    private int buttonColor, backgroundColor, textColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sessionId = getArguments().getLong("session_id", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session_details, container, false);

        buttonColor = ThemeManager.getButtonColor(requireContext());
        backgroundColor = ThemeManager.getBackgroundColor(requireContext());
        textColor = ThemeManager.getTextColor(requireContext());

        view.setBackgroundColor(backgroundColor);

        graphContainer = view.findViewById(R.id.graphContainer);
        MaterialCardView graphCard = view.findViewById(R.id.graphCard);
        graphCard.setCardBackgroundColor(backgroundColor);
        graphCard.setStrokeColor(buttonColor);

        backButton = view.findViewById(R.id.backButton);
        backButton.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        backButton.setTextColor(textColor);
        backButton.setOnClickListener(v -> {
            if (isAdded()) {  // Ensure the fragment is still attached
                // Create an instance of the RecordedDataFragment
                RecordedDataFragment recordedDataFragment = new RecordedDataFragment();

                // Replace the current fragment with RecordedDataFragment
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayoutActivityContent, recordedDataFragment); // Replace with your container ID
                transaction.addToBackStack(null); // Optional: Add to back stack to allow back navigation
                transaction.commit();
            } else {
                Log.w("SessionDetailsFragment", "Fragment is not attached. Button click ignored.");
            }
        });

        tableLayout = view.findViewById(R.id.rawDataTable);
        progressBar = view.findViewById(R.id.graphLoadingSpinner);
        toggleTableButton = view.findViewById(R.id.toggleTableButton);
        toggleTableButton.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        toggleTableButton.setTextColor(textColor);

        toggleTableButton.setOnClickListener(v -> {
            isTableVisible = !isTableVisible;
            tableLayout.setVisibility(isTableVisible ? View.VISIBLE : View.GONE);
            toggleTableButton.setText(isTableVisible ? "Hide Raw Data" : "Show Raw Data");
        });

        fetchGraphAndData();

        return view;
    }

    private void fetchGraphAndData() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                String graphsURL = "https://cat-tester-api.azurewebsites.net/build-graphs?SessionID=" + sessionId;
                HttpURLConnection connection = (HttpURLConnection) new URL(graphsURL).openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                InputStream inputStream;

                if (responseCode >= 400) {
                    inputStream = connection.getErrorStream();
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        TextView errorText = new TextView(requireContext());
                        errorText.setText("No valid records found for this session.");
                        errorText.setPadding(32, 164, 32, 32);
                        errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        errorText.setTextSize(16);
                        graphContainer.addView(errorText);
                    });
                    return;
                } else {
                    inputStream = connection.getInputStream();
                }

                StringBuilder response = new StringBuilder();
                int ch;
                while ((ch = inputStream.read()) != -1) {
                    response.append((char) ch);
                }

                JSONObject jsonResponse = new JSONObject(response.toString());

                if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.getString("error");
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        TextView errorText = new TextView(requireContext());
                        errorText.setText("Error: " + errorMessage);
                        errorText.setTextColor(textColor);
                        errorText.setPadding(32, 64, 32, 32);
                        errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        errorText.setTextSize(16);
                        graphContainer.addView(errorText);
                    });
                    return;
                }

                JSONArray graphArray = jsonResponse.getJSONArray("Graph");

                String[] graphTitles = {
                        "Engineering Stress vs Strain",
                        "True Stress vs True Strain",
                        "Displacement vs Force",
                        "Load vs Time",
                        "Displacement vs Time"
                };

                requireActivity().runOnUiThread(() -> {
                    for (int i = 0; i < graphArray.length(); i++) {
                        try {
                            TextView graphTitle = new TextView(requireContext());
                            graphTitle.setText(graphTitles[i]);
                            graphTitle.setTextColor(textColor);
                            graphTitle.setTextSize(18);
                            graphTitle.setTextColor(getResources().getColor(R.color.black));
                            graphTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                            graphTitle.setPadding(0, 24, 0, 8);
                            graphTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            graphContainer.addView(graphTitle);

                            String base64 = graphArray.getString(i);
                            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ImageView imageView = new ImageView(requireContext());
                            imageView.setImageBitmap(bitmap);
                            imageView.setAdjustViewBounds(true);
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            imageView.setPadding(0, 0, 0, 32);
                            graphContainer.addView(imageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    progressBar.setVisibility(View.GONE);
                    toggleTableButton.setVisibility(View.VISIBLE);
                });

                String dataUrl = "https://cat-tester-api.azurewebsites.net/request-session-records?SessionID=" + sessionId;
                HttpURLConnection dataConnection = (HttpURLConnection) new URL(dataUrl).openConnection();
                dataConnection.setRequestMethod("GET");

                int dataResponseCode = dataConnection.getResponseCode();
                if (dataResponseCode >= 400) {
                    Log.e("SessionDetails", "Failed to fetch raw records");
                    return;
                }

                InputStream dataInputStream = dataConnection.getInputStream();
                StringBuilder dataResponse = new StringBuilder();
                while ((ch = dataInputStream.read()) != -1) {
                    dataResponse.append((char) ch);
                }

                JSONObject dataJson = new JSONObject(dataResponse.toString());
                JSONArray records = dataJson.getJSONArray("records");

                requireActivity().runOnUiThread(() -> {
                    addTableHeader();
                    for (int i = 0; i < records.length(); i++) {
                        try {
                            JSONObject record = records.getJSONObject(i);
                            String id = String.valueOf(record.getInt("RecordID"));
                            String dist = String.valueOf(record.getDouble("Distance"));
                            String temp = String.valueOf(record.getDouble("Temperature"));
                            String press = String.valueOf(record.getDouble("Pressure"));
                            String isoTimestamp = record.getString("Timestamp");
                            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                            Date parsedDate = inputFormat.parse(isoTimestamp);
                            String formattedTime = outputFormat.format(parsedDate);
                            addRawRow(id, dist, press, temp, formattedTime);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    TextView errorText = new TextView(requireContext());
                    errorText.setText("No valid records found for this session.");
                    errorText.setPadding(32, 164, 32, 32);
                    errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    errorText.setTextSize(16);
                    graphContainer.addView(errorText);
                });
            }
        }).start();
    }


    private void addTableHeader() {
        TableRow headerRow = new TableRow(requireContext());
        addCellToRow(headerRow, "Record ID");
        addCellToRow(headerRow, "Distance");
        addCellToRow(headerRow, "Pressure");
        addCellToRow(headerRow, "Temp");
        addCellToRow(headerRow, "Timestamp");
        tableLayout.addView(headerRow);
    }

    private void addRawRow(String id, String dist, String press, String temp, String time) {
        TableRow row = new TableRow(requireContext());
        addCellToRow(row, id);
        addCellToRow(row, dist);
        addCellToRow(row, press);
        addCellToRow(row, temp);
        addCellToRow(row, time);
        tableLayout.addView(row);
    }

    private void addCellToRow(TableRow row, String text) {
        TextView cell = new TextView(requireContext());
        cell.setText(text);
        //cell.setTextColor(ThemeManager.getTextColor(requireContext()));
        cell.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        cell.setPadding(16, 8, 16, 8);
        row.addView(cell);
    }
}

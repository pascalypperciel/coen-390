package com.example.minicapapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class SessionDetailsFragment extends Fragment {

    private long sessionId;
    private LinearLayout graphContainer;
    private TableLayout tableLayout;
    private ProgressBar progressBar;
    private Button toggleTableButton;

    private boolean isTableVisible = false;

    public SessionDetailsFragment() {
        // Required empty public constructor
    }

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

        graphContainer = view.findViewById(R.id.graphContainer);
        tableLayout = view.findViewById(R.id.rawDataTable);
        progressBar = view.findViewById(R.id.graphLoadingSpinner);
        toggleTableButton = view.findViewById(R.id.toggleTableButton);

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
                String apiUrl = "https://cat-tester-api.azurewebsites.net/build-graphs?SessionID=" + sessionId;
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
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
                        errorText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        errorText.setPadding(32, 64, 32, 32);
                        errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        errorText.setTextSize(16);
                        graphContainer.addView(errorText);
                    });
                    return;
                }

                JSONArray graphArray = jsonResponse.getJSONArray("Graph");

                requireActivity().runOnUiThread(() -> {
                    for (int i = 0; i < graphArray.length(); i++) {
                        try {
                            String base64 = graphArray.getString(i);
                            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ImageView imageView = new ImageView(requireContext());
                            imageView.setImageBitmap(bitmap);
                            imageView.setAdjustViewBounds(true);
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            imageView.setPadding(0, 32, 0, 32);
                            graphContainer.addView(imageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    progressBar.setVisibility(View.GONE);
                    toggleTableButton.setVisibility(View.VISIBLE);
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
        cell.setPadding(16, 8, 16, 8);
        row.addView(cell);
    }
}

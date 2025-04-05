package com.example.minicapapp;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionDetailsFragment extends Fragment {

    private long sessionId;
    private String sessionName;
    private Date sessionTimestamp;
    private float initialLength;
    private float initialArea;
    private float yieldStrain;
    private float yieldStress;

    private TextView textViewSessionId;
    private TextView textViewSessionName;
    private TextView textViewSessionTimestamp;
    private TextView textViewInitialLength;
    private TextView textViewInitialArea;
    private TextView textViewYieldStrain;
    private TextView textViewYieldStress;

    public SessionDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            sessionId = getArguments().getLong("session_id", -1);
            sessionName = getArguments().getString("session_name", "N/A");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                sessionTimestamp = getArguments().getSerializable("session_timestamp", Date.class);
            } else {
                sessionTimestamp = (Date) getArguments().getSerializable("session_timestamp");
            }

            initialLength = getArguments().getFloat("initial_length", Float.NaN);
            initialArea = getArguments().getFloat("initial_area", Float.NaN);
            yieldStrain = getArguments().getFloat("yield_strain", Float.NaN);
            yieldStress = getArguments().getFloat("yield_stress", Float.NaN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session_details, container, false);

        textViewSessionId = view.findViewById(R.id.textViewSessionId);
        textViewSessionName = view.findViewById(R.id.textViewSessionName);
        textViewSessionTimestamp = view.findViewById(R.id.textViewSessionTimestamp);
        textViewInitialLength = view.findViewById(R.id.textViewInitialLength);
        textViewInitialArea = view.findViewById(R.id.textViewInitialArea);
        textViewYieldStrain = view.findViewById(R.id.textViewYieldStrain);
        textViewYieldStress = view.findViewById(R.id.textViewYieldStress);

        DecimalFormat df = new DecimalFormat("#.###");
        textViewSessionId.setText("Session ID: " + sessionId);
        textViewSessionName.setText("Session Name: " + sessionName);
        textViewSessionTimestamp.setText("Timestamp: " +
                (sessionTimestamp != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(sessionTimestamp) : "Unknown"));

        textViewInitialLength.setText("Initial Length: " + df.format(initialLength) + " mm");
        textViewInitialArea.setText("Initial Area: " + df.format(initialArea) + " mm²");

        textViewYieldStrain.setText("Yield Strain: " + (!Float.isNaN(yieldStrain) ? df.format(yieldStrain) : "N/A"));
        textViewYieldStress.setText("Yield Stress: " + (!Float.isNaN(yieldStress) ? df.format(yieldStress) : "N/A"));

        return view;
    }
}

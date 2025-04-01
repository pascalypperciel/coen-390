package com.example.minicapapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    // The UI elements present in the Settings Fragment
    protected ImageButton imageButtonHelpSettings;
    protected TextView textViewTemp;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Define and set the behaviour of the UI elements in ths fragment
        imageButtonHelpSettings = view.findViewById(R.id.imageButtonHelpSettings);
        imageButtonHelpSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpFragment helpFragment = new HelpFragment();
                helpFragment.show(getActivity().getSupportFragmentManager(), "HelpDialogue");
            }
        });

        textViewTemp = view.findViewById(R.id.textViewSettingsTemp);

        return view;
    }
}
package com.example.minicapapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


public class RecordedDataFragment extends Fragment {
    // The UI elements present in the Recorded Data Fragment
    protected ImageButton imageButtonHelpRecordedData;
    protected TextView textViewTemp;

    public RecordedDataFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recorded_data, container, false);

        // Define and set the behaviour of the UI elements in ths fragment
        imageButtonHelpRecordedData = view.findViewById(R.id.imageButtonHelpRecordedData);
        imageButtonHelpRecordedData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpFragment helpFragment = new HelpFragment();
                helpFragment.show(getActivity().getSupportFragmentManager(), "HelpDialogue");
            }
        });

        textViewTemp = view.findViewById(R.id.textViewRecordedDataTemp);

        return view;
    }
}
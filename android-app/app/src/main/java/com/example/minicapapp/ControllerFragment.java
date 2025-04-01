package com.example.minicapapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class ControllerFragment extends Fragment {
    // Internal Attributes
    public boolean isConnected = false; // A global variable that is kept to track if the Bluetooth connection is persistent.

    // The UI elements present in the Controller Fragment
    protected ImageButton imageButtonHelpController;
    protected TextView textViewTemp;

    public ControllerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_controller, container, false);

        // Define and set the behaviour of the UI elements in ths fragment
        imageButtonHelpController = view.findViewById(R.id.imageButtonHelpController);
        imageButtonHelpController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpFragment helpFragment = new HelpFragment();
                helpFragment.show(getActivity().getSupportFragmentManager(), "HelpDialogue");
            }
        });

        textViewTemp = view.findViewById(R.id.textViewControllerTemp);

        return view;
    }
}
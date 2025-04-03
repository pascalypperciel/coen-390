package com.example.minicapapp;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HelpFragment extends DialogFragment {

    private static final String ARG_PAGE_KEY = "page_key";
    protected Button buttonCloseDialogueFragment;
    protected View coverUp;
    protected RelativeLayout rlTextContainer;
    protected ScrollView textbox;

    public HelpFragment() {
        // Required empty public constructor
    }
    // Factory method to create a new instance of HelpFrag with a page key
    public static HelpFragment newInstance(String pageKey) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAGE_KEY, pageKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help_dialogue, container, false);

        coverUp =view.findViewById(R.id.viewCoverUp);
        coverUp.setClickable(true);
        rlTextContainer = view.findViewById(R.id.textcontainer);
        textbox = view.findViewById(R.id.textbox);

        textbox.smoothScrollTo(0, rlTextContainer.getBottom());

        TextView helpTextView = view.findViewById(R.id.textView);

        // Retrieve the page key from the arguments
        String pageKey = getArguments() != null ? getArguments().getString(ARG_PAGE_KEY) : "";

        // Set the help text dynamically based on the page key
        switch (pageKey) {
            case "Controller":
                helpTextView.setText("This page contains controls for the CAT Strength Tester. Use the buttons to control the motor and start/stop recording data.");
                break;
            case "RecordedData":
                helpTextView.setText("This page displays all recorded data from the CAT Strength Tester. Use the settings to customize the display.");
                break;
            case "Settings":
                helpTextView.setText("This page allows you to configure the app settings, including data display preferences.");
                break;
            default:
                helpTextView.setText("This is the help page. Use this section to understand the functionality of the app.");
                break;
        }


        buttonCloseDialogueFragment =view.findViewById(R.id.buttonClose);
        buttonCloseDialogueFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }
}

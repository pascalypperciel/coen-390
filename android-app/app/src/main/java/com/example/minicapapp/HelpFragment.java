package com.example.minicapapp;

import android.content.Context;
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
    private Context context;
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

        // Retrieve the current theme of the mobile application
        context = requireContext();
        int backgroundColour = ThemeManager.getBackgroundColor(context);
        int buttonColour = ThemeManager.getButtonColor(context);
        int textColour = ThemeManager.getTextColor(context);

        // Set the colour of the view
        view.setBackgroundColor(backgroundColour);

        coverUp =view.findViewById(R.id.viewCoverUp);
        coverUp.setClickable(true);
        rlTextContainer = view.findViewById(R.id.textcontainer);
        textbox = view.findViewById(R.id.textbox);

        textbox.smoothScrollTo(0, rlTextContainer.getBottom());

        // Set the colour of the textbox
        textbox.setBackgroundColor(backgroundColour);

        // Set the colour of the text
        TextView helpTextView = view.findViewById(R.id.textView);
        helpTextView.setTextColor(textColour);

        // Retrieve the page key from the arguments
        String pageKey = getArguments() != null ? getArguments().getString(ARG_PAGE_KEY) : "";

        // Set the help text dynamically based on the page key
        switch (pageKey) {
            case "Controller":
                helpTextView.setText(R.string.controller_message);
                break;
            case "RecordedData":
                helpTextView.setText(R.string.recorded_data_message);
                break;
            case "Settings":
                helpTextView.setText(R.string.settings_message);
                break;
            case "MaterialsInformation":
                helpTextView.setText(R.string.materials_information_message);
                break;
            default:
                helpTextView.setText(R.string.default_message);
                break;
        }


        buttonCloseDialogueFragment =view.findViewById(R.id.buttonClose);
        buttonCloseDialogueFragment.setBackgroundColor(buttonColour);
        buttonCloseDialogueFragment.setTextColor(textColour);
        buttonCloseDialogueFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }
}

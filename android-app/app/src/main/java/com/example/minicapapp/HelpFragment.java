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
                helpTextView.setText("This page contains controls for the CAT Strength Tester.\n\n" +
                        "First, use the button on the top left to connect your device via Bluetooth.\n\n" +
                        "Then, populate the fields with your test's name and the dimensions of your test sample. " +
                        "Make sure you remember the test name; You'll need it later!\n\n" +
                        "Once the test is started, you can let the device do its thing. " +
                        "In the event of an error, use the buttons to control the motor or to stop the test manually.\n\n" +
                        "WARNING: This device can apply a great deal of force onto the test sample. " +
                        "Please keep away from the device while a test is in progress.\n\n\n" +
                        "Thank you!");
                break;
            case "RecordedData":
                helpTextView.setText("This page displays all recorded data from the CAT Strength Tester. \n\n" +
                        "To find your test, select the session name from the list on this page. " +
                        "Once selected, you will have access to all the data from the test " +
                        "as well as graphs demonstrating the testing process.\n\n" +
                        "We have prepared five graphs to describe the test outcome:\n\n" +
                        "Engineering Stress/Strain Curve: Using the industry-standard offset test, the reaction " +
                        "of the test sample under load. We find the yield point, which is where the material begins to " +
                        "permanently deform.\n\n" +
                        "True Stress/Strain Curve: This is instantaneous change instead of change relative to initial " +
                        "parameters. Measuring the same thing two different ways.\n\n" +
                        "Displacement vs. Force: Shows the amount that the sample is 'squished' as it undergoes load.\n\n" +
                        "Load vs. Time: Shows the rate of motor loading onto the sample. " +
                        "Should be linear or nearly linear until yield.\n\n" +
                        "Displacement vs Time: The rate at which the object is compressed. Too fast? Let us know!\n\n\n" +
                        "Thank you!");
                break;
            case "Settings":
                helpTextView.setText("This page allows you to configure the app settings.\n\n" +
                        "The first tab allows you to connect your device to the CAT via Bluetooth." +
                        "Simply select 'Scan Devices' and wait for the CAT to appear. " +
                        "Once connected, navigate to the Controller page using the icon on the bottom left to begin testing.\n\n" +
                        "The second tab allows you to change the color scheme of the app. " +
                        "Try them out! Our favourite alts are Dark Theme and Forest Tech Theme.\n\n" +
                        "When you want the test to stop itself, you can try the Threshold Settings. " +
                        "When the CAT reads a value that exceeds a threshold, it will automatically cease functioning.\n\n\n" +
                        "Thank you!");
                break;
            case "MaterialsInformation":
                helpTextView.setText("This is our explainer for the physical theory behind the CAT. " +
                        "It's a little much for us to fit on this dialogue box, so we made a page for it here.\n\n" +
                        "Not interested? No problem! Nothing in this section is necessary to know in order to use the device. " +
                        "We just thought it was important that this information should be accessible for those who wanted it.\n\n" +
                        "You can navigate directly to tests by selecting the Controller icon on the bottom right, " +
                        "or connect to Bluetooth on the Sessings page.\n\n\n" +
                        "Thank you!");
                break;
            default:
                helpTextView.setText("How did you get here? Why are you seeing this? That's not supposed to happen >:(");
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

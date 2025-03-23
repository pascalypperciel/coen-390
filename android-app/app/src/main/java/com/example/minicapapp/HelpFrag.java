package com.example.minicapapp;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class HelpFrag extends DialogFragment {

    protected Button buttonCloseDialogueFragment;
    protected View coverUp;
    protected RelativeLayout rlTextContainer;
    protected ScrollView textbox;

    public HelpFrag() {
        // Required empty public constructor
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
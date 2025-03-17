package com.example.minicapapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class HelpFrag extends Fragment {

    protected Button btnCloseFrag;
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
        View rootView=inflater.inflate(R.layout.fragment_helpfrag, container, false);

        btnCloseFrag =rootView.findViewById(R.id.closeb);
        coverUp =rootView.findViewById(R.id.coverup);
        coverUp.setClickable(true);
        textbox = rootView.findViewById(R.id.textbox);
        rlTextContainer = rootView.findViewById(R.id.textcontainer);

        textbox.smoothScrollTo(0, rlTextContainer.getBottom());

        btnCloseFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(HelpFrag.this).commit();
            }
        });
        return rootView;
    }
}
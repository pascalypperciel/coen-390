package com.example.minicapapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class infofrag extends Fragment {
    protected Button closefrag;
    protected View coverup;

    protected RelativeLayout textcontainer;
    protected ScrollView textbox;

    protected TextView textwithlink;

    public infofrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_infofrag, container, false);

        closefrag=rootView.findViewById(R.id.closeb);
        coverup=rootView.findViewById(R.id.coverup);
        coverup.setClickable(true);

        textbox = rootView.findViewById(R.id.textbox);
        textcontainer = rootView.findViewById(R.id.textcontainer);

        textwithlink=rootView.findViewById(R.id.textwithlink);
        //textwithlink.setMovementMethod(LinkMovementMethod.getInstance());

        textbox.smoothScrollTo(0,textcontainer.getBottom());

        closefrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(infofrag.this).commit();
            }
        });
        return rootView;
    }
}
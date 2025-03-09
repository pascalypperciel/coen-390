package com.example.minicapapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class InfoFrag extends Fragment {
    protected Button closeFrag;
    protected View coverup;

    protected RelativeLayout textContainer;
    protected ScrollView textbox;

    protected TextView textWithLink;

    public InfoFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_infofrag, container, false);

        closeFrag =rootView.findViewById(R.id.closeb);
        coverup=rootView.findViewById(R.id.coverup);
        coverup.setClickable(true);

        textbox = rootView.findViewById(R.id.textbox);
        textContainer = rootView.findViewById(R.id.textcontainer);

        textWithLink =rootView.findViewById(R.id.textwithlink);
        //textwithlink.setMovementMethod(LinkMovementMethod.getInstance());

        textbox.smoothScrollTo(0, textContainer.getBottom());

        closeFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(InfoFrag.this).commit();
            }
        });
        return rootView;
    }
}
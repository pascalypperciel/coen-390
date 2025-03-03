package com.example.minicapapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class infofrag extends Fragment {
    protected Button closefrag;
    protected View coverup;

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

        closefrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(infofrag.this).commit();
            }
        });
        return rootView;
    }
}
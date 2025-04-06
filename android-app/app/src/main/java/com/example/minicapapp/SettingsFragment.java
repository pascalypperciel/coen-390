package com.example.minicapapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        LinearLayout bluetoothSettingsRow = view.findViewById(R.id.bluetoothSettingsRow);
        bluetoothSettingsRow.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutActivityContent, new BluetoothFragment())
                .addToBackStack(null)
                .commit());

        LinearLayout themesSettingsRow = view.findViewById(R.id.themesSettingsRow);
        themesSettingsRow.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutActivityContent, new ThemesFragment())
                .addToBackStack(null)
                .commit());

        return view;
    }
}

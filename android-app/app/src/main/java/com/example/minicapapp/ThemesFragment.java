package com.example.minicapapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ThemesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_themes, container, false);

        Button btnDefault = view.findViewById(R.id.themeDefaultButton);
        Button btnDark = view.findViewById(R.id.themeDarkButton);
        Button btnSunset = view.findViewById(R.id.themeSunsetButton);

        btnDefault.setOnClickListener(v -> applyTheme(ThemeManager.Theme.DEFAULT));
        btnDark.setOnClickListener(v -> applyTheme(ThemeManager.Theme.DARK));
        btnSunset.setOnClickListener(v -> applyTheme(ThemeManager.Theme.SUNSET));

        return view;
    }

    private void applyTheme(ThemeManager.Theme theme) {
        ThemeManager.setTheme(requireContext(), theme);
        requireActivity().recreate();
    }
}

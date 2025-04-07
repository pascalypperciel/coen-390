package com.example.minicapapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ThemesFragment extends Fragment {

    @Nullable //Means explicitly can be null
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_themes, container, false);

        int backgroundColor = ThemeManager.getBackgroundColor(requireContext());
        int textColor = ThemeManager.getTextColor(requireContext());
        int buttonColor = ThemeManager.getButtonColor(requireContext());

        ScrollView scrollView = (ScrollView) view; //ScrollView in case we have more themes than fit on screen. very ambitious
        scrollView.setBackgroundColor(backgroundColor);

        TextView title = view.findViewById(R.id.themeTitleTextView);
        title.setTextColor(textColor);

        //Selectable buttons to change theme
        Button btnDefault = view.findViewById(R.id.themeDefaultButton);
        Button btnDark = view.findViewById(R.id.themeDarkButton);
        Button btnModern = view.findViewById(R.id.themeModernButton);
        Button btnModernCoastal = view.findViewById(R.id.themeModernCoastalButton);
        Button btnSunriseBlush = view.findViewById(R.id.themeSunriseBlushButton);
        Button btnForestTech = view.findViewById(R.id.themeForestTechButton);

        styleButton(btnDefault, buttonColor, textColor);
        styleButton(btnDark, buttonColor, textColor);
        styleButton(btnModern, buttonColor, textColor);
        styleButton(btnModernCoastal, buttonColor, textColor);
        styleButton(btnSunriseBlush, buttonColor, textColor);
        styleButton(btnForestTech, buttonColor, textColor);

        //On click, set it to be the appt theme
        btnDefault.setOnClickListener(v -> applyTheme(ThemeManager.Theme.DEFAULT));
        btnDark.setOnClickListener(v -> applyTheme(ThemeManager.Theme.DARK));
        btnModern.setOnClickListener(v -> applyTheme(ThemeManager.Theme.MODERN));
        btnModernCoastal.setOnClickListener(v -> applyTheme(ThemeManager.Theme.MODERN_COASTAL));
        btnSunriseBlush.setOnClickListener(v -> applyTheme(ThemeManager.Theme.SUNRISE_BLUSH));
        btnForestTech.setOnClickListener(v -> applyTheme(ThemeManager.Theme.FOREST_TECH));

        return view;
    }

    private void styleButton(Button button, int backgroundColor, int textColor) {
        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setTextColor(textColor);
    }

    private void applyTheme(ThemeManager.Theme theme) {
        //ThemeManager holds the actual theme change logic, where loading another fragment is in the correct theme
        ThemeManager.setTheme(requireContext(), theme);

        //recreate() updates the UI in real time. It returns you to the the Controller fragment
        requireActivity().recreate();
    }
}

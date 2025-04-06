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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_themes, container, false);

        int backgroundColor = ThemeManager.getBackgroundColor(requireContext());
        int textColor = ThemeManager.getTextColor(requireContext());
        int buttonColor = ThemeManager.getButtonColor(requireContext());

        ScrollView scrollView = (ScrollView) view;
        scrollView.setBackgroundColor(backgroundColor);

        TextView title = view.findViewById(R.id.themeTitleTextView);
        title.setTextColor(textColor);

        Button btnDefault = view.findViewById(R.id.themeDefaultButton);
        Button btnDark = view.findViewById(R.id.themeDarkButton);
        Button btnSunset = view.findViewById(R.id.themeSunsetButton);

        styleButton(btnDefault, buttonColor, textColor);
        styleButton(btnDark, buttonColor, textColor);
        styleButton(btnSunset, buttonColor, textColor);

        btnDefault.setOnClickListener(v -> applyTheme(ThemeManager.Theme.DEFAULT));
        btnDark.setOnClickListener(v -> applyTheme(ThemeManager.Theme.DARK));
        btnSunset.setOnClickListener(v -> applyTheme(ThemeManager.Theme.SUNSET));

        return view;
    }

    private void styleButton(Button button, int backgroundColor, int textColor) {
        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setTextColor(textColor);
    }

    private void applyTheme(ThemeManager.Theme theme) {
        ThemeManager.setTheme(requireContext(), theme);
        requireActivity().recreate();
    }
}

package com.example.minicapapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        int textColor = ThemeManager.getTextColor(requireContext());
        int buttonColor = ThemeManager.getButtonColor(requireContext());
        int backgroundColor = ThemeManager.getBackgroundColor(requireContext());

        view.setBackgroundColor(backgroundColor);

        TextView settingsTitle = view.findViewById(R.id.settingsTitle); //From .xml file
        settingsTitle.setTextColor(textColor);

        CardView settingsCard = view.findViewById(R.id.settingsCard); //Also from .xml
        settingsCard.setCardBackgroundColor(backgroundColor);

        View cardBorderWrapper = settingsCard.getChildAt(0); //This is the little background around the options bar
        cardBorderWrapper.setBackgroundTintList(ColorStateList.valueOf(buttonColor));

        LinearLayout bluetoothSettingsRow = view.findViewById(R.id.bluetoothSettingsRow);
        bluetoothSettingsRow.setOnClickListener(v -> requireActivity().getSupportFragmentManager() //Nav logic to bluetooth page
                .beginTransaction()
                .replace(R.id.frameLayoutActivityContent, new BluetoothFragment())
                .addToBackStack(null)
                .commit());
        tintRow(bluetoothSettingsRow, textColor);

        LinearLayout themesSettingsRow = view.findViewById(R.id.themesSettingsRow); //Nav to theme settings
        themesSettingsRow.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutActivityContent, new ThemesFragment())
                .addToBackStack(null)
                .commit());
        tintRow(themesSettingsRow, textColor);

        return view;
    }

    private void tintRow(LinearLayout rowLayout, int textColor) { //Changes the background colour of the row
        for (int i = 0; i < rowLayout.getChildCount(); i++) {
            View child = rowLayout.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(textColor); //This one does text
            } else if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(textColor); //This one does background image. Hope this helps lmaoo
            }
        }
    }
}

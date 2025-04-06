package com.example.minicapapp;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class ThresholdsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thresholds, container, false);

        int textColor = ThemeManager.getTextColor(requireContext());
        int buttonColor = ThemeManager.getButtonColor(requireContext());
        int backgroundColor = ThemeManager.getBackgroundColor(requireContext());

        MaterialCardView card = view.findViewById(R.id.cardThresholds);
        card.setCardBackgroundColor(backgroundColor);
        card.setStrokeColor(buttonColor);

        EditText inputMaxPressure = view.findViewById(R.id.editTextMaxPressure);
        EditText inputMaxDistance = view.findViewById(R.id.editTextMaxDistance);
        EditText inputMinDistance = view.findViewById(R.id.editTextMinDistance);
        EditText inputYoungModulus = view.findViewById(R.id.editTextYoungModulus);

        inputMaxPressure.setText(String.valueOf(ThresholdsManager.getMaxPressure(requireContext())));
        inputMaxDistance.setText(String.valueOf(ThresholdsManager.getMaxDistance(requireContext())));
        inputMinDistance.setText(String.valueOf(ThresholdsManager.getMinDistance(requireContext())));
        inputYoungModulus.setText(String.valueOf(ThresholdsManager.getYoungModulus(requireContext())));

        inputMaxPressure.setTextColor(textColor);
        inputMaxDistance.setTextColor(textColor);
        inputMinDistance.setTextColor(textColor);
        inputYoungModulus.setTextColor(textColor);

        inputMaxPressure.setHintTextColor(textColor);
        inputMaxDistance.setHintTextColor(textColor);
        inputMinDistance.setHintTextColor(textColor);
        inputYoungModulus.setHintTextColor(textColor);

        inputMaxPressure.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputMaxDistance.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputMinDistance.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputYoungModulus.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        Button buttonSave = view.findViewById(R.id.buttonSaveThresholds);
        buttonSave.setBackgroundColor(buttonColor);
        buttonSave.setTextColor(textColor);
        buttonSave.setOnClickListener(v -> {
            try {
                float maxPressure = Float.parseFloat(inputMaxPressure.getText().toString());
                float maxDistance = Float.parseFloat(inputMaxDistance.getText().toString());
                float minDistance = Float.parseFloat(inputMinDistance.getText().toString());
                double youngModulus = Double.parseDouble(inputYoungModulus.getText().toString());

                ThresholdsManager.setThresholds(requireContext(), maxPressure, maxDistance, minDistance, youngModulus);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Thresholds successfully set", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (NumberFormatException ignored) {
                //ignore
            }
        });

        view.setBackgroundColor(backgroundColor);

        return view;
    }
}

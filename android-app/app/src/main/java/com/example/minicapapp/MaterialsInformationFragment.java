package com.example.minicapapp;

import com.google.android.material.card.MaterialCardView;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

public class MaterialsInformationFragment extends Fragment {
    // The UI elements present in the Materials Information Fragment
    protected ImageView imageViewLogo;
    protected ImageButton imageButtonHelpMaterialsInformation;
    protected WebView webViewMaterialsInformationPage;


    public MaterialsInformationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_materials_information, container, false);

        // Define and set the behaviour of the UI elements in ths fragment
        // Logo
        imageViewLogo = view.findViewById(R.id.imageViewLogo);

        // Help Button
        imageButtonHelpMaterialsInformation = view.findViewById(R.id.imageButtonHelpMaterialsInformation);
        imageButtonHelpMaterialsInformation.setOnClickListener(v -> {
            HelpFragment helpFragment = new HelpFragment();
            helpFragment.show(requireActivity().getSupportFragmentManager(), "HelpDialogue");
        });

        // Materials Information Page Web View
        webViewMaterialsInformationPage = view.findViewById(R.id.webViewMaterialsInformationPage);
        webViewMaterialsInformationPage.loadUrl("file:///android_asset/info-page.html");

        int backgroundColor = ThemeManager.getBackgroundColor(requireContext());
        int textColor = ThemeManager.getTextColor(requireContext());

        view.setBackgroundColor(backgroundColor);
        imageButtonHelpMaterialsInformation.setColorFilter(textColor);

        MaterialCardView card = view.findViewById(R.id.webViewCard);
        card.setStrokeColor(ThemeManager.getButtonColor(requireContext()));

        return view;
    }
}

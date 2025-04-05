package com.example.minicapapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RecordedDataRecyclerViewAdapter extends RecyclerView.Adapter<RecordedDataRecyclerViewAdapter.ViewHolder> {
    // Variables for the storage of objects to be displayed in the RecyclerView.
    private FragmentActivity activity;
    private List<RecordedDataItem> localRecordedDataList; // A list of the recorded data objects.

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // UI elements present within the List Item Resource
        private TextView textViewRecordedDataListItemName;
        private TextView textViewRecordedDataLisItemTimestamp;
        private ImageButton  imageButtonMoreDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRecordedDataListItemName = itemView.findViewById(R.id.textViewListItemName);
            textViewRecordedDataLisItemTimestamp = itemView.findViewById(R.id.textViewListItemTimestamp);
            imageButtonMoreDetails = itemView.findViewById(R.id.imageButtonMoreDetails);
        }

        public TextView getTextViewRecordedDataListItemName() {
            return textViewRecordedDataListItemName;
        }

        public TextView getTextViewRecordedDataLisItemTimestamp() {
            return textViewRecordedDataLisItemTimestamp;
        }

        public ImageButton getImageButtonMoreDetails() {
            return imageButtonMoreDetails;
        }
    }

    public RecordedDataRecyclerViewAdapter(FragmentActivity activity, List<RecordedDataItem> localRecordedDataList) {
        this.activity = activity;
        this.localRecordedDataList = localRecordedDataList;
    }

    // This method will allow us to retrieve or "inflate" the Recorded Data List Item resource.
    @NonNull
    @Override
    public RecordedDataRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    // This method will bind the Recorded Data List item to its holder.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Retrieve the Data Item at each position in the Recycler View.
        RecordedDataItem dataItem = localRecordedDataList.get(position);

        // Set the correct information for each Recorded Data Item.
        holder.getTextViewRecordedDataListItemName().setText(dataItem.getSessionName());
        holder.getTextViewRecordedDataLisItemTimestamp().setText(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(dataItem.getSessionTimestamp()));

        holder.itemView.setOnClickListener(v -> {
            Log.d("RecyclerView", "Item clicked: " + dataItem.getSessionName());

            SessionDetailsFragment sessionDetailsFragment = new SessionDetailsFragment();
            Bundle args = new Bundle();
            args.putLong("session_id", dataItem.getSessionID());
            args.putString("session_name", dataItem.getSessionName());
            args.putSerializable("session_timestamp", dataItem.getSessionTimestamp());
            args.putFloat("initial_length", dataItem.getInitialLength());
            args.putFloat("initial_area", dataItem.getInitialArea());
            args.putFloat("yield_strain", dataItem.getYieldStrain());
            args.putFloat("yield_stress", dataItem.getYieldStress());
            sessionDetailsFragment.setArguments(args);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayoutActivityContent, sessionDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Go to the Data Item Activity if the "More Details" button is pressed.
        holder.getImageButtonMoreDetails().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Change the mechanic of this button to pass a bundle to a new fragment.
                //Intent intent  = new Intent(context, DataItemActivity.class); // Explicit Intent.
                //intent.putExtra("data_item_session_id", dataItem.getSessionID());
                //context.startActivity(intent);
            }
        });
    }

    // This method will determine the length of the list that will be shown.
    @Override
    public int getItemCount() {
        return localRecordedDataList.size();
    }

    // This method will sort the list of Profiles alphabetically or numerically, depending on the display mode.
    public void sortData(String filteringOption) {
        if(filteringOption.equals("1. Chronologically (Newest to Oldest)")) { // Sort the data chronologically, by Timestamp, from Newest to Oldest.
            Collections.sort(localRecordedDataList, new Comparator<RecordedDataItem>() {
                @Override
                public int compare(RecordedDataItem d1, RecordedDataItem d2) {
                    return d2.getSessionTimestamp().compareTo(d1.getSessionTimestamp());
                }
            });
        } else if (filteringOption.equals("2. Chronologically (Oldest to Newest)")) { // Sort the data chronologically, by Timestamp, from Oldest to Newest.
            Collections.sort(localRecordedDataList, new Comparator<RecordedDataItem>() {
                @Override
                public int compare(RecordedDataItem d1, RecordedDataItem d2) {
                    return d1.getSessionTimestamp().compareTo(d2.getSessionTimestamp());
                }
            });
        } else if(filteringOption.equals("3. Alphabetically (A to Z)")) { // Sort the data alphabetically, by Name, from A to Z.
            Collections.sort(localRecordedDataList, new Comparator<RecordedDataItem>() {
                @Override
                public int compare(RecordedDataItem d1, RecordedDataItem d2) {
                    return d1.getSessionName().compareToIgnoreCase(d2.getSessionName());
                }
            });
        } else if(filteringOption.equals("4. Alphabetically (Z to A)")) { // Sort the data alphabetically, by name, from Z to A.
            Collections.sort(localRecordedDataList, new Comparator<RecordedDataItem>() {
                @Override
                public int compare(RecordedDataItem d1, RecordedDataItem d2) {
                    return d2.getSessionName().compareToIgnoreCase(d1.getSessionName());
                }
            });
        }
        notifyDataSetChanged();
    }
}
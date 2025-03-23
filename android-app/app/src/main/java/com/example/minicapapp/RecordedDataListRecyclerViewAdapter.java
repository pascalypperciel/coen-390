package com.example.minicapapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordedDataListRecyclerViewAdapter extends RecyclerView.Adapter<RecordedDataListRecyclerViewAdapter.ViewHolder> {
    // Variables for the storage of objects to be displayed in the RecyclerView.
    private Context context; // The context of the activity housing the RecyclerView.
    private List<RecordedDataItem> localRecordedDataList; // A list of the recorded data objects.

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // UI elements present within the List Item Resource
        private TextView textViewRecordedDataListItemName;
        private TextView textViewRecordedDataLisItemTimestamp;
        private TextView textViewRecordedDataListItemTestType;
        private TextView textViewRecordedDataListItemMaterialType;
        private Button  buttonMoreDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRecordedDataListItemName = itemView.findViewById(R.id.textViewListItemName);
            textViewRecordedDataLisItemTimestamp = itemView.findViewById(R.id.textViewListItemTimestamp);
            textViewRecordedDataListItemTestType = itemView.findViewById(R.id.textViewListItemTestType);
            textViewRecordedDataListItemMaterialType = itemView.findViewById(R.id.textViewMaterialType);
            buttonMoreDetails = itemView.findViewById(R.id.buttonMoreDetails);
        }

        public TextView getTextViewRecordedDataListItemName() {
            return textViewRecordedDataListItemName;
        }

        public TextView getTextViewRecordedDataLisItemTimestamp() {
            return textViewRecordedDataLisItemTimestamp;
        }

        public TextView getTextViewRecordedDataListItemTestType() {
            return textViewRecordedDataListItemTestType;
        }

        public TextView getTextViewRecordedDataListItemMaterialType() {
            return textViewRecordedDataListItemMaterialType;
        }

        public Button getButtonMoreDetails() {
            return buttonMoreDetails;
        }
    }

    public RecordedDataListRecyclerViewAdapter(Context context, List<RecordedDataItem> localRecordedDataList) {
        this.context = context;
        this.localRecordedDataList = localRecordedDataList;
    }

    // This method will allow us to retrieve or "inflate" the Recorded Data List Item resource.
    @NonNull
    @Override
    public RecordedDataListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    // This method will bind the Recorded Data List item to its holder.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Retrieve the Data Item at each position in the Recycler View.
        RecordedDataItem dataItem = localRecordedDataList.get(position);

        // Set the correct information for each Recorded Data Item.
        holder.getTextViewRecordedDataListItemName().setText(dataItem.getTestName());
        holder.getTextViewRecordedDataLisItemTimestamp().setText(dataItem.getTimestamp());
        holder.getTextViewRecordedDataListItemTestType().setText(dataItem.getTestType());
        holder.getTextViewRecordedDataListItemMaterialType().setText(dataItem.getMaterialType());
        holder.getButtonMoreDetails().setText("More Details >");

        // Go to the Data Item Activity if the "More Details" button is pressed.
        holder.buttonMoreDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(context, DataItemActivity.class); // Explicit Intent.
                intent.putExtra("data_item_session_id", dataItem.getId());
                context.startActivity(intent);
            }
        });
    }

    // This method will determine the length of the list that will be shown.
    @Override
    public int getItemCount() {
        return localRecordedDataList.size();
    }

    // This method will refresh the recycler view when a new profile is added.
    public void updateList(List<RecordedDataItem> updatedProfiles) {
        this.localRecordedDataList = updatedProfiles;
        notifyDataSetChanged();
    }

    // TODO: Create methods for sorting.
}
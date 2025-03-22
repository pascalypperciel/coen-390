package com.example.minicapapp;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordedDataListRecyclerViewAdapter {
    // Variables for the storage of objects to be displayed in the RecyclerView.
    private Context context; // The context of the activity housing the RecyclerView.
    // TODO: Make the list take the correct object type.
    private List<Integer> localRecordedDataList; // A list of the recorded data objects.

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // UI elements present within the List Item Resource
        // TODO: Design the list item

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find all of the UI elements in the list item by their ID
        }

        // TODO: Make getters for all of the items in the list item.
    }

    public RecordedDataListRecyclerViewAdapter(Context context, List<Integer> localRecordedDataList) {
        this.context = context;
        this.localRecordedDataList = localRecordedDataList;
    }

    // TODO: Finish transferring recycler view code.
}
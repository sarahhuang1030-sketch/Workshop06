package com.example.workshop06.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.QuoteResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {

    private final List<QuoteResponse> originalList = new ArrayList<>();
    private final List<QuoteResponse> displayedList = new ArrayList<>();

    public void updateData(List<QuoteResponse> newItems) {
        originalList.clear();
        displayedList.clear();

        if (newItems != null) {
            originalList.addAll(newItems);
            displayedList.addAll(newItems);
        }

        notifyDataSetChanged();
    }

    public void filter(String query, String status) {

        List<QuoteResponse> filtered = new ArrayList<>();

        for (QuoteResponse item : originalList) {

            Log.d("QUOTE_STATUS", "DB status = [" + item.getStatus() + "]");
            Log.d("QUOTE_STATUS", "filter status = [" + status + "]");

            boolean matchQuery =
                    item.getCustomerName() != null &&
                            item.getCustomerName().toLowerCase().contains(query.toLowerCase());

            boolean matchStatus =
                    status.equals("All") ||
                            (item.getStatus() != null &&
                                    item.getStatus().equalsIgnoreCase(status));

            if (matchQuery && matchStatus) {
                filtered.add(item);
            }
        }

        displayedList.clear();
        displayedList.addAll(filtered);

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return displayedList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        QuoteResponse item = displayedList.get(position);

        holder.title.setText(item.getCustomerName() + " - " + item.getPlanName());

        holder.subtitle.setText(
                "Status: " + item.getStatus() +
                        " | Total: $" + item.getTotalAmount()
        );
    }

    static class QuoteViewHolder extends RecyclerView.ViewHolder {

        TextView title, subtitle;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);
        }
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);

        return new QuoteViewHolder(view);
    }
}
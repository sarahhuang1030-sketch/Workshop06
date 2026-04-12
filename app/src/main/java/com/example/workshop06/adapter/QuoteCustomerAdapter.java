package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.model.QuoteResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuoteCustomerAdapter extends RecyclerView.Adapter<QuoteCustomerAdapter.ViewHolder> {

    private final List<QuoteResponse> allQuotes = new ArrayList<>();
    private final List<Integer> uniqueCustomerIds = new ArrayList<>();
    private final List<String> uniqueCustomerNames = new ArrayList<>();

    private final List<Integer> displayedCustomerIds = new ArrayList<>();
    private final List<String> displayedCustomerNames = new ArrayList<>();

    public interface OnCustomerClickListener {
        void onCustomerClick(int customerId, String customerName);
    }

    private final OnCustomerClickListener listener;

    public QuoteCustomerAdapter(OnCustomerClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<QuoteResponse> quotes) {
        allQuotes.clear();
        uniqueCustomerIds.clear();
        uniqueCustomerNames.clear();

        if (quotes != null) {
            allQuotes.addAll(quotes);
            Set<Integer> seenIds = new HashSet<>();
            for (QuoteResponse q : quotes) {
                if (q.getCustomerId() != null && !seenIds.contains(q.getCustomerId())) {
                    seenIds.add(q.getCustomerId());
                    uniqueCustomerIds.add(q.getCustomerId());
                    uniqueCustomerNames.add(q.getCustomerName() != null ? q.getCustomerName() : "Unknown");
                }
            }
        }
        filter("");
    }

    public void filter(String query) {
        displayedCustomerIds.clear();
        displayedCustomerNames.clear();

        String lowerQuery = query.toLowerCase().trim();
        for (int i = 0; i < uniqueCustomerIds.size(); i++) {
            if (lowerQuery.isEmpty() || uniqueCustomerNames.get(i).toLowerCase().contains(lowerQuery)) {
                displayedCustomerIds.add(uniqueCustomerIds.get(i));
                displayedCustomerNames.add(uniqueCustomerNames.get(i));
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = displayedCustomerNames.get(position);
        int id = displayedCustomerIds.get(position);
        holder.text1.setText(name);
        holder.itemView.setOnClickListener(v -> listener.onCustomerClick(id, name));
    }

    @Override
    public int getItemCount() {
        return displayedCustomerIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1;
        ViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
        }
    }
}

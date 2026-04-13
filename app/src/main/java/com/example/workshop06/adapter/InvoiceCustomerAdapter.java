package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.InvoiceResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InvoiceCustomerAdapter extends RecyclerView.Adapter<InvoiceCustomerAdapter.ViewHolder> {

    public interface OnCustomerClickListener {
        void onCustomerClick(int customerId, String customerName);
    }

    private final List<Integer> uniqueCustomerIds = new ArrayList<>();
    private final List<String> uniqueCustomerNames = new ArrayList<>();
    private final List<Integer> displayedCustomerIds = new ArrayList<>();
    private final List<String> displayedCustomerNames = new ArrayList<>();

    private final OnCustomerClickListener listener;

    public InvoiceCustomerAdapter(OnCustomerClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<InvoiceResponse> invoices) {
        uniqueCustomerIds.clear();
        uniqueCustomerNames.clear();

        if (invoices != null) {
            Set<String> seen = new HashSet<>();
            int fakeId = 0;
            for (InvoiceResponse inv : invoices) {
                String name = inv.getCustomerName() != null
                        ? inv.getCustomerName() : "Unknown";
                if (!seen.contains(name)) {
                    seen.add(name);
                    uniqueCustomerIds.add(fakeId++);
                    uniqueCustomerNames.add(name);
                }
            }
        }
        filter("");
    }

    public void filter(String query) {
        displayedCustomerIds.clear();
        displayedCustomerNames.clear();

        String q = query.toLowerCase().trim();
        for (int i = 0; i < uniqueCustomerIds.size(); i++) {
            if (q.isEmpty() || uniqueCustomerNames.get(i).toLowerCase().contains(q)) {
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
                .inflate(R.layout.item_invoice_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = displayedCustomerNames.get(position);
        int id = displayedCustomerIds.get(position);
        holder.tvCustomerName.setText(name);
        holder.tvInitials.setText(getInitials(name));
        holder.itemView.setOnClickListener(v -> listener.onCustomerClick(id, name));
    }

    @Override
    public int getItemCount() {
        return displayedCustomerIds.size();
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvCustomerName;

        ViewHolder(View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
        }
    }
}
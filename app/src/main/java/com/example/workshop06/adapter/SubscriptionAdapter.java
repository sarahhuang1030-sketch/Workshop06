package com.example.workshop06.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.SubscriptionResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    public interface SubscriptionActionListener {
        void onEdit(SubscriptionResponse item);
        void onDelete(SubscriptionResponse item);
    }

    private final List<SubscriptionResponse> originalItems = new ArrayList<>();
    private final List<SubscriptionResponse> filteredItems = new ArrayList<>();
    private final SubscriptionActionListener listener;

    public SubscriptionAdapter(List<SubscriptionResponse> items, SubscriptionActionListener listener) {
        if (items != null) {
            originalItems.addAll(items);
            filteredItems.addAll(items);
        }
        this.listener = listener;
    }

    public void updateData(List<SubscriptionResponse> newItems) {
        originalItems.clear();
        filteredItems.clear();

        if (newItems != null) {
            originalItems.addAll(newItems);
            filteredItems.addAll(newItems);
        }

        Log.d("SubscriptionAdapter", "updateData size: " + filteredItems.size());
        notifyDataSetChanged();
    }

    public void filter(String query, String selectedStatus) {
        filteredItems.clear();

        String safeQuery = query == null ? "" : query.trim().toLowerCase(Locale.US);
        String safeStatus = selectedStatus == null ? "All" : selectedStatus.trim();

        for (SubscriptionResponse item : originalItems) {
            String customerName = safe(item.getCustomerName());
            String planName = safe(item.getPlanName());
            String status = safe(item.getStatus());

            boolean matchesQuery =
                    safeQuery.isEmpty()
                            || customerName.toLowerCase(Locale.US).contains(safeQuery)
                            || planName.toLowerCase(Locale.US).contains(safeQuery);

            boolean matchesStatus =
                    safeStatus.equalsIgnoreCase("All")
                            || status.equalsIgnoreCase(safeStatus);

            if (matchesQuery && matchesStatus) {
                filteredItems.add(item);
            }
        }

        Log.d("SubscriptionAdapter", "filter result size: " + filteredItems.size());
        notifyDataSetChanged();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        SubscriptionResponse item = filteredItems.get(position);

        holder.tvTitle.setText("Subscription #" + item.getSubscriptionId());

        String customerName = safe(item.getCustomerName());
        String planName = safe(item.getPlanName());

        if (customerName.isEmpty()) {
            customerName = "Customer #" + item.getCustomerId();
        }

        if (planName.isEmpty()) {
            planName = "Plan #" + item.getPlanId();
        }

        holder.tvSubtitle.setText(customerName + " • " + planName);

        holder.tvStatus.setText(
                item.getStatus() != null && !item.getStatus().trim().isEmpty()
                        ? item.getStatus()
                        : "—"
        );

        String startDate = item.getStartDate() != null ? item.getStartDate() : "—";
        String endDate = item.getEndDate() != null ? item.getEndDate() : "—";
        holder.tvDates.setText(startDate + " to " + endDate);

        Log.d("SubscriptionAdapter", "Binding row: " + item.getSubscriptionId());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        Log.d("SubscriptionAdapter", "getItemCount: " + filteredItems.size());
        return filteredItems.size();
    }

    static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvStatus, tvDates;
        ImageButton btnEdit, btnDelete;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSubscriptionTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubscriptionSubtitle);
            tvStatus = itemView.findViewById(R.id.tvSubscriptionStatus);
            tvDates = itemView.findViewById(R.id.tvSubscriptionDates);
            btnEdit = itemView.findViewById(R.id.btnEditSubscription);
            btnDelete = itemView.findViewById(R.id.btnDeleteSubscription);
        }
    }
}
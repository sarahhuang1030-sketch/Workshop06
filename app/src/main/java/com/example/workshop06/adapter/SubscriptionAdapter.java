package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.SubscriptionResponse;

import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    public interface SubscriptionActionListener {
        void onEdit(SubscriptionResponse item);
        void onDelete(SubscriptionResponse item);
    }

    private final List<SubscriptionResponse> items;
    private final SubscriptionActionListener listener;

    public SubscriptionAdapter(List<SubscriptionResponse> items, SubscriptionActionListener listener) {
        this.items = items;
        this.listener = listener;
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
        SubscriptionResponse item = items.get(position);

        holder.tvTitle.setText("Subscription #" + item.getSubscriptionId());
        holder.tvSubtitle.setText("Customer " + item.getCustomerId() + " • Plan " + item.getPlanId());
        holder.tvStatus.setText(item.getStatus() != null ? item.getStatus() : "—");
        holder.tvDates.setText(
                (item.getStartDate() != null ? item.getStartDate() : "—") +
                        " to " +
                        (item.getEndDate() != null ? item.getEndDate() : "—")
        );

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
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
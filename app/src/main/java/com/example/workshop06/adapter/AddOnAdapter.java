package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.AddOnResponse;

import java.util.List;
import java.util.Locale;

public class AddOnAdapter extends RecyclerView.Adapter<AddOnAdapter.AddOnViewHolder> {

    public interface AddOnActionListener {
        void onEdit(AddOnResponse item);
        void onDelete(AddOnResponse item);
    }

    private final List<AddOnResponse> items;
    private final AddOnActionListener listener;

    public AddOnAdapter(List<AddOnResponse> items, AddOnActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddOnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_addon, parent, false);
        return new AddOnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddOnViewHolder holder, int position) {
        AddOnResponse item = items.get(position);

        holder.tvName.setText(item.getAddOnName() != null ? item.getAddOnName() : "");
        holder.tvDescription.setText(item.getDescription() != null ? item.getDescription() : "");

        if (item.getMonthlyPrice() != null) {
            holder.tvPrice.setText(String.format(Locale.US, "$%.2f / month", item.getMonthlyPrice()));
        } else {
            holder.tvPrice.setText("$0.00 / month");
        }

        holder.tvStatus.setText(Boolean.TRUE.equals(item.getIsActive()) ? "Active" : "Inactive");

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class AddOnViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvPrice, tvStatus;
        ImageButton btnEdit, btnDelete;

        public AddOnViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAddOnName);
            tvDescription = itemView.findViewById(R.id.tvAddOnDescription);
            tvPrice = itemView.findViewById(R.id.tvAddOnPrice);
            tvStatus = itemView.findViewById(R.id.tvAddOnStatus);
            btnEdit = itemView.findViewById(R.id.btnEditAddOn);
            btnDelete = itemView.findViewById(R.id.btnDeleteAddOn);
        }
    }
}
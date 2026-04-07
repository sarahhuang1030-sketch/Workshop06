package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.CustomerResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    public interface OnCustomerActionListener {
        void onEdit(CustomerResponse item);
        void onDelete(CustomerResponse item);
        void onAddress(CustomerResponse item);
    }

    private final List<CustomerResponse> fullList = new ArrayList<>();
    private final List<CustomerResponse> filteredList = new ArrayList<>();
    private final OnCustomerActionListener listener;

    private boolean readOnlyMode = false;

    public CustomerAdapter(OnCustomerActionListener listener) {
        this.listener = listener;
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
        notifyDataSetChanged();
    }

    public void setData(List<CustomerResponse> data) {
        fullList.clear();
        filteredList.clear();

        if (data != null) {
            fullList.addAll(data);
            filteredList.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void applyFilters(String searchText,
                             String statusFilter,
                             String typeFilter) {

        filteredList.clear();

        String search = searchText == null ? "" : searchText.toLowerCase(Locale.US).trim();
        String status = statusFilter == null ? "All Status" : statusFilter.trim();
        String type = typeFilter == null ? "All Types" : typeFilter.trim();

        for (CustomerResponse item : fullList) {
            String first = item.getFirstName() == null ? "" : item.getFirstName().toLowerCase(Locale.US);
            String last = item.getLastName() == null ? "" : item.getLastName().toLowerCase(Locale.US);
            String business = item.getBusinessName() == null ? "" : item.getBusinessName().toLowerCase(Locale.US);
            String email = item.getEmail() == null ? "" : item.getEmail().toLowerCase(Locale.US);
            String phone = item.getHomePhone() == null ? "" : item.getHomePhone().toLowerCase(Locale.US);
            String customerStatus = item.getStatus() == null ? "" : item.getStatus();
            String customerType = item.getCustomerType() == null ? "" : item.getCustomerType();

            boolean matchesSearch =
                    search.isEmpty()
                            || first.contains(search)
                            || last.contains(search)
                            || business.contains(search)
                            || email.contains(search)
                            || phone.contains(search);

            boolean matchesStatus =
                    status.equalsIgnoreCase("All Status")
                            || customerStatus.equalsIgnoreCase(status);

            boolean matchesType =
                    type.equalsIgnoreCase("All Types")
                            || customerType.equalsIgnoreCase(type);

            if (matchesSearch && matchesStatus && matchesType) {
                filteredList.add(item);
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerResponse item = filteredList.get(position);

        String displayName;
        if ("Business".equalsIgnoreCase(item.getCustomerType())) {
            displayName = item.getBusinessName();
        } else {
            String first = item.getFirstName() != null ? item.getFirstName() : "";
            String last = item.getLastName() != null ? item.getLastName() : "";
            displayName = (first + " " + last).trim();
        }

        if (displayName == null || displayName.isEmpty()) {
            displayName = "Unnamed Customer";
        }

        holder.tvCustomerId.setText(displayName);
        holder.tvCustomerName.setVisibility(View.GONE);

        holder.tvCustomerType.setText(item.getCustomerType() != null ? item.getCustomerType() : "-");
        holder.tvBusinessName.setText(item.getBusinessName() != null ? item.getBusinessName() : "-");
        holder.tvEmail.setText(item.getEmail() != null ? item.getEmail() : "-");
        holder.tvPhone.setText(item.getHomePhone() != null ? item.getHomePhone() : "-");
        holder.tvStatus.setText(item.getStatus() != null ? item.getStatus() : "-");
        holder.tvCreatedAt.setText(item.getCreatedAt() != null ? item.getCreatedAt() : "-");

        holder.btnEdit.setVisibility(readOnlyMode ? View.GONE : View.VISIBLE);
        holder.btnDelete.setVisibility(readOnlyMode ? View.GONE : View.VISIBLE);
        holder.btnAddress.setVisibility(View.VISIBLE);

        holder.btnEdit.setOnClickListener(v -> {
            if (!readOnlyMode && listener != null) listener.onEdit(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (!readOnlyMode && listener != null) listener.onDelete(item);
        });

        holder.btnAddress.setOnClickListener(v -> {
            if (listener != null) listener.onAddress(item);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerId, tvCustomerName, tvCustomerType, tvBusinessName,
                tvEmail, tvPhone, tvStatus, tvCreatedAt;
        ImageButton btnEdit, btnDelete, btnAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerId = itemView.findViewById(R.id.tvCustomerId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerType = itemView.findViewById(R.id.tvCustomerType);
            tvBusinessName = itemView.findViewById(R.id.tvBusinessName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnAddress = itemView.findViewById(R.id.btnAddress);
        }
    }
}
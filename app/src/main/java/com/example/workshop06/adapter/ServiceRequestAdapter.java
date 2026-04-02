package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.ServiceRequestResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServiceRequestAdapter extends RecyclerView.Adapter<ServiceRequestAdapter.ViewHolder> {

    public interface OnRequestActionListener {
        void onEdit(ServiceRequestResponse item);
        void onDelete(ServiceRequestResponse item);
        void onAppointments(ServiceRequestResponse item);
    }

    private final List<ServiceRequestResponse> fullList = new ArrayList<>();
    private final List<ServiceRequestResponse> filteredList = new ArrayList<>();
    private final OnRequestActionListener listener;

    public ServiceRequestAdapter(OnRequestActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<ServiceRequestResponse> data) {
        fullList.clear();
        filteredList.clear();

        if (data != null) {
            fullList.addAll(data);
            filteredList.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void filter(String keyword) {
        filteredList.clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String q = keyword.toLowerCase(Locale.US).trim();

            for (ServiceRequestResponse item : fullList) {
                String customerName = item.getCustomerName() != null ? item.getCustomerName().toLowerCase(Locale.US) : "";
                String technicianName = item.getTechnicianName() != null ? item.getTechnicianName().toLowerCase(Locale.US) : "";
                String requestType = item.getRequestType() != null ? item.getRequestType().toLowerCase(Locale.US) : "";
                String status = item.getStatus() != null ? item.getStatus().toLowerCase(Locale.US) : "";
                String priority = item.getPriority() != null ? item.getPriority().toLowerCase(Locale.US) : "";
                String requestId = item.getRequestId() != null ? String.valueOf(item.getRequestId()) : "";

                if (customerName.contains(q)
                        || technicianName.contains(q)
                        || requestType.contains(q)
                        || status.contains(q)
                        || priority.contains(q)
                        || requestId.contains(q)) {
                    filteredList.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceRequestAdapter.ViewHolder holder, int position) {
        ServiceRequestResponse item = filteredList.get(position);

        holder.tvRequestId.setText(item.getRequestId() != null
                ? "Request #" + item.getRequestId()
                : "Request #-");

        holder.tvCustomerName.setText(item.getCustomerName() != null && !item.getCustomerName().trim().isEmpty()
                ? item.getCustomerName()
                : "Unknown Customer");

        holder.tvRequestType.setText(item.getRequestType() != null ? item.getRequestType() : "-");
        holder.tvStatus.setText(item.getStatus() != null ? item.getStatus() : "-");
        holder.tvPriority.setText(item.getPriority() != null ? item.getPriority() : "-");
        holder.tvTechnician.setText(item.getTechnicianName() != null ? item.getTechnicianName() : "—");
        holder.tvAddress.setText(item.getAddressText() != null ? item.getAddressText() : "—");
        holder.tvDescription.setText(item.getDescription() != null ? item.getDescription() : "—");
        holder.tvCreatedAt.setText(item.getCreatedAt() != null ? item.getCreatedAt() : "—");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });

        holder.btnAppointments.setOnClickListener(v -> {
            if (listener != null) listener.onAppointments(item);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRequestId, tvCustomerName, tvRequestType, tvStatus, tvPriority,
                tvTechnician, tvAddress, tvDescription, tvCreatedAt;
        ImageButton btnEdit, btnDelete, btnAppointments;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRequestId = itemView.findViewById(R.id.tvRequestId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvRequestType = itemView.findViewById(R.id.tvRequestType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvTechnician = itemView.findViewById(R.id.tvTechnician);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnAppointments = itemView.findViewById(R.id.btnAppointments);
        }
    }
}
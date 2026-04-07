package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.ServiceAppointmentResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServiceAppointmentAdapter extends RecyclerView.Adapter<ServiceAppointmentAdapter.ViewHolder> {

    public interface OnAppointmentActionListener {
        void onEdit(ServiceAppointmentResponse item);
        void onDelete(ServiceAppointmentResponse item);
    }

    private final List<ServiceAppointmentResponse> fullList = new ArrayList<>();
    private final List<ServiceAppointmentResponse> filteredList = new ArrayList<>();
    private final OnAppointmentActionListener listener;

    private boolean readOnlyMode = false;

    public ServiceAppointmentAdapter(OnAppointmentActionListener listener) {
        this.listener = listener;
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
        notifyDataSetChanged();
    }

    public void setData(List<ServiceAppointmentResponse> data) {
        fullList.clear();
        filteredList.clear();
        if (data != null) {
            fullList.addAll(data);
            filteredList.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void applyFilters(String keyword, String statusFilter, String locationTypeFilter, String technicianFilter) {
        filteredList.clear();

        String q = keyword == null ? "" : keyword.toLowerCase(Locale.US).trim();
        String status = statusFilter == null ? "All" : statusFilter.trim();
        String locationType = locationTypeFilter == null ? "All" : locationTypeFilter.trim();
        String technician = technicianFilter == null ? "All" : technicianFilter.trim();

        for (ServiceAppointmentResponse item : fullList) {
            String techName = item.getTechnicianName() != null
                    ? item.getTechnicianName().trim()
                    : "";

            String techNameLower = techName.toLowerCase(Locale.US);

            String itemStatus = item.getStatus() != null
                    ? item.getStatus().trim()
                    : "";

            String itemLocationType = item.getLocationType() != null
                    ? item.getLocationType().trim()
                    : "";

            String address = item.getAddressText() != null
                    ? item.getAddressText().toLowerCase(Locale.US).trim()
                    : "";

            boolean matchesSearch = q.isEmpty()
                    || techNameLower.contains(q)
                    || address.contains(q);

            boolean matchesStatus = status.equalsIgnoreCase("All")
                    || itemStatus.equalsIgnoreCase(status);

            boolean matchesLocationType = locationType.equalsIgnoreCase("All")
                    || itemLocationType.equalsIgnoreCase(locationType);

            boolean matchesTechnician = technician.equalsIgnoreCase("All")
                    || techName.equalsIgnoreCase(technician);

            if (matchesSearch && matchesStatus && matchesLocationType && matchesTechnician) {
                filteredList.add(item);
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceAppointmentResponse item = filteredList.get(position);

        holder.tvAppointmentId.setText(item.getAppointmentId() != null
                ? "Appointment #" + item.getAppointmentId()
                : "Appointment #-");

        holder.tvTechnician.setText(item.getTechnicianName() != null ? item.getTechnicianName() : "—");
        holder.tvLocationType.setText(item.getLocationType() != null ? item.getLocationType() : "—");
        holder.tvAddress.setText(item.getAddressText() != null ? item.getAddressText() : "—");
        holder.tvStart.setText(item.getScheduledStart() != null ? item.getScheduledStart() : "—");
        holder.tvEnd.setText(item.getScheduledEnd() != null ? item.getScheduledEnd() : "—");
        holder.tvStatus.setText(item.getStatus() != null ? item.getStatus() : "—");
        holder.tvNotes.setText(item.getNotes() != null ? item.getNotes() : "—");

        holder.btnEdit.setVisibility(readOnlyMode ? View.GONE : View.VISIBLE);
        holder.btnDelete.setVisibility(readOnlyMode ? View.GONE : View.VISIBLE);

        holder.btnEdit.setOnClickListener(v -> {
            if (!readOnlyMode && listener != null) listener.onEdit(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (!readOnlyMode && listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppointmentId, tvTechnician, tvLocationType, tvAddress, tvStart, tvEnd, tvStatus, tvNotes;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppointmentId = itemView.findViewById(R.id.tvAppointmentId);
            tvTechnician = itemView.findViewById(R.id.tvTechnician);
            tvLocationType = itemView.findViewById(R.id.tvLocationType);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStart = itemView.findViewById(R.id.tvStart);
            tvEnd = itemView.findViewById(R.id.tvEnd);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
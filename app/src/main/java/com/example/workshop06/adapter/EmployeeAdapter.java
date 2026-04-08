package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.EmployeeResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {

    public interface OnEmployeeActionListener {
        void onEdit(EmployeeResponse item);
        void onDelete(EmployeeResponse item);
    }

    private final List<EmployeeResponse> items = new ArrayList<>();
    private final OnEmployeeActionListener listener;

    private Map<Integer, String> managerNameMap = new HashMap<>();
    private Map<Integer, String> locationNameMap = new HashMap<>();



    public EmployeeAdapter(OnEmployeeActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<EmployeeResponse> data) {
        items.clear();

        if (data != null) {
            items.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void setManagerNameMap(Map<Integer, String> managerNameMap) {
        this.managerNameMap = managerNameMap != null ? managerNameMap : new HashMap<>();
        notifyDataSetChanged();
    }

    public void setLocationNameMap(Map<Integer, String> locationNameMap) {
        this.locationNameMap = locationNameMap != null ? locationNameMap : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmployeeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeAdapter.ViewHolder holder, int position) {
        EmployeeResponse item = items.get(position);

        String firstName = item.getFirstName() != null ? item.getFirstName().trim() : "";
        String lastName = item.getLastName() != null ? item.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();

        holder.tvEmployeeId.setText(fullName.isEmpty() ? "Unknown Employee" : fullName);
        holder.tvEmployeeName.setText(item.getEmail() != null ? item.getEmail() : "-");

        holder.tvEmail.setText(item.getEmail() != null ? item.getEmail() : "-");
        holder.tvPhone.setText(item.getPhone() != null ? item.getPhone() : "-");
        holder.tvRole.setText(item.getRole() != null ? item.getRole() : "-");
        holder.tvSalary.setText(item.getSalary() != null ? String.format(Locale.US, "$%.2f", item.getSalary()) : "-");
        holder.tvHireDate.setText(item.getHireDate() != null ? item.getHireDate() : "-");
        holder.tvStatus.setText(item.getStatus() != null ? item.getStatus() : "-");

        String locationName = "-";
        if (item.getPrimaryLocationId() != null) {
            locationName = locationNameMap.get(item.getPrimaryLocationId());
            if (locationName == null || locationName.trim().isEmpty()) {
                locationName = "Location #" + item.getPrimaryLocationId();
            }
        }
        holder.tvPrimaryLocationId.setText(locationName);

        String managerName = "-";
        if (item.getManagerId() != null) {
            managerName = managerNameMap.get(item.getManagerId());
            if (managerName == null || managerName.trim().isEmpty()) {
                managerName = "Employee #" + item.getManagerId();
            }
        }
        holder.tvManagerId.setText(managerName);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeId, tvEmployeeName, tvEmail, tvPhone, tvRole, tvSalary,
                tvHireDate, tvStatus, tvPrimaryLocationId, tvManagerId;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeId = itemView.findViewById(R.id.tvEmployeeId);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvHireDate = itemView.findViewById(R.id.tvHireDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrimaryLocationId = itemView.findViewById(R.id.tvPrimaryLocationId);
            tvManagerId = itemView.findViewById(R.id.tvManagerId);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
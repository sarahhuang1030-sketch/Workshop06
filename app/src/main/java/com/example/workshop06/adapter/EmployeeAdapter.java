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
import java.util.List;
import java.util.Locale;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {

    public interface OnEmployeeActionListener {
        void onEdit(EmployeeResponse item);
        void onDelete(EmployeeResponse item);
    }

    private final List<EmployeeResponse> fullList = new ArrayList<>();
    private final List<EmployeeResponse> filteredList = new ArrayList<>();
    private final OnEmployeeActionListener listener;

    public EmployeeAdapter(OnEmployeeActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<EmployeeResponse> data) {
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

            for (EmployeeResponse item : fullList) {
                String employeeId = item.getEmployeeId() != null ? String.valueOf(item.getEmployeeId()) : "";
                String firstName = item.getFirstName() != null ? item.getFirstName().toLowerCase(Locale.US) : "";
                String lastName = item.getLastName() != null ? item.getLastName().toLowerCase(Locale.US) : "";
                String email = item.getEmail() != null ? item.getEmail().toLowerCase(Locale.US) : "";
                String phone = item.getPhone() != null ? item.getPhone().toLowerCase(Locale.US) : "";
                String role = item.getRole() != null ? item.getRole().toLowerCase(Locale.US) : "";
                String status = item.getStatus() != null ? item.getStatus().toLowerCase(Locale.US) : "";

                if (employeeId.contains(q)
                        || firstName.contains(q)
                        || lastName.contains(q)
                        || email.contains(q)
                        || phone.contains(q)
                        || role.contains(q)
                        || status.contains(q)) {
                    filteredList.add(item);
                }
            }
        }

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
        EmployeeResponse item = filteredList.get(position);

        holder.tvEmployeeId.setText(item.getEmployeeId() != null
                ? "Employee #" + item.getEmployeeId()
                : "Employee #-");

        String fullName = ((item.getFirstName() != null ? item.getFirstName() : "") + " "
                + (item.getLastName() != null ? item.getLastName() : "")).trim();
        holder.tvEmployeeName.setText(fullName.isEmpty() ? "Unknown Employee" : fullName);

        holder.tvEmail.setText(item.getEmail() != null ? item.getEmail() : "-");
        holder.tvPhone.setText(item.getPhone() != null ? item.getPhone() : "-");
        holder.tvRole.setText(item.getRole() != null ? item.getRole() : "-");
        holder.tvSalary.setText(item.getSalary() != null ? String.format(Locale.US, "$%.2f", item.getSalary()) : "-");
        holder.tvHireDate.setText(item.getHireDate() != null ? item.getHireDate() : "-");
        holder.tvStatus.setText(item.getStatus() != null ? item.getStatus() : "-");
        holder.tvActive.setText(item.getActive() != null && item.getActive() == 1 ? "Yes" : "No");
        holder.tvPrimaryLocationId.setText(item.getPrimaryLocationId() != null ? String.valueOf(item.getPrimaryLocationId()) : "-");
        holder.tvManagerId.setText(item.getManagerId() != null ? String.valueOf(item.getManagerId()) : "-");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeId, tvEmployeeName, tvEmail, tvPhone, tvRole, tvSalary,
                tvHireDate, tvStatus, tvActive, tvPrimaryLocationId, tvManagerId;
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
            tvActive = itemView.findViewById(R.id.tvActive);
            tvPrimaryLocationId = itemView.findViewById(R.id.tvPrimaryLocationId);
            tvManagerId = itemView.findViewById(R.id.tvManagerId);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
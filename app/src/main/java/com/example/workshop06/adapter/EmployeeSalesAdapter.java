package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.EmployeeSalesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeSalesAdapter extends RecyclerView.Adapter<EmployeeSalesAdapter.ViewHolder> {

    private final List<EmployeeSalesResponse> fullList = new ArrayList<>();
    private final List<EmployeeSalesResponse> filteredList = new ArrayList<>();

    public void setData(List<EmployeeSalesResponse> data) {
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
            for (EmployeeSalesResponse item : fullList) {
                String name = item.getEmployeeName() != null
                        ? item.getEmployeeName().toLowerCase(Locale.US)
                        : "";

                String employeeId = item.getEmployeeId() != null
                        ? String.valueOf(item.getEmployeeId())
                        : "";

                if (name.contains(q) || employeeId.contains(q)) {
                    filteredList.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmployeeSalesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee_sale, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeSalesAdapter.ViewHolder holder, int position) {
        EmployeeSalesResponse item = filteredList.get(position);

        holder.tvEmployeeId.setText(item.getEmployeeId() != null
                ? String.valueOf(item.getEmployeeId())
                : "-");

        holder.tvEmployeeName.setText(item.getEmployeeName().isEmpty()
                ? "Unknown Employee"
                : item.getEmployeeName());

        holder.tvSalesCount.setText(item.getSalesCount() != null
                ? String.valueOf(item.getSalesCount())
                : "0");

        holder.tvTotalSales.setText(String.format(
                Locale.US,
                "$%.2f",
                item.getTotalSales() != null ? item.getTotalSales() : 0.0
        ));

        holder.tvLastSaleDate.setText(item.getLastSaleDate() != null
                ? item.getLastSaleDate()
                : "-");
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeId, tvEmployeeName, tvSalesCount, tvTotalSales, tvLastSaleDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeId = itemView.findViewById(R.id.tvEmployeeId);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvSalesCount = itemView.findViewById(R.id.tvSalesCount);
            tvTotalSales = itemView.findViewById(R.id.tvTotalSales);
            tvLastSaleDate = itemView.findViewById(R.id.tvLastSaleDate);
        }
    }
}
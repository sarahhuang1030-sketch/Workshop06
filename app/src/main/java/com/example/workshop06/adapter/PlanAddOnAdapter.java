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
import com.example.workshop06.model.ServiceTypeResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlanAddOnAdapter extends RecyclerView.Adapter<PlanAddOnAdapter.ViewHolder> {

    public interface OnPlanAddOnActionListener {
        void onRemove(AddOnResponse item);
    }

    private final List<AddOnResponse> fullList = new ArrayList<>();
    private final List<AddOnResponse> filteredList = new ArrayList<>();
    private final OnPlanAddOnActionListener listener;

    private List<ServiceTypeResponse> serviceTypes;

    public PlanAddOnAdapter(OnPlanAddOnActionListener listener) {
        this.listener = listener;
    }

    public void setServiceTypes(List<ServiceTypeResponse> serviceTypes) {
        this.serviceTypes = serviceTypes;
        notifyDataSetChanged();
    }

    public void setData(List<AddOnResponse> data) {
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

        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.US);

        for (AddOnResponse item : fullList) {
            String name = item.getAddOnName() != null ? item.getAddOnName().toLowerCase(Locale.US) : "";
            String description = item.getDescription() != null ? item.getDescription().toLowerCase(Locale.US) : "";

            if (q.isEmpty() || name.contains(q) || description.contains(q)) {
                filteredList.add(item);
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_add_on, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddOnResponse item = filteredList.get(position);

        holder.tvAddOnName.setText(
                item.getAddOnName() != null && !item.getAddOnName().trim().isEmpty()
                        ? item.getAddOnName()
                        : "Unnamed Add-on"
        );

        holder.tvServiceType.setText(getServiceTypeName(item.getServiceTypeId()));

        holder.tvMonthlyPrice.setText(
                item.getMonthlyPrice() != null
                        ? String.format(Locale.US, "$%.2f", item.getMonthlyPrice())
                        : "-"
        );

        holder.tvDescription.setText(
                item.getDescription() != null && !item.getDescription().trim().isEmpty()
                        ? item.getDescription()
                        : "-"
        );

        holder.tvIsActive.setText(
                item.getIsActive() != null && item.getIsActive()
                        ? "Yes"
                        : "No"
        );

        holder.btnEdit.setVisibility(View.GONE);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(item);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddOnName, tvServiceType, tvMonthlyPrice, tvDescription, tvIsActive;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddOnName = itemView.findViewById(R.id.tvAddOnName);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvMonthlyPrice = itemView.findViewById(R.id.tvMonthlyPrice);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvIsActive = itemView.findViewById(R.id.tvIsActive);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private String getServiceTypeName(Integer id) {
        if (serviceTypes == null || id == null) return String.valueOf(id);

        for (ServiceTypeResponse s : serviceTypes) {
            if (id.equals(s.getServiceTypeId())) {
                return s.getName();
            }
        }
        return String.valueOf(id);
    }
}
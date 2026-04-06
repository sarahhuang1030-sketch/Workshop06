package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.PlanFeatureResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlanFeatureAdapter extends RecyclerView.Adapter<PlanFeatureAdapter.ViewHolder> {

    public interface OnPlanFeatureActionListener {
        void onEdit(PlanFeatureResponse item);
        void onDelete(PlanFeatureResponse item);
    }

    private final List<PlanFeatureResponse> fullList = new ArrayList<>();
    private final List<PlanFeatureResponse> filteredList = new ArrayList<>();
    private final OnPlanFeatureActionListener listener;

    public PlanFeatureAdapter(OnPlanFeatureActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<PlanFeatureResponse> data) {
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

            for (PlanFeatureResponse item : fullList) {
                String featureId = item.getFeatureId() != null ? String.valueOf(item.getFeatureId()) : "";
                String featureName = item.getFeatureName() != null ? item.getFeatureName().toLowerCase(Locale.US) : "";
                String featureValue = item.getFeatureValue() != null ? item.getFeatureValue().toLowerCase(Locale.US) : "";
                String unit = item.getUnit() != null ? item.getUnit().toLowerCase(Locale.US) : "";

                if (featureId.contains(q)
                        || featureName.contains(q)
                        || featureValue.contains(q)
                        || unit.contains(q)) {
                    filteredList.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlanFeatureAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_feature, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanFeatureAdapter.ViewHolder holder, int position) {
        PlanFeatureResponse item = filteredList.get(position);

        holder.tvFeatureName.setText(
                item.getFeatureName() != null && !item.getFeatureName().trim().isEmpty()
                        ? item.getFeatureName()
                        : "Unnamed Feature"
        );

        holder.tvFeatureValue.setText(item.getFeatureValue() != null ? item.getFeatureValue() : "-");

        holder.tvUnit.setText(
                item.getUnit() != null && !item.getUnit().trim().isEmpty()
                        ? "(" + item.getUnit() + ")"
                        : ""
        );

        holder.tvSortOrder.setText(
                item.getSortOrder() != null
                        ? "Order: " + item.getSortOrder()
                        : "-"
        );

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
        TextView tvFeatureName, tvFeatureValue, tvUnit, tvSortOrder;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvFeatureName = itemView.findViewById(R.id.tvFeatureName);
            tvFeatureValue = itemView.findViewById(R.id.tvFeatureValue);
            tvUnit = itemView.findViewById(R.id.tvUnit);
            tvSortOrder = itemView.findViewById(R.id.tvSortOrder);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.PlanResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlanManagerAdapter extends RecyclerView.Adapter<PlanManagerAdapter.ViewHolder> {

    public interface OnPlanActionListener {
        void onEdit(PlanResponse item);
        void onDelete(PlanResponse item);
    }

    private final List<PlanResponse> fullList = new ArrayList<>();
    private final List<PlanResponse> filteredList = new ArrayList<>();
    private final OnPlanActionListener listener;

    public PlanManagerAdapter(OnPlanActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<PlanResponse> data) {
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

            for (PlanResponse item : fullList) {
                String planId = item.getPlanId() != null ? String.valueOf(item.getPlanId()) : "";
                String serviceTypeId = item.getServiceTypeId() != null ? String.valueOf(item.getServiceTypeId()) : "";
                String planName = item.getPlanName() != null ? item.getPlanName().toLowerCase(Locale.US) : "";
                String desc = item.getDescription() != null ? item.getDescription().toLowerCase(Locale.US) : "";
                String tagline = item.getTagline() != null ? item.getTagline().toLowerCase(Locale.US) : "";
                String badge = item.getBadge() != null ? item.getBadge().toLowerCase(Locale.US) : "";
                String dataLabel = item.getDataLabel() != null ? item.getDataLabel().toLowerCase(Locale.US) : "";

                if (planId.contains(q)
                        || serviceTypeId.contains(q)
                        || planName.contains(q)
                        || desc.contains(q)
                        || tagline.contains(q)
                        || badge.contains(q)
                        || dataLabel.contains(q)) {
                    filteredList.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_manager, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanResponse item = filteredList.get(position);

        holder.tvPlanId.setText(item.getPlanId() != null
                ? "Plan #" + item.getPlanId()
                : "Plan #-");

        holder.tvPlanName.setText(item.getPlanName() != null ? item.getPlanName() : "Unnamed Plan");
        holder.tvServiceTypeId.setText(item.getServiceTypeId() != null ? String.valueOf(item.getServiceTypeId()) : "-");
        holder.tvMonthlyPrice.setText(item.getMonthlyPrice() != null
                ? String.format(Locale.US, "$%.2f", item.getMonthlyPrice())
                : "-");
        holder.tvContractTermMonths.setText(item.getContractTermMonths() != null
                ? String.valueOf(item.getContractTermMonths())
                : "-");
        holder.tvDescription.setText(item.getDescription() != null ? item.getDescription() : "-");
        holder.tvIsActive.setText(item.getIsActive() != null && item.getIsActive() == 1 ? "Yes" : "No");
        holder.tvTagline.setText(item.getTagline() != null ? item.getTagline() : "-");
        holder.tvBadge.setText(item.getBadge() != null ? item.getBadge() : "-");
        holder.tvIconKey.setText(item.getIconKey() != null ? item.getIconKey() : "-");
        holder.tvThemeKey.setText(item.getThemeKey() != null ? item.getThemeKey() : "-");
        holder.tvDataLabel.setText(item.getDataLabel() != null ? item.getDataLabel() : "-");

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
        TextView tvPlanId, tvPlanName, tvServiceTypeId, tvMonthlyPrice, tvContractTermMonths,
                tvDescription, tvIsActive, tvTagline, tvBadge, tvIconKey, tvThemeKey, tvDataLabel;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanId = itemView.findViewById(R.id.tvPlanId);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvServiceTypeId = itemView.findViewById(R.id.tvServiceTypeId);
            tvMonthlyPrice = itemView.findViewById(R.id.tvMonthlyPrice);
            tvContractTermMonths = itemView.findViewById(R.id.tvContractTermMonths);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvIsActive = itemView.findViewById(R.id.tvIsActive);
            tvTagline = itemView.findViewById(R.id.tvTagline);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            tvIconKey = itemView.findViewById(R.id.tvIconKey);
            tvThemeKey = itemView.findViewById(R.id.tvThemeKey);
            tvDataLabel = itemView.findViewById(R.id.tvDataLabel);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
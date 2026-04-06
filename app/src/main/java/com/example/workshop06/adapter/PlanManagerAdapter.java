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
import com.example.workshop06.model.PlanResponse;
import com.example.workshop06.model.ServiceTypeResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlanManagerAdapter extends RecyclerView.Adapter<PlanManagerAdapter.ViewHolder> {

    public interface OnPlanActionListener {
        void onEdit(PlanResponse item);
        void onDelete(PlanResponse item);
        void onManageAddOns(PlanResponse item);
    }

    private final List<PlanResponse> fullList = new ArrayList<>();
    private final List<PlanResponse> filteredList = new ArrayList<>();
    private final OnPlanActionListener listener;

    private List<ServiceTypeResponse> serviceTypes;

    // NEW: features grouped by planId
    private Map<Integer, List<PlanFeatureResponse>> featureMap = new HashMap<>();

    public PlanManagerAdapter(OnPlanActionListener listener) {
        this.listener = listener;
    }

    public void setServiceTypes(List<ServiceTypeResponse> serviceTypes) {
        this.serviceTypes = serviceTypes;
        notifyDataSetChanged();
    }

    // NEW
    public void setFeatureMap(Map<Integer, List<PlanFeatureResponse>> featureMap) {
        this.featureMap = featureMap != null ? featureMap : new HashMap<>();
        notifyDataSetChanged();
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

    public void applyFilters(String keyword,
                             Double minAmount,
                             Double maxAmount,
                             String status,
                             Integer contractTermMonths) {

        filteredList.clear();

        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.US);

        for (PlanResponse item : fullList) {
            String planName = item.getPlanName() != null
                    ? item.getPlanName().toLowerCase(Locale.US) : "";

            String description = item.getDescription() != null
                    ? item.getDescription().toLowerCase(Locale.US) : "";

            Double monthlyPrice = item.getMonthlyPrice();
            Integer isActive = item.getIsActive();
            Integer contractTerm = item.getContractTermMonths();

            boolean matchesKeyword =
                    q.isEmpty()
                            || planName.contains(q)
                            || description.contains(q);

            boolean matchesMinAmount =
                    minAmount == null
                            || (monthlyPrice != null && monthlyPrice >= minAmount);

            boolean matchesMaxAmount =
                    maxAmount == null
                            || (monthlyPrice != null && monthlyPrice <= maxAmount);

            boolean matchesStatus = true;
            if ("Active".equalsIgnoreCase(status)) {
                matchesStatus = isActive != null && isActive == 1;
            } else if ("Inactive".equalsIgnoreCase(status)) {
                matchesStatus = isActive != null && isActive == 0;
            }

            boolean matchesContractTerm =
                    contractTermMonths == null
                            || (contractTerm != null && contractTerm.equals(contractTermMonths));

            if (matchesKeyword && matchesMinAmount && matchesMaxAmount
                    && matchesStatus && matchesContractTerm) {
                filteredList.add(item);
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlanManagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_manager, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanResponse item = filteredList.get(position);

        holder.tvPlanTitle.setText(
                item.getPlanName() != null && !item.getPlanName().trim().isEmpty()
                        ? item.getPlanName()
                        : "Unnamed Plan"
        );

        holder.tvServiceTypeId.setText(getServiceTypeName(item.getServiceTypeId()));

        holder.tvMonthlyPrice.setText(
                item.getMonthlyPrice() != null
                        ? String.format(Locale.US, "$%.2f", item.getMonthlyPrice())
                        : "-"
        );

        holder.tvContractTermMonths.setText(
                item.getContractTermMonths() != null
                        ? String.valueOf(item.getContractTermMonths())
                        : "-"
        );

        holder.tvDescription.setText(
                item.getDescription() != null && !item.getDescription().trim().isEmpty()
                        ? item.getDescription()
                        : "-"
        );

        holder.tvAddons.setText(
                item.getAddOnNames() != null && !item.getAddOnNames().trim().isEmpty()
                        ? item.getAddOnNames()
                        : "No add-ons"
        );

        // NEW: show features
        holder.tvFeatures.setText(buildFeatureText(item.getPlanId()));

        holder.tvIsActive.setText(
                item.getIsActive() != null && item.getIsActive() == 1
                        ? "Yes"
                        : "No"
        );

        holder.btnManageAddOns.setOnClickListener(v -> {
            if (listener != null) listener.onManageAddOns(item);
        });

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
        TextView tvPlanTitle, tvServiceTypeId, tvMonthlyPrice, tvContractTermMonths,
                tvDescription, tvAddons, tvFeatures, tvIsActive;
        ImageButton btnManageAddOns, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPlanTitle = itemView.findViewById(R.id.tvPlanTitle);
            tvServiceTypeId = itemView.findViewById(R.id.tvServiceTypeId);
            tvMonthlyPrice = itemView.findViewById(R.id.tvMonthlyPrice);
            tvContractTermMonths = itemView.findViewById(R.id.tvContractTermMonths);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAddons = itemView.findViewById(R.id.tvAddons);
            tvFeatures = itemView.findViewById(R.id.tvFeatures);
            tvIsActive = itemView.findViewById(R.id.tvIsActive);

            btnManageAddOns = itemView.findViewById(R.id.btnManageAddOns);
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

    private String buildFeatureText(Integer planId) {
        if (planId == null || featureMap == null) {
            return "No features";
        }

        List<PlanFeatureResponse> features = featureMap.get(planId);
        if (features == null || features.isEmpty()) {
            return "No features";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < features.size(); i++) {
            PlanFeatureResponse feature = features.get(i);

            String name = feature.getFeatureName() != null ? feature.getFeatureName().trim() : "";
            String value = feature.getFeatureValue() != null ? feature.getFeatureValue().trim() : "";
            String unit = feature.getUnit() != null ? feature.getUnit().trim() : "";

            if (!name.isEmpty()) {
                sb.append(name);
            } else {
                sb.append("Feature");
            }

            if (!value.isEmpty()) {
                sb.append(": ").append(value);
            }

            if (!unit.isEmpty()) {
                sb.append(" ").append(unit);
            }

            if (i < features.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString().trim();
    }
}
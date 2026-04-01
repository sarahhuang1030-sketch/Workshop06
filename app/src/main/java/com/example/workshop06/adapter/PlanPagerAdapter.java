package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.CurrentPlanItemResponse;

import java.util.List;
import java.util.Locale;

public class PlanPagerAdapter extends RecyclerView.Adapter<PlanPagerAdapter.PlanViewHolder> {

    private final List<CurrentPlanItemResponse> plans;

    public PlanPagerAdapter(List<CurrentPlanItemResponse> plans) {
        this.plans = plans;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_card, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        CurrentPlanItemResponse plan = plans.get(position);

        String planName = plan.getPlanName() != null ? plan.getPlanName() : "No Active Plan";
        holder.tvPlanName.setText(planName);

        double total = 0.0;
        if (plan.getTotalMonthlyPrice() != null) {
            total = plan.getTotalMonthlyPrice();
        } else if (plan.getMonthlyPrice() != null) {
            total = plan.getMonthlyPrice();
        }
        holder.tvPlanPrice.setText(String.format(java.util.Locale.getDefault(), "$%.2f", total));

        Integer subscriptionId = plan.getSubscriptionId();
        if (subscriptionId != null && subscriptionId > 0) {
            holder.tvPlanSubInfo.setText("Subscription #" + subscriptionId);
        } else {
            holder.tvPlanSubInfo.setText("No subscription details");
        }

        double addonTotal = plan.getAddonTotal() != null ? plan.getAddonTotal() : 0.0;
        if (addonTotal > 0) {
            holder.tvPlanAddonInfo.setText(
                    String.format(java.util.Locale.getDefault(), "Add-ons included: $%.2f", addonTotal)
            );
        } else {
            holder.tvPlanAddonInfo.setText("No active add-ons");
        }
    }
    @Override
    public int getItemCount() {
        return plans == null ? 0 : plans.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlanName, tvPlanPrice, tvPlanSubInfo, tvPlanAddonInfo;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvPlanPrice = itemView.findViewById(R.id.tvPlanPrice);
            tvPlanSubInfo = itemView.findViewById(R.id.tvPlanSubInfo);
            tvPlanAddonInfo = itemView.findViewById(R.id.tvPlanAddonInfo);
        }
    }
}
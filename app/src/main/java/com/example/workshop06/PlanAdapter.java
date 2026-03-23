package com.example.workshop06;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<Plan> planList;

    public PlanAdapter(List<Plan> planList) {
        this.planList = planList;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = planList.get(position);
        holder.tvPlanName.setText(plan.getName());
        holder.tvPlanDescription.setText(plan.getDescription());
        holder.tvPlanPrice.setText(plan.getPrice());
        holder.tvPlanBadge.setText(plan.getBadge());
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlanName, tvPlanDescription, tvPlanPrice, tvPlanBadge;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvPlanDescription = itemView.findViewById(R.id.tvPlanDescription);
            tvPlanPrice = itemView.findViewById(R.id.tvPlanPrice);
            tvPlanBadge = itemView.findViewById(R.id.tvPlanBadge);
        }
    }
}
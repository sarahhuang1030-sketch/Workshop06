package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.ActivityLogResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityLogAdapter extends RecyclerView.Adapter<ActivityLogAdapter.ViewHolder> {

    private final List<ActivityLogResponse> fullList = new ArrayList<>();
    private final List<ActivityLogResponse> filteredList = new ArrayList<>();

    public void setData(List<ActivityLogResponse> data) {
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

            for (ActivityLogResponse item : fullList) {
                String id = item.getId() != null ? String.valueOf(item.getId()) : "";
                String module = item.getModule() != null ? item.getModule().toLowerCase(Locale.US) : "";
                String action = item.getAction() != null ? item.getAction().toLowerCase(Locale.US) : "";
                String target = item.getTarget() != null ? item.getTarget().toLowerCase(Locale.US) : "";
                String doneBy = item.getDoneBy() != null ? item.getDoneBy().toLowerCase(Locale.US) : "";
                String timestamp = item.getTimestamp() != null ? item.getTimestamp().toLowerCase(Locale.US) : "";

                if (id.contains(q)
                        || module.contains(q)
                        || action.contains(q)
                        || target.contains(q)
                        || doneBy.contains(q)
                        || timestamp.contains(q)) {
                    filteredList.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityLogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityLogAdapter.ViewHolder holder, int position) {
        ActivityLogResponse item = filteredList.get(position);

        holder.tvLogId.setText(item.getId() != null
                ? "Log #" + item.getId()
                : "Log #-");

        holder.tvModule.setText(item.getModule() != null ? item.getModule() : "-");
        holder.tvAction.setText(item.getAction() != null ? item.getAction() : "-");
        holder.tvTarget.setText(item.getTarget() != null ? item.getTarget() : "-");
        holder.tvDoneBy.setText(item.getDoneBy() != null ? item.getDoneBy() : "-");
        holder.tvTimestamp.setText(item.getTimestamp() != null ? item.getTimestamp() : "-");
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogId, tvModule, tvAction, tvTarget, tvDoneBy, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogId = itemView.findViewById(R.id.tvLogId);
            tvModule = itemView.findViewById(R.id.tvModule);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvTarget = itemView.findViewById(R.id.tvTarget);
            tvDoneBy = itemView.findViewById(R.id.tvDoneBy);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
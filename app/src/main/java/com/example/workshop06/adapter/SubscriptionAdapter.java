package com.example.workshop06.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.SubscriptionAddOnResponse;
import com.example.workshop06.model.SubscriptionResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    public static class CustomerSubscriptionGroup {
        private final Integer customerId;
        private final String customerName;
        private final List<SubscriptionResponse> subscriptions = new ArrayList<>();
        private boolean expanded = false;

        public CustomerSubscriptionGroup(Integer customerId, String customerName) {
            this.customerId = customerId;
            this.customerName = customerName;
        }

        public Integer getCustomerId() {
            return customerId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public List<SubscriptionResponse> getSubscriptions() {
            return subscriptions;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }

        public String getSummaryText() {
            int count = subscriptions.size();
            return count == 1 ? "1 subscription" : count + " subscriptions";
        }

        public String getPrimaryStatus() {
            if (subscriptions.isEmpty()) return "—";

            for (SubscriptionResponse item : subscriptions) {
                String status = safe(item.getStatus());
                if (!status.isEmpty()) {
                    return status;
                }
            }
            return "—";
        }

        private static String safe(String value) {
            return value == null ? "" : value.trim();
        }
    }

    private final List<SubscriptionResponse> originalItems = new ArrayList<>();
    private final List<CustomerSubscriptionGroup> groupedItems = new ArrayList<>();

    public SubscriptionAdapter(List<SubscriptionResponse> items) {
        if (items != null) {
            originalItems.addAll(items);
        }
        regroupAndDisplay(originalItems, "", "All");
    }

    public void updateData(List<SubscriptionResponse> newItems) {
        originalItems.clear();
        if (newItems != null) {
            originalItems.addAll(newItems);
        }
        regroupAndDisplay(originalItems, "", "All");
    }

    public void filter(String query, String selectedStatus) {
        regroupAndDisplay(originalItems, query, selectedStatus);
    }

    private void regroupAndDisplay(List<SubscriptionResponse> source,
                                   String query,
                                   String selectedStatus) {

        groupedItems.clear();

        String safeQuery = query == null ? "" : query.trim().toLowerCase(Locale.US);
        String safeStatus = selectedStatus == null ? "All" : selectedStatus.trim();

        Map<String, CustomerSubscriptionGroup> groupedMap = new LinkedHashMap<>();

        for (SubscriptionResponse item : source) {
            String customerName = getDisplayCustomerName(item);
            String planName = getDisplayPlanName(item);
            String status = safe(item.getStatus());

            boolean matchesQuery =
                    safeQuery.isEmpty()
                            || customerName.toLowerCase(Locale.US).contains(safeQuery)
                            || planName.toLowerCase(Locale.US).contains(safeQuery);

            boolean matchesStatus =
                    safeStatus.equalsIgnoreCase("All")
                            || status.equalsIgnoreCase(safeStatus);

            if (!matchesQuery || !matchesStatus) {
                continue;
            }

            String groupKey = item.getCustomerId() != null
                    ? "ID_" + item.getCustomerId()
                    : "NAME_" + customerName.toLowerCase(Locale.US);

            CustomerSubscriptionGroup group = groupedMap.get(groupKey);
            if (group == null) {
                group = new CustomerSubscriptionGroup(item.getCustomerId(), customerName);
                groupedMap.put(groupKey, group);
            }

            group.getSubscriptions().add(item);
        }

        groupedItems.addAll(groupedMap.values());
        notifyDataSetChanged();
    }

    private String getDisplayCustomerName(SubscriptionResponse item) {
        if (item == null) return "Customer";

        String customerName = safe(item.getCustomerName());
        if (!customerName.isEmpty()) {
            return customerName;
        }

        return item.getCustomerId() != null
                ? "Customer #" + item.getCustomerId()
                : "Customer";
    }

    private String getDisplayPlanName(SubscriptionResponse item) {
        if (item == null) return "Plan";

        String planName = safe(item.getPlanName());
        if (!planName.isEmpty()) {
            return planName;
        }

        return item.getPlanId() != null
                ? "Plan #" + item.getPlanId()
                : "Plan";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        CustomerSubscriptionGroup group = groupedItems.get(position);

        holder.tvTitle.setText(group.getCustomerName());
        holder.tvSubtitle.setText(group.getSummaryText());
        holder.tvStatus.setText(group.getPrimaryStatus());
        holder.tvDates.setText("Tap View More to see all subscription details");

        holder.layoutDetails.setVisibility(group.isExpanded() ? View.VISIBLE : View.GONE);
        holder.btnViewMore.setText(group.isExpanded() ? "Hide Details" : "View More");

        renderSubscriptionDetails(holder.itemView.getContext(), holder.containerDetails, group.getSubscriptions());

        holder.btnViewMore.setOnClickListener(v -> {
            group.setExpanded(!group.isExpanded());
            notifyItemChanged(position);
        });
    }

    private void renderSubscriptionDetails(Context context,
                                           LinearLayout container,
                                           List<SubscriptionResponse> subscriptions) {
        container.removeAllViews();

        for (SubscriptionResponse item : subscriptions) {
            MaterialCardView detailCard = new MaterialCardView(context);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.topMargin = dp(context, 8);
            detailCard.setLayoutParams(cardParams);
            detailCard.setRadius(dp(context, 16));
            detailCard.setCardElevation(0f);
            detailCard.setStrokeWidth(dp(context, 1));
            detailCard.setStrokeColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            detailCard.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));

            LinearLayout content = new LinearLayout(context);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(dp(context, 14), dp(context, 14), dp(context, 14), dp(context, 14));

            TextView tvPlan = buildText(context, getDisplayPlanName(item), 15, true, "#2D1F5E");
            TextView tvStatus = buildText(context,
                    "Status: " + (safe(item.getStatus()).isEmpty() ? "—" : item.getStatus()),
                    13, false, "#6F56B3");

            String startDate = item.getStartDate() != null ? item.getStartDate() : "—";
            String endDate = item.getEndDate() != null ? item.getEndDate() : "—";
            TextView tvDates = buildText(context,
                    "Dates: " + startDate + " to " + endDate,
                    13, false, "#7D7896");

            content.addView(tvPlan);
            content.addView(tvStatus);
            content.addView(tvDates);

            List<SubscriptionAddOnResponse> addOns = item.getAddons();
            if (addOns != null && !addOns.isEmpty()) {
                TextView tvAddOnTitle = buildText(context, "Add-ons", 13, true, "#2D1F5E");
                LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) tvAddOnTitle.getLayoutParams();
                if (titleParams != null) {
                    titleParams.topMargin = dp(context, 10);
                } else {
                    tvAddOnTitle.setPadding(0, dp(context, 10), 0, 0);
                }
                content.addView(tvAddOnTitle);

                for (SubscriptionAddOnResponse addOn : addOns) {
                    String addOnName = "Add-on";
                    String addOnStatus = "—";

                    try {
                        if (addOn.getAddOnName() != null && !addOn.getAddOnName().trim().isEmpty()) {
                            addOnName = addOn.getAddOnName();
                        }
                    } catch (Exception ignored) { }

                    try {
                        if (addOn.getStatus() != null && !addOn.getStatus().trim().isEmpty()) {
                            addOnStatus = addOn.getStatus();
                        }
                    } catch (Exception ignored) { }

                    TextView tvAddOn = buildText(context,
                            "• " + addOnName + " (" + addOnStatus + ")",
                            13, false, "#444444");
                    content.addView(tvAddOn);
                }
            } else {
                TextView tvNoAddOn = buildText(context, "Add-ons: None", 13, false, "#7D7896");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.topMargin = dp(context, 10);
                tvNoAddOn.setLayoutParams(params);
                content.addView(tvNoAddOn);
            }

            detailCard.addView(content);
            container.addView(detailCard);
        }
    }

    private TextView buildText(Context context,
                               String text,
                               int textSizeSp,
                               boolean bold,
                               String colorHex) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(textSizeSp);
        tv.setTextColor(android.graphics.Color.parseColor(colorHex));
        tv.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(context, 4);
        tv.setLayoutParams(params);

        return tv;
    }

    private int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return groupedItems.size();
    }

    static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvStatus, tvDates;
        MaterialButton btnViewMore;
        LinearLayout layoutDetails, containerDetails;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSubscriptionTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubscriptionSubtitle);
            tvStatus = itemView.findViewById(R.id.tvSubscriptionStatus);
            tvDates = itemView.findViewById(R.id.tvSubscriptionDates);
            btnViewMore = itemView.findViewById(R.id.btnViewMoreSubscription);
            layoutDetails = itemView.findViewById(R.id.layoutSubscriptionDetails);
            containerDetails = itemView.findViewById(R.id.containerSubscriptionDetails);
        }
    }
}
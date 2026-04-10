package com.example.workshop06.adapter;

import android.content.Context;
import android.graphics.Color;
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

    public interface OnSubscriptionStatusActionListener {
        void onActivateClicked(SubscriptionResponse subscription);
        void onDeactivateClicked(SubscriptionResponse subscription);
        void onEditClicked(SubscriptionResponse subscription);
    }

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
    private final OnSubscriptionStatusActionListener listener;

    public SubscriptionAdapter(List<SubscriptionResponse> items,
                               OnSubscriptionStatusActionListener listener) {
        this.listener = listener;
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

        Map<String, Boolean> expandedStateMap = new LinkedHashMap<>();
        for (CustomerSubscriptionGroup group : groupedItems) {
            String key = group.getCustomerId() != null
                    ? "ID_" + group.getCustomerId()
                    : "NAME_" + group.getCustomerName().toLowerCase(Locale.US);
            expandedStateMap.put(key, group.isExpanded());
        }

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
                Boolean wasExpanded = expandedStateMap.get(groupKey);
                group.setExpanded(wasExpanded != null && wasExpanded);
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
            notifyItemChanged(holder.getBindingAdapterPosition());
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
            detailCard.setStrokeColor(Color.parseColor("#DDD6F3"));
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

            double planPrice = item.getMonthlyPrice() != null ? item.getMonthlyPrice() : 0.0;
            TextView tvPlanPrice = buildText(
                    context,
                    String.format(Locale.US, "Plan Amount: $%.2f/mo", planPrice),
                    13,
                    false,
                    "#444444"
            );

            TextView tvTotal = buildText(
                    context,
                    String.format(Locale.US, "Total Amount: $%.2f/mo", item.getTotalAmount()),
                    14,
                    true,
                    "#2D1F5E"
            );

            content.addView(tvPlan);
            content.addView(tvStatus);
            content.addView(tvDates);
            content.addView(tvPlanPrice);

            List<SubscriptionAddOnResponse> addOns = item.getAddons();
            if (addOns != null && !addOns.isEmpty()) {
                TextView tvAddOnTitle = buildText(context, "Add-ons", 13, true, "#2D1F5E");
                LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) tvAddOnTitle.getLayoutParams();
                titleParams.topMargin = dp(context, 10);
                tvAddOnTitle.setLayoutParams(titleParams);
                content.addView(tvAddOnTitle);

                for (SubscriptionAddOnResponse addOn : addOns) {
                    String addOnName = safe(addOn.getAddOnName()).isEmpty() ? "Add-on" : addOn.getAddOnName();
                    String addOnStatus = safe(addOn.getStatus()).isEmpty() ? "—" : addOn.getStatus();
                    String priceText = addOn.getPrice() != null
                            ? String.format(Locale.US, " - $%.2f", addOn.getPrice())
                            : "";

                    TextView tvAddOn = buildText(
                            context,
                            "• " + addOnName + " (" + addOnStatus + ")" + priceText,
                            13,
                            false,
                            "#444444"
                    );
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

            LinearLayout.LayoutParams totalParams = (LinearLayout.LayoutParams) tvTotal.getLayoutParams();
            totalParams.topMargin = dp(context, 10);
            tvTotal.setLayoutParams(totalParams);
            content.addView(tvTotal);

            LinearLayout buttonRow = new LinearLayout(context);
            buttonRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams buttonRowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            buttonRowParams.topMargin = dp(context, 12);
            buttonRow.setLayoutParams(buttonRowParams);

            MaterialButton btnActivate = new MaterialButton(context);
            LinearLayout.LayoutParams activateParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            );
            activateParams.setMarginEnd(dp(context, 6));
            btnActivate.setLayoutParams(activateParams);
            btnActivate.setText("Activate");
            btnActivate.setAllCaps(false);
            btnActivate.setCornerRadius(dp(context, 12));
            btnActivate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#6F56B3")));
            btnActivate.setTextColor(ContextCompat.getColor(context, android.R.color.white));

            MaterialButton btnDeactivate = new MaterialButton(context);
            LinearLayout.LayoutParams deactivateParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            );
            deactivateParams.setMarginStart(dp(context, 6));
            deactivateParams.setMarginEnd(dp(context, 6));
            btnDeactivate.setLayoutParams(deactivateParams);
            btnDeactivate.setText("Deactivate");
            btnDeactivate.setAllCaps(false);
            btnDeactivate.setCornerRadius(dp(context, 12));
            btnDeactivate.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.white));
            btnDeactivate.setStrokeWidth(dp(context, 1));
            btnDeactivate.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#6F56B3")));
            btnDeactivate.setTextColor(Color.parseColor("#6F56B3"));

            MaterialButton btnEdit = new MaterialButton(context);
            LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            );
            editParams.setMarginStart(dp(context, 6));
            btnEdit.setLayoutParams(editParams);
            btnEdit.setText("Edit");
            btnEdit.setAllCaps(false);
            btnEdit.setCornerRadius(dp(context, 12));
            btnEdit.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.white));
            btnEdit.setStrokeWidth(dp(context, 1));
            btnEdit.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#6F56B3")));
            btnEdit.setTextColor(Color.parseColor("#6F56B3"));

            String status = safe(item.getStatus());
            boolean isActive = status.equalsIgnoreCase("Active");

            btnActivate.setEnabled(!isActive);
            btnDeactivate.setEnabled(isActive);

            btnActivate.setAlpha(!isActive ? 1f : 0.5f);
            btnDeactivate.setAlpha(isActive ? 1f : 0.5f);

            btnActivate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivateClicked(item);
                }
            });

            btnDeactivate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeactivateClicked(item);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClicked(item);
                }
            });

            buttonRow.addView(btnActivate);
            buttonRow.addView(btnDeactivate);
            buttonRow.addView(btnEdit);
            content.addView(buttonRow);

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
        tv.setTextColor(Color.parseColor(colorHex));
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
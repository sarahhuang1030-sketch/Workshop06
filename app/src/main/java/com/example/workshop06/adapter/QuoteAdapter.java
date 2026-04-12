package com.example.workshop06.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.QuoteRequest;
import com.example.workshop06.model.QuoteResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.Color;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import com.example.workshop06.model.AddOnResponse;
import com.example.workshop06.model.PlanResponse;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {

    private final List<QuoteResponse> originalList = new ArrayList<>();
    private final List<QuoteResponse> displayedList = new ArrayList<>();

    // Callback to notify activity to refresh
    public interface OnQuoteChangedListener {
        void onChanged();
    }

    private OnQuoteChangedListener listener;

    public void setOnQuoteChangedListener(OnQuoteChangedListener l) {
        this.listener = l;
    }

    public void updateData(List<QuoteResponse> newItems) {
        originalList.clear();
        displayedList.clear();
        if (newItems != null) {
            originalList.addAll(newItems);
            displayedList.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void filter(String query, String status) {
        List<QuoteResponse> filtered = new ArrayList<>();
        for (QuoteResponse item : originalList) {
            boolean matchQuery = item.getCustomerName() != null &&
                    item.getCustomerName().toLowerCase().contains(query.toLowerCase());
            boolean matchStatus = status.equals("All") ||
                    (item.getStatus() != null && item.getStatus().equalsIgnoreCase(status));
            if (matchQuery && matchStatus) filtered.add(item);
        }
        displayedList.clear();
        displayedList.addAll(filtered);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quote, parent, false);
        return new QuoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        QuoteResponse item = displayedList.get(position);
        Context ctx = holder.itemView.getContext();

        // Customer name
        holder.tvCustomerName.setText(
                item.getCustomerName() != null ? item.getCustomerName() : "Unknown"
        );

        // Status badge color
        String status = item.getStatus() != null ? item.getStatus() : "—";
        holder.tvStatus.setText(status);
        switch (status.toUpperCase()) {
            case "PENDING":
                holder.tvStatus.getBackground().setTint(Color.parseColor("#FF9800"));
                break;
            case "APPROVED":
                holder.tvStatus.getBackground().setTint(Color.parseColor("#4CAF50"));
                break;
            case "CANCELLED":
            case "DECLINED":
                holder.tvStatus.getBackground().setTint(Color.parseColor("#F44336"));
                break;
            default:
                holder.tvStatus.getBackground().setTint(Color.parseColor("#9E9E9E"));
        }

        // Amount
        holder.tvAmount.setText(
                item.getTotalAmount() != null
                        ? String.format("$%.2f", item.getTotalAmount())
                        : "$0.00"
        );

        // Plan ID
        holder.tvPlanId.setText(
                item.getPlanId() != null ? "#" + item.getPlanId() : "N/A"
        );

        // Show Edit/Cancel buttons only for PENDING quotes
        boolean isPending = "PENDING".equalsIgnoreCase(status);
        holder.layoutActions.setVisibility(isPending ? View.VISIBLE : View.GONE);

        if (isPending) {

            // ── CANCEL ──
            holder.btnCancel.setOnClickListener(v -> {
                new AlertDialog.Builder(ctx)
                        .setTitle("Cancel Quote")
                        .setMessage("Are you sure you want to cancel this quote for "
                                + item.getCustomerName() + "?")
                        .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                            cancelQuote(ctx, item.getQuoteId());
                        })
                        .setNegativeButton("No", null)
                        .show();
            });

            // ── EDIT ──
            holder.btnEdit.setOnClickListener(v -> {
                showEditDialog(ctx, item);
            });
        }
    }

    // =========================
    // CANCEL API CALL
    // =========================
    private void cancelQuote(Context ctx, Integer quoteId) {
        ApiService api = RetrofitClient.getRetrofitInstance(ctx).create(ApiService.class);
        api.cancelQuote(quoteId).enqueue(new Callback<QuoteResponse>() {
            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ctx, "Quote cancelled", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onChanged();
                } else {
                    Toast.makeText(ctx, "Failed to cancel: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<QuoteResponse> call, Throwable t) {
                Toast.makeText(ctx, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // EDIT DIALOG
    // =========================
    // =========================
// EDIT DIALOG (with plans + addons)
// =========================
    private void showEditDialog(Context ctx, QuoteResponse item) {

        // Show loading toast while fetching
        Toast.makeText(ctx, "Loading plans...", Toast.LENGTH_SHORT).show();

        ApiService api = RetrofitClient.getRetrofitInstance(ctx).create(ApiService.class);

        // Determine service type from current plan to load correct plans/addons
        // We load all plans first, then filter by matching plan's serviceType
        api.getPlans("Internet").enqueue(new Callback<List<PlanResponse>>() {
            @Override
            public void onResponse(Call<List<PlanResponse>> call, Response<List<PlanResponse>> planRes) {

                // Load both Internet and Mobile plans
                api.getPlans("Mobile").enqueue(new Callback<List<PlanResponse>>() {
                    @Override
                    public void onResponse(Call<List<PlanResponse>> call2, Response<List<PlanResponse>> mobileRes) {

                        List<PlanResponse> allPlans = new ArrayList<>();
                        if (planRes.isSuccessful() && planRes.body() != null)
                            allPlans.addAll(planRes.body());
                        if (mobileRes.isSuccessful() && mobileRes.body() != null)
                            allPlans.addAll(mobileRes.body());

                        // Now load addons
                        api.getAddOns().enqueue(new Callback<List<AddOnResponse>>() {
                            @Override
                            public void onResponse(Call<List<AddOnResponse>> call3, Response<List<AddOnResponse>> addonRes) {

                                List<AddOnResponse> allAddons = new ArrayList<>();
                                if (addonRes.isSuccessful() && addonRes.body() != null)
                                    allAddons.addAll(addonRes.body());

                                // Now show dialog on main thread
                                if (ctx instanceof android.app.Activity) {
                                    ((android.app.Activity) ctx).runOnUiThread(() ->
                                            buildEditDialog(ctx, item, allPlans, allAddons)
                                    );
                                }
                            }

                            @Override
                            public void onFailure(Call<List<AddOnResponse>> call3, Throwable t) {
                                Toast.makeText(ctx, "Failed to load addons", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<PlanResponse>> call2, Throwable t) {
                        Toast.makeText(ctx, "Failed to load mobile plans", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<PlanResponse>> call, Throwable t) {
                Toast.makeText(ctx, "Failed to load plans", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildEditDialog(Context ctx, QuoteResponse item,
                                 List<PlanResponse> plans,
                                 List<AddOnResponse> addons) {

        ScrollView scrollView = new ScrollView(ctx);
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(ctx, 16);
        root.setPadding(pad, pad, pad, pad);
        scrollView.addView(root);

        final int[] selectedPlanIdMobile   = {-1};
        final int[] selectedPlanIdInternet = {-1};

        // Split plans by type
        List<PlanResponse> mobilePlans   = new ArrayList<>();
        List<PlanResponse> internetPlans = new ArrayList<>();
        for (PlanResponse p : plans) {
            if ("Mobile".equalsIgnoreCase(p.getServiceType()))   mobilePlans.add(p);
            if ("Internet".equalsIgnoreCase(p.getServiceType())) internetPlans.add(p);
        }

        // Pre-select current plan
        if (item.getPlanId() != null) {
            for (PlanResponse p : mobilePlans) {
                if (p.getPlanId().equals(item.getPlanId())) {
                    selectedPlanIdMobile[0] = item.getPlanId();
                    break;
                }
            }
            for (PlanResponse p : internetPlans) {
                if (p.getPlanId().equals(item.getPlanId())) {
                    selectedPlanIdInternet[0] = item.getPlanId();
                    break;
                }
            }
        }

        List<Integer> currentAddonIds  = item.getAddonIds() != null
                ? new ArrayList<>(item.getAddonIds()) : new ArrayList<>();
        List<Integer> selectedAddonIds = new ArrayList<>(currentAddonIds);

        // =========================
        // MOBILE PLAN SECTION
        // =========================
        addSectionLabel(root, ctx, "📱 Mobile Plan");
        RadioGroup rgMobile = new RadioGroup(ctx);
        rgMobile.setOrientation(RadioGroup.VERTICAL);

        int mobileNoneId = View.generateViewId();
        RadioButton rbMobileNone = new RadioButton(ctx);
        rbMobileNone.setId(mobileNoneId);
        rbMobileNone.setText("None");
        rgMobile.addView(rbMobileNone);

        for (PlanResponse p : mobilePlans) {
            RadioButton rb = new RadioButton(ctx);
            rb.setId(View.generateViewId());
            rb.setText(p.getPlanName() + "  –  $" + String.format("%.2f", p.getMonthlyPrice()));
            rb.setTag(p.getPlanId());
            rgMobile.addView(rb);
        }

        // Default selection
        rgMobile.check(mobileNoneId);
        for (int i = 0; i < rgMobile.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgMobile.getChildAt(i);
            if (rb.getTag() != null && rb.getTag().equals(selectedPlanIdMobile[0])) {
                rgMobile.check(rb.getId());
                break;
            }
        }
        root.addView(rgMobile);

        addDivider(root, ctx);

        // =========================
        // INTERNET PLAN SECTION
        // =========================
        addSectionLabel(root, ctx, "🌐 Internet Plan");
        RadioGroup rgInternet = new RadioGroup(ctx);
        rgInternet.setOrientation(RadioGroup.VERTICAL);

        int internetNoneId = View.generateViewId();
        RadioButton rbInternetNone = new RadioButton(ctx);
        rbInternetNone.setId(internetNoneId);
        rbInternetNone.setText("None");
        rgInternet.addView(rbInternetNone);

        for (PlanResponse p : internetPlans) {
            RadioButton rb = new RadioButton(ctx);
            rb.setId(View.generateViewId());
            rb.setText(p.getPlanName() + "  –  $" + String.format("%.2f", p.getMonthlyPrice()));
            rb.setTag(p.getPlanId());
            rgInternet.addView(rb);
        }

        // Default selection
        rgInternet.check(internetNoneId);
        for (int i = 0; i < rgInternet.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgInternet.getChildAt(i);
            if (rb.getTag() != null && rb.getTag().equals(selectedPlanIdInternet[0])) {
                rgInternet.check(rb.getId());
                break;
            }
        }
        root.addView(rgInternet);

        addDivider(root, ctx);

        // =========================
        // MOBILE ADDONS SECTION
        // =========================
        TextView labelMobileAddon = new TextView(ctx);
        labelMobileAddon.setText("📱 Mobile Add-ons");
        labelMobileAddon.setTextSize(14);
        labelMobileAddon.setTypeface(null, android.graphics.Typeface.BOLD);
        labelMobileAddon.setPadding(0, dpToPx(ctx, 4), 0, dpToPx(ctx, 8));

        LinearLayout mobileAddonContainer = new LinearLayout(ctx);
        mobileAddonContainer.setOrientation(LinearLayout.VERTICAL);

        for (AddOnResponse a : addons) {
            if (!"Mobile".equalsIgnoreCase(a.getServiceTypeName())) continue;
            CheckBox cb = new CheckBox(ctx);
            cb.setText(a.getAddOnName() + "  –  $" + String.format("%.2f", a.getMonthlyPrice()));
            cb.setChecked(currentAddonIds.contains(a.getAddOnId()));
            cb.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) {
                    if (!selectedAddonIds.contains(a.getAddOnId()))
                        selectedAddonIds.add(a.getAddOnId());
                } else {
                    selectedAddonIds.remove(Integer.valueOf(a.getAddOnId()));
                }
            });
            mobileAddonContainer.addView(cb);
        }

        root.addView(labelMobileAddon);
        root.addView(mobileAddonContainer);

        addDivider(root, ctx);

        // =========================
        // INTERNET ADDONS SECTION
        // =========================
        TextView labelInternetAddon = new TextView(ctx);
        labelInternetAddon.setText("🌐 Internet Add-ons");
        labelInternetAddon.setTextSize(14);
        labelInternetAddon.setTypeface(null, android.graphics.Typeface.BOLD);
        labelInternetAddon.setPadding(0, dpToPx(ctx, 4), 0, dpToPx(ctx, 8));

        LinearLayout internetAddonContainer = new LinearLayout(ctx);
        internetAddonContainer.setOrientation(LinearLayout.VERTICAL);

        for (AddOnResponse a : addons) {
            if (!"Internet".equalsIgnoreCase(a.getServiceTypeName())) continue;
            CheckBox cb = new CheckBox(ctx);
            cb.setText(a.getAddOnName() + "  –  $" + String.format("%.2f", a.getMonthlyPrice()));
            cb.setChecked(currentAddonIds.contains(a.getAddOnId()));
            cb.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) {
                    if (!selectedAddonIds.contains(a.getAddOnId()))
                        selectedAddonIds.add(a.getAddOnId());
                } else {
                    selectedAddonIds.remove(Integer.valueOf(a.getAddOnId()));
                }
            });
            internetAddonContainer.addView(cb);
        }

        root.addView(labelInternetAddon);
        root.addView(internetAddonContainer);

        // =========================
        // SET INITIAL ADDON VISIBILITY
        // based on pre-selected plan
        // =========================
        boolean mobilePreSelected   = selectedPlanIdMobile[0]   != -1;
        boolean internetPreSelected = selectedPlanIdInternet[0] != -1;

        labelMobileAddon.setVisibility(mobilePreSelected   ? View.VISIBLE : View.GONE);
        mobileAddonContainer.setVisibility(mobilePreSelected   ? View.VISIBLE : View.GONE);
        labelInternetAddon.setVisibility(internetPreSelected ? View.VISIBLE : View.GONE);
        internetAddonContainer.setVisibility(internetPreSelected ? View.VISIBLE : View.GONE);

        // =========================
        // RADIOGROUP LISTENERS
        // show/hide addons + clear deselected addons
        // =========================
        rgMobile.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == mobileNoneId) {
                selectedPlanIdMobile[0] = -1;
                // Hide mobile addons and uncheck all
                labelMobileAddon.setVisibility(View.GONE);
                mobileAddonContainer.setVisibility(View.GONE);
                for (int i = 0; i < mobileAddonContainer.getChildCount(); i++) {
                    CheckBox cb = (CheckBox) mobileAddonContainer.getChildAt(i);
                    cb.setChecked(false);
                }
            } else {
                RadioButton selected = group.findViewById(checkedId);
                if (selected != null && selected.getTag() != null) {
                    selectedPlanIdMobile[0] = (int) selected.getTag();
                }
                // Show mobile addons
                labelMobileAddon.setVisibility(View.VISIBLE);
                mobileAddonContainer.setVisibility(View.VISIBLE);
            }
        });

        rgInternet.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == internetNoneId) {
                selectedPlanIdInternet[0] = -1;
                // Hide internet addons and uncheck all
                labelInternetAddon.setVisibility(View.GONE);
                internetAddonContainer.setVisibility(View.GONE);
                for (int i = 0; i < internetAddonContainer.getChildCount(); i++) {
                    CheckBox cb = (CheckBox) internetAddonContainer.getChildAt(i);
                    cb.setChecked(false);
                }
            } else {
                RadioButton selected = group.findViewById(checkedId);
                if (selected != null && selected.getTag() != null) {
                    selectedPlanIdInternet[0] = (int) selected.getTag();
                }
                // Show internet addons
                labelInternetAddon.setVisibility(View.VISIBLE);
                internetAddonContainer.setVisibility(View.VISIBLE);
            }
        });

        // =========================
        // BUILD DIALOG
        // =========================
        new AlertDialog.Builder(ctx)
                .setTitle("Edit Quote #" + item.getQuoteId())
                .setView(scrollView)
                .setPositiveButton("Save", (dialog, which) -> {

                    if (selectedPlanIdMobile[0] == -1 && selectedPlanIdInternet[0] == -1) {
                        Toast.makeText(ctx, "Please select at least one plan",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Recalculate total
                    double newTotal = 0;
                    for (PlanResponse p : plans) {
                        if ((selectedPlanIdMobile[0]   != -1 && p.getPlanId().equals(selectedPlanIdMobile[0])) ||
                                (selectedPlanIdInternet[0] != -1 && p.getPlanId().equals(selectedPlanIdInternet[0]))) {
                            newTotal += p.getMonthlyPrice();
                        }
                    }
                    for (AddOnResponse a : addons) {
                        if (selectedAddonIds.contains(a.getAddOnId())) {
                            newTotal += a.getMonthlyPrice();
                        }
                    }

                    // Primary plan: prefer Internet, fallback Mobile
                    int primaryPlanId = selectedPlanIdInternet[0] != -1
                            ? selectedPlanIdInternet[0]
                            : selectedPlanIdMobile[0];

                    updateQuote(ctx, item, primaryPlanId, selectedAddonIds, newTotal);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Helpers ──
    private void addSectionLabel(LinearLayout root, Context ctx, String text) {
        TextView label = new TextView(ctx);
        label.setText(text);
        label.setTextSize(14);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setPadding(0, dpToPx(ctx, 4), 0, dpToPx(ctx, 8));
        root.addView(label);
    }

    private void addDivider(LinearLayout root, Context ctx) {
        View divider = new View(ctx);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(ctx, 1));
        p.setMargins(0, dpToPx(ctx, 12), 0, dpToPx(ctx, 12));
        divider.setLayoutParams(p);
        divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
        root.addView(divider);
    }

    // =========================
    // UPDATE API CALL
    // =========================
    private void updateQuote(Context ctx, QuoteResponse item,
                             int newPlanId,
                             List<Integer> newAddonIds,
                             double newTotal) {

        ApiService api = RetrofitClient.getRetrofitInstance(ctx).create(ApiService.class);

        QuoteRequest request = new QuoteRequest(
                item.getCustomerId(),
                newPlanId,
                newAddonIds,
                newTotal,
                item.getStatus()
        );

        api.updateQuote(item.getQuoteId(), request).enqueue(new Callback<QuoteResponse>() {
            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ctx, "Quote updated successfully", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onChanged();
                } else {
                    Toast.makeText(ctx, "Failed to update: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<QuoteResponse> call, Throwable t) {
                Toast.makeText(ctx, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // HELPER: dp to px
    // =========================
    private int dpToPx(Context ctx, int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() { return displayedList.size(); }

    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvStatus, tvAmount, tvPlanId;
        MaterialButton btnEdit, btnCancel;
        LinearLayout layoutActions;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvStatus       = itemView.findViewById(R.id.tvStatus);
            tvAmount       = itemView.findViewById(R.id.tvAmount);
            tvPlanId       = itemView.findViewById(R.id.tvPlanId);
            btnEdit        = itemView.findViewById(R.id.btnEdit);
            btnCancel      = itemView.findViewById(R.id.btnCancel);
            layoutActions  = itemView.findViewById(R.id.layoutActions);
        }
    }
}
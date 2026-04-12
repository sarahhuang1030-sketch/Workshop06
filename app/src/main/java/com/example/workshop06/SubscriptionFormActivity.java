package com.example.workshop06;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.AddOnResponse;
import com.example.workshop06.model.CustomerResponse;
import com.example.workshop06.model.PlanResponse;
import com.example.workshop06.model.SubscriptionAddOnResponse;
import com.example.workshop06.model.SubscriptionRequest;
import com.example.workshop06.model.SubscriptionStatusResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionFormActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView spinnerCustomer;
    private MaterialAutoCompleteTextView spinnerPlan;
    private MaterialAutoCompleteTextView spinnerStatus;
    private EditText etStartDate, etEndDate, etBillingCycleDay, etNotes;
    private TextInputLayout tilStartDate, tilEndDate;
    private Button btnSaveSubscription;

    private LinearLayout layoutSubscriptionAddOns;
    private TextView tvSubscriptionAddOns;
    private Button btnManageSubscriptionAddOns;

    private Integer subscriptionId = null;
    private Integer customerId = null;
    private Integer planId = null;

    private final List<CustomerResponse> customerList = new ArrayList<>();
    private final List<PlanResponse> planList = new ArrayList<>();
    private final List<AddOnResponse> allAddOns = new ArrayList<>();
    private final List<SubscriptionAddOnResponse> currentSubscriptionAddOns = new ArrayList<>();

    private final List<SubscriptionStatusResponse> statusList = new ArrayList<>();
    private final List<String> statusLabels = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_form);

        spinnerCustomer = findViewById(R.id.spinnerCustomer);
        spinnerPlan = findViewById(R.id.spinnerPlan);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etBillingCycleDay = findViewById(R.id.etBillingCycleDay);
        etNotes = findViewById(R.id.etNotes);

        tilStartDate = findViewById(R.id.tilStartDate);
        tilEndDate = findViewById(R.id.tilEndDate);
        btnSaveSubscription = findViewById(R.id.btnSaveSubscription);

        layoutSubscriptionAddOns = findViewById(R.id.layoutSubscriptionAddOns);
        tvSubscriptionAddOns = findViewById(R.id.tvSubscriptionAddOns);
        btnManageSubscriptionAddOns = findViewById(R.id.btnManageSubscriptionAddOns);

        loadStatuses();
        setupDatePickers();
        readIntentData();
        loadCustomers();
        loadPlans();
        loadAllAddOns();

        if (subscriptionId != null) {
            loadSubscription(subscriptionId);
            loadSubscriptionAddOns(subscriptionId);
            showAddOnSection(true);
            btnManageSubscriptionAddOns.setEnabled(true);
        } else {
            showAddOnSection(true);
            tvSubscriptionAddOns.setText("Save subscription first to manage add-ons");
            btnManageSubscriptionAddOns.setEnabled(false);
        }

        btnManageSubscriptionAddOns.setOnClickListener(v -> {
            if (subscriptionId == null) {
                Toast.makeText(this, "Save the subscription first before managing add-ons", Toast.LENGTH_SHORT).show();
                return;
            }
            showManageAddOnsDialog();
        });

        btnSaveSubscription.setOnClickListener(v -> saveSubscription());
    }

    private void readIntentData() {
        if (getIntent() == null) return;

        int id = getIntent().getIntExtra("subscriptionId", -1);
        if (id != -1) subscriptionId = id;

        int cId = getIntent().getIntExtra("customerId", -1);
        if (cId != -1) customerId = cId;

        int pId = getIntent().getIntExtra("planId", -1);
        if (pId != -1) planId = pId;

        String customerName = getIntent().getStringExtra("customerName");
        String planName = getIntent().getStringExtra("planName");

        if (customerName != null) {
            spinnerCustomer.setText(customerName, false);
        }
        if (planName != null) {
            spinnerPlan.setText(planName, false);
        }
    }

    private void loadStatuses() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getSubscriptionStatuses().enqueue(new Callback<List<SubscriptionStatusResponse>>() {
            @Override
            public void onResponse(Call<List<SubscriptionStatusResponse>> call,
                                   Response<List<SubscriptionStatusResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    statusList.clear();
                    statusLabels.clear();

                    List<SubscriptionStatusResponse> fetched = new ArrayList<>(response.body());
                    fetched.sort((a, b) -> {
                        int aOrder = a.getSortOrder() != null ? a.getSortOrder() : 999;
                        int bOrder = b.getSortOrder() != null ? b.getSortOrder() : 999;
                        return Integer.compare(aOrder, bOrder);
                    });

                    for (SubscriptionStatusResponse status : fetched) {
                        if (status == null) continue;

                        Boolean isActive = status.getIsActive();
                        if (Boolean.TRUE.equals(isActive)) {
                            statusList.add(status);
                            statusLabels.add(
                                    status.getDisplayName() != null
                                            ? status.getDisplayName().trim()
                                            : ""
                            );
                        }
                    }

                    ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                            SubscriptionFormActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            statusLabels
                    );

                    spinnerStatus.setAdapter(statusAdapter);
                    spinnerStatus.setOnClickListener(v -> spinnerStatus.showDropDown());
                    spinnerStatus.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) spinnerStatus.showDropDown();
                    });

                    spinnerStatus.setOnItemClickListener((parent, view, position, id) -> {
                        if (position >= 0 && position < statusLabels.size()) {
                            spinnerStatus.setText(statusLabels.get(position), false);
                        }
                    });

                    String currentStatus = spinnerStatus.getText() != null
                            ? spinnerStatus.getText().toString().trim()
                            : "";

                    if (!currentStatus.isEmpty()) {
                        spinnerStatus.setText(currentStatus, false);
                    } else if (!statusLabels.isEmpty()) {
                        spinnerStatus.setText(statusLabels.get(0), false);
                    }
                } else {
                    Toast.makeText(SubscriptionFormActivity.this, "Failed to load statuses", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SubscriptionStatusResponse>> call, Throwable t) {
                Toast.makeText(SubscriptionFormActivity.this, "Failed to load statuses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));
        tilStartDate.setEndIconOnClickListener(v -> showDatePicker(etStartDate));
        tilEndDate.setEndIconOnClickListener(v -> showDatePicker(etEndDate));
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();

        String existingDate = targetEditText.getText() != null ? targetEditText.getText().toString().trim() : "";
        if (!existingDate.isEmpty()) {
            try {
                LocalDate localDate = LocalDate.parse(existingDate);
                calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
            } catch (Exception ignored) {
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );
                    targetEditText.setText(formattedDate);

                    if (targetEditText == etStartDate) {
                        autoPopulateDatesFromPlanAndStartDate();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void showAddOnSection(boolean show) {
        if (layoutSubscriptionAddOns != null) {
            layoutSubscriptionAddOns.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void loadCustomers() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getCustomers().enqueue(new Callback<List<CustomerResponse>>() {
            @Override
            public void onResponse(Call<List<CustomerResponse>> call, Response<List<CustomerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    customerList.clear();
                    customerList.addAll(response.body());

                    List<String> labels = new ArrayList<>();
                    int selectedIndex = -1;

                    for (int i = 0; i < customerList.size(); i++) {
                        CustomerResponse customer = customerList.get(i);
                        labels.add(getCustomerDisplayName(customer));
                        if (customerId != null && customer.getCustomerId() != null
                                && customerId.intValue() == customer.getCustomerId().intValue()) {
                            selectedIndex = i;
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            SubscriptionFormActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            labels
                    );
                    spinnerCustomer.setAdapter(adapter);
                    spinnerCustomer.setOnClickListener(v -> spinnerCustomer.showDropDown());
                    spinnerCustomer.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) spinnerCustomer.showDropDown();
                    });

                    spinnerCustomer.setOnItemClickListener((parent, view, position, id) -> {
                        CustomerResponse selected = customerList.get(position);
                        customerId = selected.getCustomerId();
                        spinnerCustomer.setText(getCustomerDisplayName(selected), false);
                    });

                    if (selectedIndex >= 0) {
                        spinnerCustomer.setText(labels.get(selectedIndex), false);
                    }
                } else {
                    Toast.makeText(SubscriptionFormActivity.this, "Failed to load customers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CustomerResponse>> call, Throwable t) {
                Toast.makeText(SubscriptionFormActivity.this, "Failed to load customers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlans() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getPlansManager().enqueue(new Callback<List<PlanResponse>>() {
            @Override
            public void onResponse(Call<List<PlanResponse>> call, Response<List<PlanResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    planList.clear();
                    planList.addAll(response.body());

                    List<String> labels = new ArrayList<>();
                    int selectedIndex = -1;

                    for (int i = 0; i < planList.size(); i++) {
                        PlanResponse plan = planList.get(i);
                        String planName = plan.getPlanName() != null && !plan.getPlanName().trim().isEmpty()
                                ? plan.getPlanName().trim()
                                : "Plan #" + plan.getPlanId();

                        labels.add(planName);

                        if (planId != null && plan.getPlanId() != null
                                && planId.intValue() == plan.getPlanId().intValue()) {
                            selectedIndex = i;
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            SubscriptionFormActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            labels
                    );
                    spinnerPlan.setAdapter(adapter);
                    spinnerPlan.setOnClickListener(v -> spinnerPlan.showDropDown());
                    spinnerPlan.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) spinnerPlan.showDropDown();
                    });

                    spinnerPlan.setOnItemClickListener((parent, view, position, id) -> {
                        PlanResponse selected = planList.get(position);
                        planId = selected.getPlanId();

                        String selectedPlanName = selected.getPlanName() != null && !selected.getPlanName().trim().isEmpty()
                                ? selected.getPlanName().trim()
                                : "Plan #" + selected.getPlanId();

                        spinnerPlan.setText(selectedPlanName, false);
                        autoPopulateDatesFromPlanAndStartDate();
                    });

                    if (selectedIndex >= 0) {
                        spinnerPlan.setText(labels.get(selectedIndex), false);
                    }

                    autoPopulateDatesFromPlanAndStartDate();
                } else {
                    Toast.makeText(SubscriptionFormActivity.this, "Failed to load plans", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PlanResponse>> call, Throwable t) {
                Toast.makeText(SubscriptionFormActivity.this, "Failed to load plans", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllAddOns() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getAddOns().enqueue(new Callback<List<AddOnResponse>>() {
            @Override
            public void onResponse(Call<List<AddOnResponse>> call, Response<List<AddOnResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allAddOns.clear();
                    allAddOns.addAll(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<AddOnResponse>> call, Throwable t) {
            }
        });
    }

    private String getCustomerDisplayName(CustomerResponse customer) {
        if (customer == null) return "";

        String customerType = customer.getCustomerType() != null ? customer.getCustomerType().trim() : "";

        if ("Business".equalsIgnoreCase(customerType)) {
            String businessName = customer.getBusinessName() != null ? customer.getBusinessName().trim() : "";
            if (!businessName.isEmpty()) return businessName;
        }

        String firstName = customer.getFirstName() != null ? customer.getFirstName().trim() : "";
        String lastName = customer.getLastName() != null ? customer.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();

        if (!fullName.isEmpty()) return fullName;

        return customer.getCustomerId() != null ? "Customer #" + customer.getCustomerId() : "Customer";
    }

    private void loadSubscription(int id) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getSubscriptionById(id).enqueue(new Callback<SubscriptionRequest>() {
            @Override
            public void onResponse(Call<SubscriptionRequest> call, Response<SubscriptionRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SubscriptionRequest item = response.body();

                    customerId = item.getCustomerId();
                    planId = item.getPlanId();

                    etStartDate.setText(item.getStartDate() != null ? item.getStartDate() : "");
                    etEndDate.setText(item.getEndDate() != null ? item.getEndDate() : "");
                    etBillingCycleDay.setText(item.getBillingCycleDay() != null ? String.valueOf(item.getBillingCycleDay()) : "");
                    etNotes.setText(item.getNotes() != null ? item.getNotes() : "");

                    String status = item.getStatus() != null ? item.getStatus().trim() : "";
                    if (!status.isEmpty()) {
                        spinnerStatus.setText(status, false);
                    }

                    autoPopulateDatesFromPlanAndStartDate();
                } else {
                    Toast.makeText(SubscriptionFormActivity.this, "Failed to load subscription", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SubscriptionRequest> call, Throwable t) {
                Toast.makeText(SubscriptionFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSubscriptionAddOns(int id) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getSubscriptionAddOns(id).enqueue(new Callback<List<SubscriptionAddOnResponse>>() {
            @Override
            public void onResponse(Call<List<SubscriptionAddOnResponse>> call, Response<List<SubscriptionAddOnResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentSubscriptionAddOns.clear();
                    currentSubscriptionAddOns.addAll(response.body());
                    renderSubscriptionAddOns();
                } else {
                    tvSubscriptionAddOns.setText("No add-ons");
                }
            }

            @Override
            public void onFailure(Call<List<SubscriptionAddOnResponse>> call, Throwable t) {
                tvSubscriptionAddOns.setText("No add-ons");
            }
        });
    }

    private void renderSubscriptionAddOns() {
        if (currentSubscriptionAddOns.isEmpty()) {
            tvSubscriptionAddOns.setText("No add-ons");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (SubscriptionAddOnResponse item : currentSubscriptionAddOns) {
            String name = item.getAddOnName() != null && !item.getAddOnName().trim().isEmpty()
                    ? item.getAddOnName().trim()
                    : "Add-on #" + item.getAddOnId();
            sb.append("• ").append(name).append("\n");
        }
        tvSubscriptionAddOns.setText(sb.toString().trim());
    }

    private void showManageAddOnsDialog() {
        if (subscriptionId == null) return;

        if (allAddOns.isEmpty()) {
            Toast.makeText(this, "Add-on list is still loading", Toast.LENGTH_SHORT).show();
            return;
        }

        List<AddOnResponse> available = new ArrayList<>();
        for (AddOnResponse addOn : allAddOns) {
            if (addOn != null && Boolean.TRUE.equals(addOn.getIsActive())) {
                available.add(addOn);
            }
        }

        String[] names = new String[available.size()];
        boolean[] checked = new boolean[available.size()];

        for (int i = 0; i < available.size(); i++) {
            AddOnResponse addOn = available.get(i);
            String name = addOn.getAddOnName() != null ? addOn.getAddOnName() : "Add-on";

            String priceText = "";
            if (addOn.getMonthlyPrice() != null) {
                priceText = String.format(Locale.getDefault(), " ($%.2f/mo)", addOn.getMonthlyPrice());
            }

            names[i] = name + priceText;

            boolean isAttached = false;
            for (SubscriptionAddOnResponse attached : currentSubscriptionAddOns) {
                if (attached.getAddOnId() != null
                        && addOn.getAddOnId() != null
                        && attached.getAddOnId().intValue() == addOn.getAddOnId().intValue()) {
                    isAttached = true;
                    break;
                }
            }
            checked[i] = isAttached;
        }

        boolean[] workingChecked = checked.clone();

        new AlertDialog.Builder(this)
                .setTitle("Manage Subscription Add-ons")
                .setMultiChoiceItems(names, workingChecked, (dialog, which, isChecked) -> workingChecked[which] = isChecked)
                .setPositiveButton("Save", (dialog, which) -> applySubscriptionAddOnChanges(available, checked, workingChecked))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applySubscriptionAddOnChanges(List<AddOnResponse> available, boolean[] original, boolean[] updated) {
        if (subscriptionId == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        for (int i = 0; i < available.size(); i++) {
            AddOnResponse addOn = available.get(i);
            Integer addOnId = addOn.getAddOnId();
            if (addOnId == null) continue;

            boolean wasAttached = original[i];
            boolean shouldBeAttached = updated[i];

            if (!wasAttached && shouldBeAttached) {
                apiService.attachAddOnToSubscription(subscriptionId, addOnId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        loadSubscriptionAddOns(subscriptionId);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                    }
                });
            } else if (wasAttached && !shouldBeAttached) {
                apiService.removeAddOnFromSubscription(subscriptionId, addOnId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        loadSubscriptionAddOns(subscriptionId);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                    }
                });
            }
        }
    }

    private void autoPopulateDatesFromPlanAndStartDate() {
        if (planId == null) return;

        String startDateText = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        if (startDateText.isEmpty()) return;

        PlanResponse selectedPlan = getSelectedPlanById(planId);
        if (selectedPlan == null) return;

        Integer contractTermMonths = selectedPlan.getContractTermMonths();
        if (contractTermMonths == null || contractTermMonths <= 0) return;

        try {
            LocalDate startDate = LocalDate.parse(startDateText);
            LocalDate endDate = startDate.plusMonths(contractTermMonths);

            etEndDate.setText(endDate.toString());
            etBillingCycleDay.setText(String.valueOf(startDate.getDayOfMonth()));

        } catch (DateTimeParseException e) {
            etStartDate.setError("Invalid date format. Use yyyy-MM-dd");
        }
    }

    private PlanResponse getSelectedPlanById(Integer selectedPlanId) {
        if (selectedPlanId == null) return null;

        for (PlanResponse plan : planList) {
            if (plan != null && plan.getPlanId() != null
                    && selectedPlanId.intValue() == plan.getPlanId().intValue()) {
                return plan;
            }
        }
        return null;
    }

    private void saveSubscription() {
        String startDate = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        String endDate = etEndDate.getText() != null ? etEndDate.getText().toString().trim() : "";
        String status = spinnerStatus.getText() != null ? spinnerStatus.getText().toString().trim() : "";
        String billingCycleDayText = etBillingCycleDay.getText() != null ? etBillingCycleDay.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        if (customerId == null) {
            Toast.makeText(this, "Customer is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (planId == null) {
            Toast.makeText(this, "Plan is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate.isEmpty()) {
            etStartDate.setError("Start date is required");
            etStartDate.requestFocus();
            return;
        }

        if (endDate.isEmpty()) {
            etEndDate.setError("End date is required");
            etEndDate.requestFocus();
            return;
        }

        if (status.isEmpty()) {
            spinnerStatus.setError("Status is required");
            spinnerStatus.requestFocus();
            return;
        }

        Integer billingCycleDay = null;
        try {
            if (!billingCycleDayText.isEmpty()) {
                billingCycleDay = Integer.parseInt(billingCycleDayText);
            }
        } catch (Exception e) {
            etBillingCycleDay.setError("Invalid number");
            return;
        }

        SubscriptionRequest request = new SubscriptionRequest(
                subscriptionId,
                customerId,
                planId,
                startDate,
                endDate,
                status,
                billingCycleDay,
                notes.isEmpty() ? null : notes
        );

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if (subscriptionId == null) {
            apiService.createSubscription(request).enqueue(new Callback<SubscriptionRequest>() {
                @Override
                public void onResponse(Call<SubscriptionRequest> call, Response<SubscriptionRequest> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SubscriptionRequest created = response.body();

                        Toast.makeText(
                                SubscriptionFormActivity.this,
                                "Created. Now manage add-ons",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(SubscriptionFormActivity.this, SubscriptionFormActivity.class);
                        intent.putExtra("subscriptionId", created.getSubscriptionId());
                        intent.putExtra("customerId", created.getCustomerId());
                        intent.putExtra("planId", created.getPlanId());
                        intent.putExtra("customerName", spinnerCustomer.getText() != null ? spinnerCustomer.getText().toString() : "");
                        intent.putExtra("planName", spinnerPlan.getText() != null ? spinnerPlan.getText().toString() : "");

                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SubscriptionFormActivity.this, "Create failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SubscriptionRequest> call, Throwable t) {
                    Toast.makeText(SubscriptionFormActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } else {
            apiService.updateSubscription(subscriptionId, request).enqueue(new Callback<SubscriptionRequest>() {
                @Override
                public void onResponse(Call<SubscriptionRequest> call, Response<SubscriptionRequest> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(SubscriptionFormActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(SubscriptionFormActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SubscriptionRequest> call, Throwable t) {
                    Toast.makeText(SubscriptionFormActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
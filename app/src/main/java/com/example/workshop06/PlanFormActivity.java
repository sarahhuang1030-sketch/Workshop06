package com.example.workshop06;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.PlanFeatureCreateUpdateRequest;
import com.example.workshop06.model.PlanFeatureResponse;
import com.example.workshop06.model.PlanResponse;
import com.example.workshop06.model.SavePlanRequest;
import com.example.workshop06.model.ServiceTypeResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanFormActivity extends AppCompatActivity {

    private TextInputEditText etPlanName;
    private TextInputEditText etMonthlyPrice;
    private TextInputEditText etDescription;

    private MaterialAutoCompleteTextView spinnerServiceType;
    private MaterialAutoCompleteTextView spinnerIsActive;
    private MaterialAutoCompleteTextView spinnerContractTerm;
    private MaterialAutoCompleteTextView spinnerFeature;

    private Button btnSave;
    private Button btnAddFeature;
    private ProgressBar progressBar;
    private TextView tvSelectedFeatures;

    private String mode = "add";
    private int planId = -1;

    private ImageButton btnBack;

    private final List<ServiceTypeResponse> serviceTypeList = new ArrayList<>();
    private Integer selectedServiceTypeId = null;
    private Integer selectedContractTermMonths = null;

    private final List<PlanFeatureResponse> availableFeatureTemplates = new ArrayList<>();
    private final List<PlanFeatureResponse> selectedFeatureTemplates = new ArrayList<>();
    private PlanFeatureResponse selectedFeatureTemplate = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_form);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        initViews();
        setupStatusDropdown();
        setupContractTermDropdown();
        loadIntentData();
        loadServiceTypes();
        loadFeatureTemplates();
        loadExistingPlanFeaturesIfEdit();
        setupButtons();
    }

    private void initViews() {
        spinnerServiceType = findViewById(R.id.spinnerServiceType);
        spinnerIsActive = findViewById(R.id.spinnerIsActive);
        spinnerContractTerm = findViewById(R.id.spinnerContractTerm);
        spinnerFeature = findViewById(R.id.spinnerFeature);

        etPlanName = findViewById(R.id.etPlanName);
        etMonthlyPrice = findViewById(R.id.etMonthlyPrice);
        etDescription = findViewById(R.id.etDescription);

        btnSave = findViewById(R.id.btnSave);
        btnAddFeature = findViewById(R.id.btnAddFeature);
        progressBar = findViewById(R.id.progressBar);
        tvSelectedFeatures = findViewById(R.id.tvSelectedFeatures);
    }

    private void setupStatusDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Active", "Inactive"}
        );
        spinnerIsActive.setAdapter(adapter);
        spinnerIsActive.setOnClickListener(v -> spinnerIsActive.showDropDown());
        spinnerIsActive.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) spinnerIsActive.showDropDown();
        });
        spinnerIsActive.setText("Active", false);
    }

    private void setupContractTermDropdown() {
        List<String> terms = new ArrayList<>();
        for (int i = 6; i <= 60; i += 6) {
            terms.add(i + " months");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                terms
        );

        spinnerContractTerm.setAdapter(adapter);
        spinnerContractTerm.setOnClickListener(v -> spinnerContractTerm.showDropDown());
        spinnerContractTerm.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) spinnerContractTerm.showDropDown();
        });

        spinnerContractTerm.setOnItemClickListener((parent, view, position, id) -> {
            selectedContractTermMonths = (position + 1) * 6;
        });
    }

    private void loadFeatureTemplates() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getPlanFeatures().enqueue(new Callback<List<PlanFeatureResponse>>() {
            @Override
            public void onResponse(Call<List<PlanFeatureResponse>> call, Response<List<PlanFeatureResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                availableFeatureTemplates.clear();

                Set<String> seen = new LinkedHashSet<>();
                for (PlanFeatureResponse item : response.body()) {
                    String key = buildFeatureKey(item);
                    if (!seen.contains(key)) {
                        seen.add(key);
                        availableFeatureTemplates.add(item);
                    }
                }

                List<String> labels = new ArrayList<>();
                labels.add("Select a feature");
                for (PlanFeatureResponse item : availableFeatureTemplates) {
                    labels.add(buildFeatureLabel(item));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        PlanFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        labels
                );

                spinnerFeature.setAdapter(adapter);
                spinnerFeature.setText("Select a feature", false);
                spinnerFeature.setOnClickListener(v -> spinnerFeature.showDropDown());
                spinnerFeature.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) spinnerFeature.showDropDown();
                });

                spinnerFeature.setOnItemClickListener((parent, view, position, id) -> {
                    if (position <= 0) {
                        selectedFeatureTemplate = null;
                    } else {
                        selectedFeatureTemplate = availableFeatureTemplates.get(position - 1);
                    }
                });
            }

            @Override
            public void onFailure(Call<List<PlanFeatureResponse>> call, Throwable t) { }
        });
    }

    private void loadServiceTypes() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getServiceTypes().enqueue(new Callback<List<ServiceTypeResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceTypeResponse>> call, Response<List<ServiceTypeResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(PlanFormActivity.this, "Failed to load service types", Toast.LENGTH_SHORT).show();
                    return;
                }

                serviceTypeList.clear();
                serviceTypeList.addAll(response.body());

                List<String> names = new ArrayList<>();
                for (ServiceTypeResponse item : serviceTypeList) {
                    names.add(item.getName() != null ? item.getName() : "Service Type");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        PlanFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        names
                );
                spinnerServiceType.setAdapter(adapter);

                spinnerServiceType.setOnClickListener(v -> spinnerServiceType.showDropDown());
                spinnerServiceType.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) spinnerServiceType.showDropDown();
                });

                spinnerServiceType.setOnItemClickListener((parent, view, position, id) -> {
                    if (position >= 0 && position < serviceTypeList.size()) {
                        selectedServiceTypeId = serviceTypeList.get(position).getServiceTypeId();
                    }
                });

                applySelectedServiceType();
            }

            @Override
            public void onFailure(Call<List<ServiceTypeResponse>> call, Throwable t) {
                Toast.makeText(PlanFormActivity.this, "Failed to load service types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadIntentData() {
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if ("edit".equalsIgnoreCase(mode)) {
            planId = getIntent().getIntExtra("planId", -1);

            int serviceTypeId = getIntent().getIntExtra("serviceTypeId", Integer.MIN_VALUE);
            double monthlyPrice = getIntent().getDoubleExtra("monthlyPrice", Double.NaN);
            int isActive = getIntent().getIntExtra("isActive", Integer.MIN_VALUE);
            int contractTermMonths = getIntent().getIntExtra("contractTermMonths", Integer.MIN_VALUE);

            if (serviceTypeId != Integer.MIN_VALUE) {
                selectedServiceTypeId = serviceTypeId;
            }

            if (!Double.isNaN(monthlyPrice)) {
                etMonthlyPrice.setText(String.format(Locale.US, "%.2f", monthlyPrice));
            }

            if (contractTermMonths != Integer.MIN_VALUE && contractTermMonths > 0) {
                selectedContractTermMonths = contractTermMonths;
                spinnerContractTerm.setText(contractTermMonths + " months", false);
            }

            String planName = getIntent().getStringExtra("planName");
            String description = getIntent().getStringExtra("description");

            etPlanName.setText(planName != null ? planName : "");
            etDescription.setText(description != null ? description : "");

            if (isActive != Integer.MIN_VALUE) {
                spinnerIsActive.setText(isActive == 1 ? "Active" : "Inactive", false);
            }
        }
    }

    private void applySelectedServiceType() {
        if (selectedServiceTypeId == null) return;

        for (ServiceTypeResponse item : serviceTypeList) {
            if (item.getServiceTypeId() != null && item.getServiceTypeId().equals(selectedServiceTypeId)) {
                spinnerServiceType.setText(item.getName(), false);
                break;
            }
        }
    }

    private void loadExistingPlanFeaturesIfEdit() {
        if (!"edit".equalsIgnoreCase(mode) || planId <= 0) {
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getPlanFeaturesByPlanId(planId).enqueue(new Callback<List<PlanFeatureResponse>>() {
            @Override
            public void onResponse(Call<List<PlanFeatureResponse>> call, Response<List<PlanFeatureResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    selectedFeatureTemplates.clear();
                    selectedFeatureTemplates.addAll(response.body());
                    refreshSelectedFeaturesText();
                }
            }

            @Override
            public void onFailure(Call<List<PlanFeatureResponse>> call, Throwable t) { }
        });
    }

    private void setupButtons() {
        btnAddFeature.setOnClickListener(v -> addSelectedFeature());
        btnSave.setOnClickListener(v -> savePlan());
    }

    private void addSelectedFeature() {
        if (selectedFeatureTemplate == null) {
            Toast.makeText(this, "Please select a feature", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedKey = buildFeatureKey(selectedFeatureTemplate);
        for (PlanFeatureResponse item : selectedFeatureTemplates) {
            if (buildFeatureKey(item).equals(selectedKey)) {
                Toast.makeText(this, "Feature already added", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        selectedFeatureTemplates.add(selectedFeatureTemplate);
        refreshSelectedFeaturesText();
        spinnerFeature.setText("Select a feature", false);
        selectedFeatureTemplate = null;
    }

    private void refreshSelectedFeaturesText() {
        if (selectedFeatureTemplates.isEmpty()) {
            tvSelectedFeatures.setText("No features selected yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedFeatureTemplates.size(); i++) {
            PlanFeatureResponse item = selectedFeatureTemplates.get(i);
            sb.append("• ").append(buildFeatureLabel(item));
            if (i < selectedFeatureTemplates.size() - 1) {
                sb.append("\n");
            }
        }
        tvSelectedFeatures.setText(sb.toString());
    }

    private String buildFeatureLabel(PlanFeatureResponse item) {
        String featureName = item.getFeatureName() != null ? item.getFeatureName().trim() : "";
        String featureValue = item.getFeatureValue() != null ? item.getFeatureValue().trim() : "";
        String unit = item.getUnit() != null ? item.getUnit().trim() : "";

        StringBuilder sb = new StringBuilder();

        if (!featureName.isEmpty()) {
            sb.append(featureName);
        } else {
            sb.append("Feature");
        }

        if (!featureValue.isEmpty()) {
            sb.append(" - ").append(featureValue);
        }

        if (!unit.isEmpty()) {
            sb.append(" ").append(unit);
        }

        return sb.toString().trim();
    }

    private String buildFeatureKey(PlanFeatureResponse item) {
        String name = item.getFeatureName() != null ? item.getFeatureName().trim() : "";
        String value = item.getFeatureValue() != null ? item.getFeatureValue().trim() : "";
        String unit = item.getUnit() != null ? item.getUnit().trim() : "";
        Integer sortOrder = item.getSortOrder() != null ? item.getSortOrder() : -1;

        return name + "|" + value + "|" + unit + "|" + sortOrder;
    }

    private void savePlan() {
        String serviceTypeName = spinnerServiceType.getText() != null
                ? spinnerServiceType.getText().toString().trim()
                : "";
        String planName = etPlanName.getText() != null
                ? etPlanName.getText().toString().trim()
                : "";
        String monthlyPriceText = etMonthlyPrice.getText() != null
                ? etMonthlyPrice.getText().toString().trim()
                : "";
        String description = etDescription.getText() != null
                ? etDescription.getText().toString().trim()
                : "";
        String isActiveText = spinnerIsActive.getText() != null
                ? spinnerIsActive.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(serviceTypeName) || selectedServiceTypeId == null) {
            spinnerServiceType.setError("Service Type is required");
            spinnerServiceType.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(planName)) {
            etPlanName.setError("Plan name is required");
            etPlanName.requestFocus();
            return;
        }

        Double monthlyPrice = null;
        try {
            if (!TextUtils.isEmpty(monthlyPriceText)) {
                monthlyPrice = Double.parseDouble(monthlyPriceText);
            }
        } catch (NumberFormatException e) {
            etMonthlyPrice.setError("Monthly price must be a valid number");
            etMonthlyPrice.requestFocus();
            return;
        }

        if (monthlyPrice == null) {
            etMonthlyPrice.setError("Monthly price is required");
            etMonthlyPrice.requestFocus();
            return;
        }

        if (monthlyPrice <= 0) {
            etMonthlyPrice.setError("Price must be greater than 0");
            etMonthlyPrice.requestFocus();
            return;
        }

        if (monthlyPrice > 500) {
            etMonthlyPrice.setError("Price cannot exceed $500");
            etMonthlyPrice.requestFocus();
            return;
        }

        if (selectedContractTermMonths == null) {
            spinnerContractTerm.setError("Contract term is required");
            spinnerContractTerm.requestFocus();
            return;
        }

        Integer isActive = "Active".equalsIgnoreCase(isActiveText) ? 1 : 0;

        SavePlanRequest request = new SavePlanRequest(
                selectedServiceTypeId,
                planName,
                monthlyPrice,
                selectedContractTermMonths,
                TextUtils.isEmpty(description) ? null : description,
                isActive,
                null,
                null,
                null,
                null,
                null
        );

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if ("edit".equalsIgnoreCase(mode) && planId > 0) {
            apiService.updatePlanManager(planId, request).enqueue(new Callback<PlanResponse>() {
                @Override
                public void onResponse(Call<PlanResponse> call, Response<PlanResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        saveFeaturesForPlan(response.body().getPlanId() != null ? response.body().getPlanId() : planId);
                    } else {
                        showLoading(false);
                        Toast.makeText(PlanFormActivity.this,
                                "Update failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<PlanResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(PlanFormActivity.this,
                            "Unable to update plan",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            apiService.createPlanManager(request).enqueue(new Callback<PlanResponse>() {
                @Override
                public void onResponse(Call<PlanResponse> call, Response<PlanResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getPlanId() != null) {
                        saveFeaturesForPlan(response.body().getPlanId());
                    } else {
                        showLoading(false);
                        Toast.makeText(PlanFormActivity.this,
                                "Create failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<PlanResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(PlanFormActivity.this,
                            "Unable to create plan",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void saveFeaturesForPlan(int savedPlanId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if (selectedFeatureTemplates.isEmpty()) {
            showLoading(false);
            Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            return;
        }

        final int[] remaining = {selectedFeatureTemplates.size()};
        final boolean[] hasError = {false};

        for (PlanFeatureResponse template : selectedFeatureTemplates) {
            PlanFeatureCreateUpdateRequest request = new PlanFeatureCreateUpdateRequest(
                    savedPlanId,
                    template.getFeatureName(),
                    template.getFeatureValue(),
                    template.getUnit(),
                    template.getSortOrder()
            );

            apiService.createPlanFeatureForPlan(savedPlanId, request)
                    .enqueue(new Callback<PlanFeatureResponse>() {
                        @Override
                        public void onResponse(Call<PlanFeatureResponse> call,
                                               Response<PlanFeatureResponse> response) {
                            if (!response.isSuccessful()) {
                                hasError[0] = true;
                            }

                            remaining[0]--;
                            if (remaining[0] <= 0) {
                                finishSave(hasError[0]);
                            }
                        }

                        @Override
                        public void onFailure(Call<PlanFeatureResponse> call, Throwable t) {
                            hasError[0] = true;
                            remaining[0]--;
                            if (remaining[0] <= 0) {
                                finishSave(true);
                            }
                        }
                    });
        }
    }

    private void finishSave(boolean hasError) {
        showLoading(false);

        if (hasError) {
            Toast.makeText(this,
                    "Plan saved, but some features could not be saved",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
        btnAddFeature.setEnabled(!isLoading);
    }
}
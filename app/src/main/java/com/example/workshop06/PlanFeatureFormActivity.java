package com.example.workshop06;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.PlanFeatureCreateUpdateRequest;
import com.example.workshop06.model.PlanFeatureResponse;
import com.example.workshop06.model.PlanResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanFeatureFormActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView autoPlan;
    private EditText etFeatureName;
    private EditText etFeatureValue;
    private EditText etUnit;
    private EditText etSortOrder;

    private Button btnSave;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private ApiService apiService;

    private String mode = "add";
    private int featureId = -1;
    private Integer planId = null;

    private final List<PlanResponse> plans = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_feature_form);

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        initViews();
        loadIntentData();
        setupButtons();
        loadPlans();
    }

    private void initViews() {
        autoPlan = findViewById(R.id.autoPlan);
        etFeatureName = findViewById(R.id.etFeatureName);
        etFeatureValue = findViewById(R.id.etFeatureValue);
        etUnit = findViewById(R.id.etUnit);
        etSortOrder = findViewById(R.id.etSortOrder);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

    }

    private void loadIntentData() {
        if (getIntent() == null) return;

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if (getIntent().hasExtra("planId")) {
            int value = getIntent().getIntExtra("planId", -1);
            if (value > 0) {
                planId = value;
            }
        }

        if ("edit".equalsIgnoreCase(mode)) {
            featureId = getIntent().getIntExtra("featureId", -1);

            String featureName = getIntent().getStringExtra("featureName");
            String featureValue = getIntent().getStringExtra("featureValue");
            String unit = getIntent().getStringExtra("unit");
            Integer sortOrder = getNullableIntExtra("sortOrder");

            if (!TextUtils.isEmpty(featureName)) {
                etFeatureName.setText(featureName);
            }

            if (!TextUtils.isEmpty(featureValue)) {
                etFeatureValue.setText(featureValue);
            }

            if (!TextUtils.isEmpty(unit)) {
                etUnit.setText(unit);
            }

            if (sortOrder != null) {
                etSortOrder.setText(String.valueOf(sortOrder));
            }
        }
    }

    private Integer getNullableIntExtra(String key) {
        if (!getIntent().hasExtra(key)) return null;
        int value = getIntent().getIntExtra(key, Integer.MIN_VALUE);
        return value == Integer.MIN_VALUE ? null : value;
    }

    private void setupButtons() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnSave.setOnClickListener(v -> savePlanFeature());
    }

    private void loadPlans() {
        apiService.getPlansManager().enqueue(new Callback<List<PlanResponse>>() {
            @Override
            public void onResponse(Call<List<PlanResponse>> call, Response<List<PlanResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(PlanFeatureFormActivity.this,
                            "Unable to load plans",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                plans.clear();
                plans.addAll(response.body());

                List<String> displayNames = new ArrayList<>();
                int selectedIndex = -1;

                for (int i = 0; i < plans.size(); i++) {
                    PlanResponse plan = plans.get(i);
                    displayNames.add(getPlanDisplayName(plan));

                    if (planId != null && plan.getPlanId() != null && planId.equals(plan.getPlanId())) {
                        selectedIndex = i;
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        PlanFeatureFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        displayNames
                );

                autoPlan.setAdapter(adapter);
                autoPlan.setThreshold(1);

                autoPlan.setOnItemClickListener((parent, view, position, id) -> {
                    PlanResponse selected = plans.get(position);
                    planId = selected.getPlanId();
                    autoPlan.setError(null);
                });

                if (selectedIndex >= 0) {
                    autoPlan.setText(displayNames.get(selectedIndex), false);
                }

                if ("edit".equalsIgnoreCase(mode)) {
                    lockPlanField();
                }
            }

            @Override
            public void onFailure(Call<List<PlanResponse>> call, Throwable t) {
                Toast.makeText(PlanFeatureFormActivity.this,
                        "Unable to load plans",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void lockPlanField() {
        autoPlan.setEnabled(false);
        autoPlan.setFocusable(false);
        autoPlan.setFocusableInTouchMode(false);
        autoPlan.setCursorVisible(false);
        autoPlan.setKeyListener(null);
        autoPlan.setAlpha(0.85f);
    }

    private void savePlanFeature() {
        String featureName = etFeatureName.getText().toString().trim();
        String featureValue = etFeatureValue.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String sortOrderText = etSortOrder.getText().toString().trim();

        if (planId == null || planId <= 0) {
            autoPlan.setError("Please select a plan");
            autoPlan.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(featureName)) {
            etFeatureName.setError("Feature name is required");
            etFeatureName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(featureValue)) {
            etFeatureValue.setError("Feature value is required");
            etFeatureValue.requestFocus();
            return;
        }

        Integer sortOrder = null;
        if (!TextUtils.isEmpty(sortOrderText)) {
            try {
                sortOrder = Integer.parseInt(sortOrderText);
            } catch (NumberFormatException e) {
                etSortOrder.setError("Sort order must be a number");
                etSortOrder.requestFocus();
                return;
            }
        }

        if (TextUtils.isEmpty(unit)) {
            unit = null;
        }

        PlanFeatureCreateUpdateRequest request = new PlanFeatureCreateUpdateRequest(
                planId,
                featureName,
                featureValue,
                unit,
                sortOrder
        );

        showLoading(true);

        if ("edit".equalsIgnoreCase(mode) && featureId > 0) {
            apiService.updatePlanFeature(featureId, request).enqueue(new Callback<PlanFeatureResponse>() {
                @Override
                public void onResponse(Call<PlanFeatureResponse> call, Response<PlanFeatureResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(PlanFeatureFormActivity.this,
                                "Updated successfully",
                                Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(PlanFeatureFormActivity.this,
                                "Update failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<PlanFeatureResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(PlanFeatureFormActivity.this,
                            "Unable to update plan feature",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            apiService.createPlanFeature(request).enqueue(new Callback<PlanFeatureResponse>() {
                @Override
                public void onResponse(Call<PlanFeatureResponse> call, Response<PlanFeatureResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(PlanFeatureFormActivity.this,
                                "Created successfully",
                                Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(PlanFeatureFormActivity.this,
                                "Create failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<PlanFeatureResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(PlanFeatureFormActivity.this,
                            "Unable to create plan feature",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnSave != null) {
            btnSave.setEnabled(!isLoading);
        }
    }

    private String getPlanDisplayName(PlanResponse plan) {
        if (plan == null) return "";

        String name = null;
        try {
            name = plan.getPlanName();
        } catch (Exception ignored) {
        }

        if (!TextUtils.isEmpty(name)) {
            return name;
        }

        String description = null;
        try {
            description = plan.getDescription();
        } catch (Exception ignored) {
        }

        if (!TextUtils.isEmpty(description)) {
            return description;
        }

        Integer id = null;
        try {
            id = plan.getPlanId();
        } catch (Exception ignored) {
        }

        return id != null ? "Plan #" + id : "Plan";
    }
}
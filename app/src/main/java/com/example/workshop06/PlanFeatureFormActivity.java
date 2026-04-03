package com.example.workshop06;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanFeatureFormActivity extends AppCompatActivity {

    private EditText etPlanId;
    private EditText etFeatureName;
    private EditText etFeatureValue;
    private EditText etUnit;
    private EditText etSortOrder;
    private Button btnSave;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private String mode = "add";
    private int featureId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_feature_form);

        initViews();
        loadIntentData();
        setupButtons();
    }

    private void initViews() {
        etPlanId = findViewById(R.id.etPlanId);
        etFeatureName = findViewById(R.id.etFeatureName);
        etFeatureValue = findViewById(R.id.etFeatureValue);
        etUnit = findViewById(R.id.etUnit);
        etSortOrder = findViewById(R.id.etSortOrder);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadIntentData() {
        if (getIntent() == null) return;

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if ("edit".equalsIgnoreCase(mode)) {
            featureId = getIntent().getIntExtra("featureId", -1);

            int planId = getIntent().getIntExtra("planId", Integer.MIN_VALUE);
            int sortOrder = getIntent().getIntExtra("sortOrder", Integer.MIN_VALUE);

            String featureName = getIntent().getStringExtra("featureName");
            String featureValue = getIntent().getStringExtra("featureValue");
            String unit = getIntent().getStringExtra("unit");

            if (planId != Integer.MIN_VALUE) etPlanId.setText(String.valueOf(planId));
            if (sortOrder != Integer.MIN_VALUE) etSortOrder.setText(String.valueOf(sortOrder));
            if (featureName != null) etFeatureName.setText(featureName);
            if (featureValue != null) etFeatureValue.setText(featureValue);
            if (unit != null) etUnit.setText(unit);
        }
    }

    private void setupButtons() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> savePlanFeature());
        }
    }

    private void savePlanFeature() {
        String planIdText = etPlanId.getText().toString().trim();
        String featureName = etFeatureName.getText().toString().trim();
        String featureValue = etFeatureValue.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String sortOrderText = etSortOrder.getText().toString().trim();

        if (TextUtils.isEmpty(planIdText)) {
            etPlanId.setError("Plan ID is required");
            etPlanId.requestFocus();
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

        Integer planId = Integer.parseInt(planIdText);
        Integer sortOrder = TextUtils.isEmpty(sortOrderText) ? null : Integer.parseInt(sortOrderText);

        PlanFeatureCreateUpdateRequest request = new PlanFeatureCreateUpdateRequest(
                planId,
                featureName,
                featureValue,
                TextUtils.isEmpty(unit) ? null : unit,
                sortOrder
        );

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if ("edit".equalsIgnoreCase(mode) && featureId > 0) {
            apiService.updatePlanFeature(featureId, request).enqueue(new Callback<PlanFeatureResponse>() {
                @Override
                public void onResponse(Call<PlanFeatureResponse> call, Response<PlanFeatureResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(PlanFeatureFormActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(PlanFeatureFormActivity.this, "Created successfully", Toast.LENGTH_SHORT).show();
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
}
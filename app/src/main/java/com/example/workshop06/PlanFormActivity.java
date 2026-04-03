package com.example.workshop06;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.PlanResponse;
import com.example.workshop06.model.SavePlanRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanFormActivity extends AppCompatActivity {

    private EditText etServiceTypeId, etPlanName, etMonthlyPrice, etContractTermMonths,
            etDescription, etTagline, etBadge, etIconKey, etThemeKey, etDataLabel;
    private Spinner spinnerIsActive;
    private Button btnSave;

    private ProgressBar progressBar;

    private String mode = "add";
    private int planId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_form);

        initViews();
        setupSpinner();
        loadIntentData();
        setupButtons();
    }

    private void initViews() {
        etServiceTypeId = findViewById(R.id.etServiceTypeId);
        etPlanName = findViewById(R.id.etPlanName);
        etMonthlyPrice = findViewById(R.id.etMonthlyPrice);
        etContractTermMonths = findViewById(R.id.etContractTermMonths);
        etDescription = findViewById(R.id.etDescription);
        etTagline = findViewById(R.id.etTagline);
        etBadge = findViewById(R.id.etBadge);
        etIconKey = findViewById(R.id.etIconKey);
        etThemeKey = findViewById(R.id.etThemeKey);
        etDataLabel = findViewById(R.id.etDataLabel);
        spinnerIsActive = findViewById(R.id.spinnerIsActive);
        btnSave = findViewById(R.id.btnSave);

        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"1", "0"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIsActive.setAdapter(adapter);
    }

    private void loadIntentData() {
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if ("edit".equalsIgnoreCase(mode)) {
            planId = getIntent().getIntExtra("planId", -1);

            int serviceTypeId = getIntent().getIntExtra("serviceTypeId", Integer.MIN_VALUE);
            double monthlyPrice = getIntent().getDoubleExtra("monthlyPrice", Double.NaN);
            int contractTermMonths = getIntent().getIntExtra("contractTermMonths", Integer.MIN_VALUE);
            int isActive = getIntent().getIntExtra("isActive", Integer.MIN_VALUE);

            if (serviceTypeId != Integer.MIN_VALUE) etServiceTypeId.setText(String.valueOf(serviceTypeId));
            if (!Double.isNaN(monthlyPrice)) etMonthlyPrice.setText(String.valueOf(monthlyPrice));
            if (contractTermMonths != Integer.MIN_VALUE) etContractTermMonths.setText(String.valueOf(contractTermMonths));

            etPlanName.setText(getIntent().getStringExtra("planName"));
            etDescription.setText(getIntent().getStringExtra("description"));
            etTagline.setText(getIntent().getStringExtra("tagline"));
            etBadge.setText(getIntent().getStringExtra("badge"));
            etIconKey.setText(getIntent().getStringExtra("iconKey"));
            etThemeKey.setText(getIntent().getStringExtra("themeKey"));
            etDataLabel.setText(getIntent().getStringExtra("dataLabel"));

            if (isActive != Integer.MIN_VALUE) {
                setSpinnerValue(String.valueOf(isActive));
            }
        }
    }

    private void setSpinnerValue(String value) {
        if (value == null) return;
        for (int i = 0; i < spinnerIsActive.getCount(); i++) {
            String item = String.valueOf(spinnerIsActive.getItemAtPosition(i));
            if (item.equalsIgnoreCase(value)) {
                spinnerIsActive.setSelection(i);
                break;
            }
        }
    }

    private void setupButtons() {

        btnSave.setOnClickListener(v -> savePlan());
    }

    private void savePlan() {
        String serviceTypeIdText = etServiceTypeId.getText().toString().trim();
        String planName = etPlanName.getText().toString().trim();
        String monthlyPriceText = etMonthlyPrice.getText().toString().trim();
        String contractTermMonthsText = etContractTermMonths.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String isActiveText = spinnerIsActive.getSelectedItem().toString();
        String tagline = etTagline.getText().toString().trim();
        String badge = etBadge.getText().toString().trim();
        String iconKey = etIconKey.getText().toString().trim();
        String themeKey = etThemeKey.getText().toString().trim();
        String dataLabel = etDataLabel.getText().toString().trim();

        if (TextUtils.isEmpty(serviceTypeIdText)) {
            etServiceTypeId.setError("Service Type ID is required");
            etServiceTypeId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(planName)) {
            etPlanName.setError("Plan name is required");
            etPlanName.requestFocus();
            return;
        }

        Integer serviceTypeId = Integer.parseInt(serviceTypeIdText);
        Double monthlyPrice = TextUtils.isEmpty(monthlyPriceText) ? null : Double.parseDouble(monthlyPriceText);
        Integer contractTermMonths = TextUtils.isEmpty(contractTermMonthsText) ? null : Integer.parseInt(contractTermMonthsText);
        Integer isActive = TextUtils.isEmpty(isActiveText) ? null : Integer.parseInt(isActiveText);

        SavePlanRequest request = new SavePlanRequest(
                serviceTypeId,
                planName,
                monthlyPrice,
                contractTermMonths,
                TextUtils.isEmpty(description) ? null : description,
                isActive,
                TextUtils.isEmpty(tagline) ? null : tagline,
                TextUtils.isEmpty(badge) ? null : badge,
                TextUtils.isEmpty(iconKey) ? null : iconKey,
                TextUtils.isEmpty(themeKey) ? null : themeKey,
                TextUtils.isEmpty(dataLabel) ? null : dataLabel
        );

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if ("edit".equalsIgnoreCase(mode) && planId > 0) {
            apiService.updatePlanManager(planId, request).enqueue(new Callback<PlanResponse>() {
                @Override
                public void onResponse(Call<PlanResponse> call, Response<PlanResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(PlanFormActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
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
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(PlanFormActivity.this, "Created successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
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

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        btnSave.setEnabled(!isLoading);
    }
}
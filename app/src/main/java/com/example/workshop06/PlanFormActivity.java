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
import com.example.workshop06.model.PlanResponse;
import com.example.workshop06.model.SavePlanRequest;
import com.example.workshop06.model.ServiceTypeResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanFormActivity extends AppCompatActivity {

    private EditText etPlanName;
    private EditText etMonthlyPrice;
    private EditText etDescription;

    private MaterialAutoCompleteTextView spinnerServiceType;
    private MaterialAutoCompleteTextView spinnerIsActive;

    private Button btnSave;
    private ProgressBar progressBar;

    private String mode = "add";
    private int planId = -1;

    private ImageButton btnBack;

    private final List<ServiceTypeResponse> serviceTypeList = new ArrayList<>();
    private Integer selectedServiceTypeId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_form);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        initViews();
        setupStatusDropdown();
        loadIntentData();
        loadServiceTypes();
        setupButtons();
    }

    private void initViews() {
        spinnerServiceType = findViewById(R.id.spinnerServiceType);
        spinnerIsActive = findViewById(R.id.spinnerIsActive);

        etPlanName = findViewById(R.id.etPlanName);
        etMonthlyPrice = findViewById(R.id.etMonthlyPrice);
        etDescription = findViewById(R.id.etDescription);

        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
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

            if (serviceTypeId != Integer.MIN_VALUE) {
                selectedServiceTypeId = serviceTypeId;
            }

            if (!Double.isNaN(monthlyPrice)) {
                etMonthlyPrice.setText(String.format(Locale.US, "%.2f", monthlyPrice));
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

    private void setupButtons() {
        btnSave.setOnClickListener(v -> savePlan());
    }

    private void savePlan() {
        String serviceTypeName = spinnerServiceType.getText() != null
                ? spinnerServiceType.getText().toString().trim()
                : "";
        String planName = etPlanName.getText().toString().trim();
        String monthlyPriceText = etMonthlyPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
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

        Integer isActive = "Active".equalsIgnoreCase(isActiveText) ? 1 : 0;

        SavePlanRequest request = new SavePlanRequest(
                selectedServiceTypeId,
                planName,
                monthlyPrice,
                null,
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
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
    }
}
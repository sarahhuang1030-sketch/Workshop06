package com.example.workshop06;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.example.workshop06.model.ServiceRequestCreateUpdateRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceRequestFormActivity extends AppCompatActivity {

    private EditText etCustomerName;
    private EditText etCreatedByName;
    private EditText etAssignedTechnicianName;

    private EditText etCustomerId;
    private EditText etCreatedByUserId;
    private EditText etAssignedTechnicianUserId;
    private EditText etParentRequestId;
    private EditText etDescription;
    private EditText etRequestType;
    private Spinner spinnerPriority;
    private Spinner spinnerStatus;
    private Button btnSave;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private String mode = "add";
    private int requestId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_request_form);

        initViews();
        setupSpinners();
        loadIntentData();
        setupButtons();
    }

    private void initViews() {
        etCustomerName = findViewById(R.id.etCustomerName);
        etCreatedByName = findViewById(R.id.etCreatedByName);
        etAssignedTechnicianName = findViewById(R.id.etAssignedTechnicianName);

        etCustomerId = findViewById(R.id.etCustomerId);
        etCreatedByUserId = findViewById(R.id.etCreatedByUserId);
        etAssignedTechnicianUserId = findViewById(R.id.etAssignedTechnicianUserId);

        etParentRequestId = findViewById(R.id.etParentRequestId);
        etDescription = findViewById(R.id.etDescription);
        etRequestType = findViewById(R.id.etRequestType);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Low", "Medium", "High"}
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Open", "In Progress", "Closed", "Pending"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void loadIntentData() {
        if (getIntent() == null) return;

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        Integer customerId = getNullableIntExtra("customerId");
        Integer createdByUserId = getNullableIntExtra("createdByUserId");
        Integer assignedTechnicianUserId = getNullableIntExtra("assignedTechnicianUserId");

        String customerName = getIntent().getStringExtra("customerName");
        String createdByName = getIntent().getStringExtra("createdByName");
        String technicianName = getIntent().getStringExtra("technicianName");

        if (customerId != null) etCustomerId.setText(String.valueOf(customerId));
        if (createdByUserId != null) etCreatedByUserId.setText(String.valueOf(createdByUserId));
        if (assignedTechnicianUserId != null) {
            etAssignedTechnicianUserId.setText(String.valueOf(assignedTechnicianUserId));
        }

        etCustomerName.setText(!TextUtils.isEmpty(customerName) ? customerName : "—");
        etCreatedByName.setText(!TextUtils.isEmpty(createdByName) ? createdByName : "—");
        etAssignedTechnicianName.setText(!TextUtils.isEmpty(technicianName) ? technicianName : "—");

        if ("edit".equalsIgnoreCase(mode)) {
            requestId = getIntent().getIntExtra("requestId", -1);

            String requestType = getIntent().getStringExtra("requestType");
            String priority = getIntent().getStringExtra("priority");
            String status = getIntent().getStringExtra("status");
            String description = getIntent().getStringExtra("description");
            Integer parentRequestId = getNullableIntExtra("parentRequestId");

            if (requestType != null) etRequestType.setText(requestType);
            if (description != null) etDescription.setText(description);
            if (parentRequestId != null) etParentRequestId.setText(String.valueOf(parentRequestId));

            setSpinnerValue(spinnerPriority, priority);
            setSpinnerValue(spinnerStatus, status);
        }
    }

    private Integer getNullableIntExtra(String key) {
        if (!getIntent().hasExtra(key)) return null;
        int value = getIntent().getIntExtra(key, Integer.MIN_VALUE);
        return value == Integer.MIN_VALUE ? null : value;
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;

        for (int i = 0; i < spinner.getCount(); i++) {
            String item = String.valueOf(spinner.getItemAtPosition(i));
            if (item.equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupButtons() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveServiceRequest());
        }
    }

    private void saveServiceRequest() {
        String customerIdText = etCustomerId.getText().toString().trim();
        String createdByUserIdText = etCreatedByUserId.getText().toString().trim();
        String assignedTechnicianUserIdText = etAssignedTechnicianUserId.getText().toString().trim();
        String parentRequestIdText = etParentRequestId.getText().toString().trim();
        String requestType = etRequestType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priority = spinnerPriority.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();

        if (TextUtils.isEmpty(customerIdText)) {
            Toast.makeText(this, "Customer is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(createdByUserIdText)) {
            Toast.makeText(this, "Created by user is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(requestType)) {
            etRequestType.setError("Request type is required");
            etRequestType.requestFocus();
            return;
        }

        Integer customerId = Integer.parseInt(customerIdText);
        Integer createdByUserId = Integer.parseInt(createdByUserIdText);
        Integer assignedTechnicianUserId = TextUtils.isEmpty(assignedTechnicianUserIdText)
                ? null : Integer.parseInt(assignedTechnicianUserIdText);
        Integer parentRequestId = TextUtils.isEmpty(parentRequestIdText)
                ? null : Integer.parseInt(parentRequestIdText);

        ServiceRequestCreateUpdateRequest request = new ServiceRequestCreateUpdateRequest(
                customerId,
                createdByUserId,
                assignedTechnicianUserId,
                parentRequestId,
                requestType,
                priority,
                status,
                description
        );

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if ("edit".equalsIgnoreCase(mode) && requestId > 0) {
            apiService.updateServiceRequest(requestId, request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(ServiceRequestFormActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(ServiceRequestFormActivity.this,
                                "Update failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(ServiceRequestFormActivity.this,
                            "Unable to update service request",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            apiService.createServiceRequest(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(ServiceRequestFormActivity.this, "Created successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(ServiceRequestFormActivity.this,
                                "Create failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(ServiceRequestFormActivity.this,
                            "Unable to create service request",
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
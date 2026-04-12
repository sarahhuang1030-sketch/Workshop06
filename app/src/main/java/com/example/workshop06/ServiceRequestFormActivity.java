package com.example.workshop06;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.workshop06.model.CustomerResponse;
import com.example.workshop06.model.EmployeeResponse;
import com.example.workshop06.model.MeResponse;
import com.example.workshop06.model.ServiceRequestCreateUpdateRequest;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceRequestFormActivity extends AppCompatActivity {

    private static final String TAG = "ServiceRequestForm";

    private MaterialAutoCompleteTextView etCustomerName;
    private EditText etCreatedByName;
    private MaterialAutoCompleteTextView etAssignedTechnicianName;

    private EditText etCustomerId;
    private EditText etCreatedByUserId;
    private EditText etAssignedTechnicianUserId;

    private EditText etDescription;
    private EditText etRequestType;

    private MaterialAutoCompleteTextView spinnerPriority;
    private MaterialAutoCompleteTextView spinnerStatus;

    private Button btnSave;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private String mode = "add";
    private int requestId = -1;

    private final List<CustomerResponse> customerList = new ArrayList<>();
    private final List<EmployeeResponse> technicianList = new ArrayList<>();

    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_request_form);

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        initViews();
        setupDropdowns();
        loadIntentData();
        setupButtons();

        if (isEditMode()) {
            lockLookupFields();
            loadEmployees();
        } else {
            loadCustomers();
            loadEmployees();
            loadCurrentUser();
        }
    }

    private void initViews() {
        etCustomerName = findViewById(R.id.etCustomerName);
        etCreatedByName = findViewById(R.id.etCreatedByName);
        etAssignedTechnicianName = findViewById(R.id.etAssignedTechnicianName);

        etCustomerId = findViewById(R.id.etCustomerId);
        etCreatedByUserId = findViewById(R.id.etCreatedByUserId);
        etAssignedTechnicianUserId = findViewById(R.id.etAssignedTechnicianUserId);

        etDescription = findViewById(R.id.etDescription);
        etRequestType = findViewById(R.id.etRequestType);

        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerStatus = findViewById(R.id.spinnerStatus);

        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupDropdowns() {
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Low", "Medium", "High"}
        );
        spinnerPriority.setAdapter(priorityAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Open", "Assigned", "In Progress", "Completed", "Cancelled"}
        );
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

        if (!TextUtils.isEmpty(customerName)) {
            etCustomerName.setText(customerName, false);
        }
        if (!TextUtils.isEmpty(createdByName)) {
            etCreatedByName.setText(createdByName);
        }
        if (!TextUtils.isEmpty(technicianName)) {
            etAssignedTechnicianName.setText(technicianName, false);
        }

        if (isEditMode()) {
            requestId = getIntent().getIntExtra("requestId", -1);

            String requestType = getIntent().getStringExtra("requestType");
            String priority = getIntent().getStringExtra("priority");
            String status = getIntent().getStringExtra("status");
            String description = getIntent().getStringExtra("description");

            if (!TextUtils.isEmpty(requestType)) {
                etRequestType.setText(requestType);
            }
            if (!TextUtils.isEmpty(description)) {
                etDescription.setText(description);
            }
            if (!TextUtils.isEmpty(priority)) {
                spinnerPriority.setText(priority, false);
            }
            if (!TextUtils.isEmpty(status)) {
                spinnerStatus.setText(status, false);
            }
        }
    }

    private boolean isEditMode() {
        return "edit".equalsIgnoreCase(mode);
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

        btnSave.setOnClickListener(v -> saveServiceRequest());
    }

    private void loadCustomers() {
        apiService.getCustomers().enqueue(new Callback<List<CustomerResponse>>() {
            @Override
            public void onResponse(Call<List<CustomerResponse>> call, Response<List<CustomerResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ServiceRequestFormActivity.this, "Unable to load customers", Toast.LENGTH_SHORT).show();
                    return;
                }

                customerList.clear();
                customerList.addAll(response.body());

                List<String> displayNames = new ArrayList<>();
                for (CustomerResponse customer : customerList) {
                    displayNames.add(getCustomerDisplayName(customer));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ServiceRequestFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        displayNames
                );

                etCustomerName.setAdapter(adapter);
                etCustomerName.setThreshold(1);

                etCustomerName.setOnItemClickListener((parent, view, position, id) -> {
                    CustomerResponse selected = customerList.get(position);
                    etCustomerId.setText(String.valueOf(selected.getCustomerId()));
                    etCustomerName.setError(null);
                });
            }

            @Override
            public void onFailure(Call<List<CustomerResponse>> call, Throwable t) {
                Toast.makeText(ServiceRequestFormActivity.this, "Unable to load customers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEmployees() {
        apiService.getEmployees().enqueue(new Callback<List<EmployeeResponse>>() {
            @Override
            public void onResponse(Call<List<EmployeeResponse>> call, Response<List<EmployeeResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ServiceRequestFormActivity.this, "Unable to load employees", Toast.LENGTH_SHORT).show();
                    return;
                }

                technicianList.clear();

                for (EmployeeResponse employee : response.body()) {
                    boolean technician = isTechnician(employee);

                    Log.d(TAG,
                            "Employee="
                                    + getEmployeeDisplayName(employee)
                                    + " roleId=" + employee.getRoleId()
                                    + " role=" + employee.getRole()
                                    + " roleName=" + employee.getRoleName()
                                    + " positionTitle=" + employee.getPositionTitle()
                                    + " technician=" + technician);

                    if (technician) {
                        technicianList.add(employee);
                    }
                }

                bindTechnicianDropdown();
                Log.d(TAG, "technicianList size=" + technicianList.size());
            }

            @Override
            public void onFailure(Call<List<EmployeeResponse>> call, Throwable t) {
                Toast.makeText(ServiceRequestFormActivity.this, "Unable to load employees", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurrentUser() {
        String token = "Bearer " + getSharedPreferences("teleconnect_prefs", MODE_PRIVATE)
                .getString("jwt_token", "");

        apiService.getMe(token).enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ServiceRequestFormActivity.this,
                            "Unable to load current user",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                MeResponse me = response.body();

                String fullName = me.getFirstName() + " " + me.getLastName();

                etCreatedByName.setText(fullName);
                etCreatedByUserId.setText(String.valueOf(me.getEmployeeId()));

                lockEditText(etCreatedByName);
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                Toast.makeText(ServiceRequestFormActivity.this,
                        "Unable to load current user",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isTechnician(EmployeeResponse employee) {
        if (employee == null) return false;

        Integer roleId = employee.getRoleId();
        if (roleId != null && roleId == 3) {
            return true;
        }

        String role = safeLower(employee.getRole());
        String roleName = safeLower(employee.getRoleName());
        String positionTitle = safeLower(employee.getPositionTitle());

        return role.contains("technician")
                || role.equals("tech")
                || roleName.contains("technician")
                || roleName.equals("tech")
                || positionTitle.contains("technician")
                || positionTitle.equals("tech");
    }

    private void bindTechnicianDropdown() {
        List<String> displayNames = new ArrayList<>();
        for (EmployeeResponse employee : technicianList) {
            displayNames.add(getEmployeeDisplayName(employee));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                displayNames
        );

        etAssignedTechnicianName.setAdapter(adapter);
        etAssignedTechnicianName.setThreshold(1);

        etAssignedTechnicianName.setOnItemClickListener((parent, view, position, id) -> {
            EmployeeResponse selected = technicianList.get(position);
            etAssignedTechnicianUserId.setText(String.valueOf(selected.getEmployeeId()));
            etAssignedTechnicianName.setError(null);
        });

        if (technicianList.isEmpty()) {
            etAssignedTechnicianName.setEnabled(false);
            etAssignedTechnicianName.setHint("No technicians found");
            Log.e(TAG, "No technicians found from employee API response");
        } else {
            etAssignedTechnicianName.setEnabled(true);
        }
    }

    private void lockLookupFields() {
        lockField(etCustomerName);
        lockEditText(etCreatedByName);
        lockField(etAssignedTechnicianName);
    }

    private void lockField(MaterialAutoCompleteTextView field) {
        field.setEnabled(false);
        field.setFocusable(false);
        field.setFocusableInTouchMode(false);
        field.setCursorVisible(false);
        field.setKeyListener(null);
        field.setAlpha(0.85f);
    }

    private void lockEditText(EditText field) {
        field.setEnabled(false);
        field.setFocusable(false);
        field.setFocusableInTouchMode(false);
        field.setCursorVisible(false);
        field.setKeyListener(null);
        field.setAlpha(0.85f);
    }

    private void saveServiceRequest() {
        String customerIdText = etCustomerId.getText().toString().trim();
        String createdByUserIdText = etCreatedByUserId.getText().toString().trim();
        String assignedTechnicianUserIdText = etAssignedTechnicianUserId.getText().toString().trim();
        String requestType = etRequestType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priority = spinnerPriority.getText().toString().trim();
        String status = spinnerStatus.getText().toString().trim();

        if (TextUtils.isEmpty(customerIdText)) {
            etCustomerName.setError("Select a customer");
            etCustomerName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(createdByUserIdText)) {
            etCreatedByName.setError("Created by is missing");
            etCreatedByName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(requestType)) {
            etRequestType.setError("Request type is required");
            etRequestType.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priority)) {
            spinnerPriority.setError("Select priority");
            spinnerPriority.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(status)) {
            spinnerStatus.setError("Select status");
            spinnerStatus.requestFocus();
            return;
        }

        Integer customerId = Integer.parseInt(customerIdText);
        Integer createdByUserId = Integer.parseInt(createdByUserIdText);
        Integer assignedTechnicianUserId = TextUtils.isEmpty(assignedTechnicianUserIdText)
                ? null
                : Integer.parseInt(assignedTechnicianUserIdText);

        ServiceRequestCreateUpdateRequest request = new ServiceRequestCreateUpdateRequest(
                customerId,
                createdByUserId,
                assignedTechnicianUserId,
                requestType,
                priority,
                status,
                description
        );

        showLoading(true);

        if (isEditMode() && requestId > 0) {
            apiService.updateServiceRequest(requestId, request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(ServiceRequestFormActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(
                                ServiceRequestFormActivity.this,
                                "Update failed. Code: " + response.code(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(
                            ServiceRequestFormActivity.this,
                            "Unable to update service request",
                            Toast.LENGTH_LONG
                    ).show();
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
                        Toast.makeText(
                                ServiceRequestFormActivity.this,
                                "Create failed. Code: " + response.code(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(
                            ServiceRequestFormActivity.this,
                            "Unable to create service request",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
    }

    private String getCustomerDisplayName(CustomerResponse customer) {
        if (customer == null) return "";

        if (!TextUtils.isEmpty(customer.getBusinessName())) {
            return customer.getBusinessName();
        }

        String fullName = (safe(customer.getFirstName()) + " " + safe(customer.getLastName())).trim();
        if (!TextUtils.isEmpty(fullName)) {
            return fullName;
        }

        return "Customer #" + customer.getCustomerId();
    }

    private String getEmployeeDisplayName(EmployeeResponse employee) {
        if (employee == null) return "";

        String fullName = (safe(employee.getFirstName()) + " " + safe(employee.getLastName())).trim();
        if (!TextUtils.isEmpty(fullName)) {
            return fullName;
        }

        return "Employee #" + employee.getEmployeeId();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }
}
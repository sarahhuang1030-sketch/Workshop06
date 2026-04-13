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
import com.example.workshop06.model.CreateCustomerRequest;
import com.example.workshop06.model.CreateCustomerResponse;
import com.example.workshop06.model.CustomerResponse;
import com.example.workshop06.model.EmployeeResponse;
import com.example.workshop06.util.FormFormatUtils;
import com.example.workshop06.util.ValidationUtils;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerFormActivity extends BaseActivity {

    private MaterialAutoCompleteTextView spinnerCustomerType;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etBusinessName;
    private EditText etEmail;
    private EditText etHomePhone;
    private MaterialAutoCompleteTextView spinnerStatus;
    private Button btnSave;
    private ProgressBar progressBar;

    private MaterialAutoCompleteTextView spinnerAssignedAgent;
    private List<EmployeeResponse> agentList = new ArrayList<>();

    private String mode = "add";
    private int customerId = -1;

    private ImageButton btnBack;


    private Integer existingAssignedEmployeeId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_form);
        btnBack = findViewById(R.id.btnBack);
        initViews();
        setupSpinners();
        attachFormatters();
        loadIntentData();
        setupTypeBehavior();
        updateCustomerTypeUI();
        loadAgents();
        setupButtons();
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        spinnerCustomerType = findViewById(R.id.spinnerCustomerType);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBusinessName = findViewById(R.id.etBusinessName);
        etEmail = findViewById(R.id.etEmail);
        etHomePhone = findViewById(R.id.etHomePhone);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        spinnerAssignedAgent = findViewById(R.id.spinnerAssignedAgent);

    }

    private void setupSpinners() {
        String[] customerTypes = {"Individual", "Business"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                customerTypes
        );
        spinnerCustomerType.setAdapter(typeAdapter);
        spinnerCustomerType.setText(customerTypes[0], false);

        String[] statuses = {"Active", "Inactive"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                statuses
        );
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setText(statuses[0], false);
    }

    private void attachFormatters() {
        FormFormatUtils.attachCanadianPhoneFormatter(etHomePhone);
    }

    private void loadIntentData() {
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if ("edit".equalsIgnoreCase(mode)) {
            customerId = getIntent().getIntExtra("customerId", -1);
            if (getIntent().hasExtra("assignedEmployeeId")) {
                int value = getIntent().getIntExtra("assignedEmployeeId", -1);
                existingAssignedEmployeeId = (value == -1) ? null : value;
            }
            setDropdownValue(spinnerCustomerType, getIntent().getStringExtra("customerType"));
            etFirstName.setText(getIntent().getStringExtra("firstName"));
            etLastName.setText(getIntent().getStringExtra("lastName"));
            etBusinessName.setText(getIntent().getStringExtra("businessName"));
            etEmail.setText(getIntent().getStringExtra("email"));
            etHomePhone.setText(getIntent().getStringExtra("homePhone"));
            setDropdownValue(spinnerStatus, getIntent().getStringExtra("status"));
        }
    }

    private void setDropdownValue(MaterialAutoCompleteTextView dropdown, String value) {
        if (value == null || value.trim().isEmpty()) return;
        dropdown.setText(value, false);
    }

    private void setupTypeBehavior() {
        spinnerCustomerType.setOnItemClickListener((parent, view, position, id) -> updateCustomerTypeUI());
    }

    private void updateCustomerTypeUI() {
        String customerType = spinnerCustomerType.getText() != null
                ? spinnerCustomerType.getText().toString().trim()
                : "Individual";

        boolean isBusiness = "Business".equalsIgnoreCase(customerType);

        if (isBusiness) {
            etBusinessName.setVisibility(View.VISIBLE);
            etBusinessName.setEnabled(true);
            etBusinessName.setError(null);

            etFirstName.setEnabled(true);
            etLastName.setEnabled(true);
        } else {
            etBusinessName.setText("");
            etBusinessName.setError(null);
            etBusinessName.setEnabled(false);
            etBusinessName.setVisibility(View.GONE);

            etFirstName.setEnabled(true);
            etLastName.setEnabled(true);
            etFirstName.setError(null);
            etLastName.setError(null);
        }
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> saveCustomer());
    }

    private boolean validateForm() {
        String customerType = spinnerCustomerType.getText() != null
                ? spinnerCustomerType.getText().toString().trim()
                : "Individual";

        boolean isBusiness = "Business".equalsIgnoreCase(customerType);

        if (spinnerCustomerType.getText() == null ||
                spinnerCustomerType.getText().toString().trim().isEmpty()) {
            spinnerCustomerType.setError("Customer type is required");
            return false;
        } else {
            spinnerCustomerType.setError(null);
        }

        if (!ValidationUtils.email(etEmail)) {
            return false;
        }

        if (!ValidationUtils.phone(etHomePhone)) {
            return false;
        }

        if (spinnerStatus.getText() == null ||
                spinnerStatus.getText().toString().trim().isEmpty()) {
            spinnerStatus.setError("Status is required");
            return false;
        } else {
            spinnerStatus.setError(null);
        }

        if (isBusiness) {
            if (!ValidationUtils.required(etBusinessName, "Business name is required for business customer")) {
                return false;
            }
        } else {
            if (!ValidationUtils.required(etFirstName, "First name is required for individual customer")) {
                return false;
            }

            if (!ValidationUtils.required(etLastName, "Last name is required for individual customer")) {
                return false;
            }
        }

        return true;
    }

    private void saveCustomer() {
        if (!validateForm()) {
            return;
        }

        String customerType = spinnerCustomerType.getText().toString().trim();
        boolean isBusiness = "Business".equalsIgnoreCase(customerType);

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String businessName = etBusinessName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String homePhone = etHomePhone.getText().toString().trim();
        String status = spinnerStatus.getText().toString().trim();

        CreateCustomerRequest request = new CreateCustomerRequest(
                TextUtils.isEmpty(firstName) ? null : firstName,
                TextUtils.isEmpty(lastName) ? null : lastName,
                isBusiness && !TextUtils.isEmpty(businessName) ? businessName : null,
                email,
                homePhone,
                customerType,
                status,
                null,       // street1
                null,       // street2
                null,       // city
                null,       // province
                null,       // postalCode
                "Canada",    // country
                getSelectedAgentId()
        );

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if ("edit".equalsIgnoreCase(mode) && customerId > 0) {
            apiService.updateCustomer(customerId, request).enqueue(new Callback<CustomerResponse>() {
                @Override
                public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(CustomerFormActivity.this,
                                "Updated successfully",
                                Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(CustomerFormActivity.this,
                                "Update failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<CustomerResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(CustomerFormActivity.this,
                            "Unable to update customer",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            apiService.createCustomer(request).enqueue(new Callback<CreateCustomerResponse>() {
                @Override
                public void onResponse(Call<CreateCustomerResponse> call, Response<CreateCustomerResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        CreateCustomerResponse created = response.body();

                        new androidx.appcompat.app.AlertDialog.Builder(CustomerFormActivity.this)
                                .setTitle("Customer Created")
                                .setMessage("Username: " + created.getUsername()
                                        + "\nTemporary Password: " + created.getTempPassword()
                                        + "\n\nTap COPY to save credentials.")
                                .setCancelable(false)
                                .setPositiveButton("Copy", (dialog, which) -> {
                                    String textToCopy = "Username: " + created.getUsername()
                                            + "\nTemporary Password: " + created.getTempPassword();

                                    android.content.ClipboardManager clipboard =
                                            (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                                    android.content.ClipData clip =
                                            android.content.ClipData.newPlainText("Customer Credentials", textToCopy);

                                    clipboard.setPrimaryClip(clip);

                                    Toast.makeText(CustomerFormActivity.this,
                                            "Copied to clipboard",
                                            Toast.LENGTH_SHORT).show();

                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .show();
                    } else {
                        Toast.makeText(CustomerFormActivity.this,
                                "Create failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<CreateCustomerResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(CustomerFormActivity.this,
                            "Unable to create customer",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
    }

    private void loadAgents() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getEmployees().enqueue(new Callback<List<EmployeeResponse>>() {
            @Override
            public void onResponse(Call<List<EmployeeResponse>> call, Response<List<EmployeeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    agentList.clear();

                    List<String> names = new ArrayList<>();
                    names.add("None"); // allow NULL

                    for (EmployeeResponse e : response.body()) {
                        String role = e.getRoleName();

                        if ("Manager".equalsIgnoreCase(role) ||
                                "Sales Agent".equalsIgnoreCase(role)) {

                            agentList.add(e);
                            names.add(e.getFirstName() + " " + e.getLastName());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            CustomerFormActivity.this,
                            android.R.layout.simple_list_item_1,
                            names
                    );

                    spinnerAssignedAgent.setAdapter(adapter);

                    if (existingAssignedEmployeeId == null) {
                        spinnerAssignedAgent.setText("None", false);
                    } else {
                        boolean found = false;

                        for (EmployeeResponse e : agentList) {
                            if (e.getEmployeeId() == existingAssignedEmployeeId) {
                                spinnerAssignedAgent.setText(
                                        e.getFirstName() + " " + e.getLastName(),
                                        false
                                );
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            spinnerAssignedAgent.setText("None", false);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EmployeeResponse>> call, Throwable t) {
                Toast.makeText(CustomerFormActivity.this,
                        "Failed to load agents",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Integer getSelectedAgentId() {
        String selected = spinnerAssignedAgent.getText().toString();

        if (selected.equals("None")) return null;

        for (EmployeeResponse e : agentList) {
            String name = e.getFirstName() + " " + e.getLastName();
            if (name.equals(selected)) {
                return e.getEmployeeId();
            }
        }

        return null;
    }
}
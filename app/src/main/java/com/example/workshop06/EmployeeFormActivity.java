package com.example.workshop06;

import android.app.AlertDialog;
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
import com.example.workshop06.model.CreateEmployeeResponse;
import com.example.workshop06.model.EmployeeResponse;
import com.example.workshop06.model.SaveEmployeeRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeFormActivity extends AppCompatActivity {

    private EditText etPrimaryLocationId;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPhone;
    private Spinner spinnerRole;
    private EditText etSalary;
    private EditText etHireDate;
    private Spinner spinnerStatus;
    private Spinner spinnerActive;
    private EditText etManagerId;
    private Button btnSave;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private String mode = "add";
    private int employeeId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_form);

        initViews();
        setupSpinners();
        loadIntentData();
        setupButtons();
    }

    private void initViews() {
        etPrimaryLocationId = findViewById(R.id.etPrimaryLocationId);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        spinnerRole = findViewById(R.id.spinnerRole);
        etSalary = findViewById(R.id.etSalary);
        etHireDate = findViewById(R.id.etHireDate);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerActive = findViewById(R.id.spinnerActive);
        etManagerId = findViewById(R.id.etManagerId);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"MANAGER", "SALES_AGENT", "SERVICE_TECHNICIAN"}
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Active", "Inactive", "On Leave", "Suspended"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<String> activeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"1", "0"}
        );
        activeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActive.setAdapter(activeAdapter);
    }

    private void loadIntentData() {
        if (getIntent() == null) return;

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if ("edit".equalsIgnoreCase(mode)) {
            employeeId = getIntent().getIntExtra("employeeId", -1);

            int primaryLocationId = getIntent().getIntExtra("primaryLocationId", Integer.MIN_VALUE);
            double salary = getIntent().getDoubleExtra("salary", Double.NaN);
            int active = getIntent().getIntExtra("active", Integer.MIN_VALUE);
            int managerId = getIntent().getIntExtra("managerId", Integer.MIN_VALUE);

            String firstName = getIntent().getStringExtra("firstName");
            String lastName = getIntent().getStringExtra("lastName");
            String email = getIntent().getStringExtra("email");
            String phone = getIntent().getStringExtra("phone");
            String role = getIntent().getStringExtra("role");
            String hireDate = getIntent().getStringExtra("hireDate");
            String status = getIntent().getStringExtra("status");

            if (primaryLocationId != Integer.MIN_VALUE) etPrimaryLocationId.setText(String.valueOf(primaryLocationId));
            if (!Double.isNaN(salary)) etSalary.setText(String.valueOf(salary));
            if (managerId != Integer.MIN_VALUE) etManagerId.setText(String.valueOf(managerId));

            if (firstName != null) etFirstName.setText(firstName);
            if (lastName != null) etLastName.setText(lastName);
            if (email != null) etEmail.setText(email);
            if (phone != null) etPhone.setText(phone);
            if (hireDate != null) etHireDate.setText(hireDate);

            setSpinnerValue(spinnerRole, role);
            setSpinnerValue(spinnerStatus, status);
            if (active != Integer.MIN_VALUE) {
                setSpinnerValue(spinnerActive, String.valueOf(active));
            }
        }
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
            btnSave.setOnClickListener(v -> saveEmployee());
        }
    }

    private void saveEmployee() {
        String primaryLocationIdText = etPrimaryLocationId.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        String salaryText = etSalary.getText().toString().trim();
        String hireDate = etHireDate.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String activeText = spinnerActive.getSelectedItem().toString();
        String managerIdText = etManagerId.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        Integer primaryLocationId = TextUtils.isEmpty(primaryLocationIdText) ? null : Integer.parseInt(primaryLocationIdText);
        Double salary = TextUtils.isEmpty(salaryText) ? null : Double.parseDouble(salaryText);
        Integer active = TextUtils.isEmpty(activeText) ? null : Integer.parseInt(activeText);
        Integer managerId = TextUtils.isEmpty(managerIdText) ? null : Integer.parseInt(managerIdText);

        SaveEmployeeRequest request = new SaveEmployeeRequest(
                primaryLocationId,
                firstName,
                lastName,
                email,
                TextUtils.isEmpty(phone) ? null : phone,
                role,
                salary,
                TextUtils.isEmpty(hireDate) ? null : hireDate,
                status,
                active,
                managerId
        );

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if ("edit".equalsIgnoreCase(mode) && employeeId > 0) {
            apiService.updateEmployee(employeeId, request).enqueue(new Callback<EmployeeResponse>() {
                @Override
                public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(EmployeeFormActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(EmployeeFormActivity.this,
                                "Update failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(EmployeeFormActivity.this,
                            "Unable to update employee",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            apiService.createEmployee(request).enqueue(new Callback<CreateEmployeeResponse>() {
                @Override
                public void onResponse(Call<CreateEmployeeResponse> call, Response<CreateEmployeeResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        CreateEmployeeResponse created = response.body();

                        new AlertDialog.Builder(EmployeeFormActivity.this)
                                .setTitle("Employee Created")
                                .setMessage("Username: " + created.getUsername()
                                        + "\nTemporary Password: " + created.getTempPassword())
                                .setPositiveButton("OK", (dialog, which) -> {
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .setCancelable(false)
                                .show();
                    } else {
                        Toast.makeText(EmployeeFormActivity.this,
                                "Create failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<CreateEmployeeResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(EmployeeFormActivity.this,
                            "Unable to create employee",
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
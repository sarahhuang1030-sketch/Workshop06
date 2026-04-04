package com.example.workshop06;

import android.app.DatePickerDialog;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.CreateEmployeeResponse;
import com.example.workshop06.model.EmployeeResponse;
import com.example.workshop06.model.LocationResponse;
import com.example.workshop06.model.SaveEmployeeRequest;
import com.example.workshop06.util.FormFormatUtils;
import com.example.workshop06.util.ValidationUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeFormActivity extends AppCompatActivity {

    private EditText etEmployeeId;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPhone;
    private Spinner spinnerRole;
    private EditText etSalary;
    private EditText etHireDate;
    private ImageButton btnHireDate;
    private Spinner spinnerStatus;
    private Spinner spinnerManager;
    private Spinner spinnerLocation;
    private Button btnSave;
    private ProgressBar progressBar;

    private String mode = "add";
    private int employeeId = -1;

    private int selectedLocationId = -1;
    private final List<LocationResponse> locationList = new ArrayList<>();
    private ArrayAdapter<String> locationAdapter;

    private final List<EmployeeResponse> managerList = new ArrayList<>();
    private ArrayAdapter<String> managerAdapter;
    private int selectedManagerId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_form);

        initViews();
        setupSpinners();
        setupFormatters();
        setupDatePicker();
        loadIntentData();
        loadLocations();
        loadManagers();
        setupButtons();
    }

    private void initViews() {
        etEmployeeId = findViewById(R.id.etEmployeeId);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        spinnerRole = findViewById(R.id.spinnerRole);
        etSalary = findViewById(R.id.etSalary);
        etHireDate = findViewById(R.id.etHireDate);
        btnHireDate = findViewById(R.id.btnHireDate);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerManager = findViewById(R.id.spinnerManager);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        btnSave = findViewById(R.id.btnSave);
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

        managerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        managerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerManager.setAdapter(managerAdapter);
    }

    private void setupFormatters() {
        FormFormatUtils.attachCanadianPhoneFormatter(etPhone);
    }

    private void setupDatePicker() {
        View.OnClickListener listener = v -> showDatePicker();
        etHireDate.setOnClickListener(listener);
        btnHireDate.setOnClickListener(listener);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formatted = String.format(
                            Locale.CANADA,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );
                    etHireDate.setText(formatted);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void loadIntentData() {
        if (getIntent() == null) return;

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if ("edit".equalsIgnoreCase(mode)) {
            employeeId = getIntent().getIntExtra("employeeId", -1);

            int primaryLocationId = getIntent().getIntExtra("primaryLocationId", Integer.MIN_VALUE);
            double salary = getIntent().getDoubleExtra("salary", Double.NaN);
            int managerId = getIntent().getIntExtra("managerId", Integer.MIN_VALUE);

            String firstName = getIntent().getStringExtra("firstName");
            String lastName = getIntent().getStringExtra("lastName");
            String email = getIntent().getStringExtra("email");
            String phone = getIntent().getStringExtra("phone");
            String role = getIntent().getStringExtra("role");
            String hireDate = getIntent().getStringExtra("hireDate");
            String status = getIntent().getStringExtra("status");

            etEmployeeId.setText(String.valueOf(employeeId));

            if (primaryLocationId != Integer.MIN_VALUE) {
                selectedLocationId = primaryLocationId;
            }

            if (!Double.isNaN(salary)) {
                etSalary.setText(String.format(Locale.US, "%.2f", salary));
            }

            if (managerId != Integer.MIN_VALUE) {
                selectedManagerId = managerId;
            }

            if (firstName != null) etFirstName.setText(firstName);
            if (lastName != null) etLastName.setText(lastName);
            if (email != null) etEmail.setText(email);
            if (phone != null) etPhone.setText(phone);
            if (hireDate != null) etHireDate.setText(hireDate);

            setSpinnerValue(spinnerRole, role);
            setSpinnerValue(spinnerStatus, status);
        }
    }

    private void loadLocations() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getLocations().enqueue(new Callback<List<LocationResponse>>() {
            @Override
            public void onResponse(Call<List<LocationResponse>> call, Response<List<LocationResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                locationList.clear();
                locationList.addAll(response.body());

                List<String> names = new ArrayList<>();
                for (LocationResponse loc : locationList) {
                    names.add(loc.getLocationName());
                }

                locationAdapter = new ArrayAdapter<>(
                        EmployeeFormActivity.this,
                        android.R.layout.simple_spinner_item,
                        names
                );
                locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerLocation.setAdapter(locationAdapter);

                setSelectedLocation();
            }

            @Override
            public void onFailure(Call<List<LocationResponse>> call, Throwable t) {
                Toast.makeText(EmployeeFormActivity.this, "Failed to load locations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSelectedLocation() {
        if (selectedLocationId <= 0) return;

        for (int i = 0; i < locationList.size(); i++) {
            if (locationList.get(i).getLocationId() == selectedLocationId) {
                spinnerLocation.setSelection(i);
                break;
            }
        }
    }

    private void loadManagers() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getEmployees().enqueue(new Callback<List<EmployeeResponse>>() {
            @Override
            public void onResponse(Call<List<EmployeeResponse>> call, Response<List<EmployeeResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                managerList.clear();
                List<String> labels = new ArrayList<>();
                labels.add("No Manager");

                for (EmployeeResponse employee : response.body()) {
                    if (employee != null && employee.getRole() != null
                            && "MANAGER".equalsIgnoreCase(employee.getRole())) {
                        managerList.add(employee);

                        String fullName = (safe(employee.getFirstName()) + " " + safe(employee.getLastName())).trim();
                        labels.add(fullName + " (MANAGER)");
                    }
                }

                managerAdapter.clear();
                managerAdapter.addAll(labels);
                managerAdapter.notifyDataSetChanged();

                if (selectedManagerId > 0) {
                    setManagerSelection(selectedManagerId);
                }
            }

            @Override
            public void onFailure(Call<List<EmployeeResponse>> call, Throwable t) {
                Toast.makeText(EmployeeFormActivity.this, "Unable to load managers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setManagerSelection(int managerId) {
        for (int i = 0; i < managerList.size(); i++) {
            if (managerList.get(i).getEmployeeId() == managerId) {
                spinnerManager.setSelection(i + 1);
                break;
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
        btnSave.setOnClickListener(v -> saveEmployee());
    }

    private void saveEmployee() {
        if (!ValidationUtils.required(etFirstName, "First name is required")) return;
        if (!ValidationUtils.required(etLastName, "Last name is required")) return;
        if (!ValidationUtils.email(etEmail)) return;
        if (!ValidationUtils.phone(etPhone)) return;
        if (!ValidationUtils.positiveNumber(etSalary, "Enter a valid salary")) return;

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        String salaryText = etSalary.getText().toString().trim();
        String hireDate = etHireDate.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        Integer primaryLocationId = null;
        int locationPosition = spinnerLocation.getSelectedItemPosition();
        if (locationPosition >= 0 && locationPosition < locationList.size()) {
            primaryLocationId = locationList.get(locationPosition).getLocationId();
        }

        Double salary = TextUtils.isEmpty(salaryText)
                ? null : Double.parseDouble(salaryText);

        Integer managerId = null;
        int managerPosition = spinnerManager.getSelectedItemPosition();
        if (managerPosition > 0 && managerPosition - 1 < managerList.size()) {
            managerId = managerList.get(managerPosition - 1).getEmployeeId();
        }

        SaveEmployeeRequest request = new SaveEmployeeRequest(
                primaryLocationId,
                firstName,
                lastName,
                email,
                phone,
                role,
                salary,
                TextUtils.isEmpty(hireDate) ? null : hireDate,
                status,
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
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
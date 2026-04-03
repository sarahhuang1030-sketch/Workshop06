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
import com.example.workshop06.model.CustomerResponse;
import com.example.workshop06.model.SaveCustomerRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerFormActivity extends AppCompatActivity {

    private Spinner spinnerCustomerType;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etBusinessName;
    private EditText etEmail;
    private EditText etHomePhone;
    private Spinner spinnerStatus;
    private Button btnSave;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private String mode = "add";
    private int customerId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_form);

        initViews();
        setupSpinners();
        loadIntentData();
        setupButtons();
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
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Individual", "Business"}
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCustomerType.setAdapter(typeAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Active", "Inactive"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void loadIntentData() {
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if ("edit".equalsIgnoreCase(mode)) {
            customerId = getIntent().getIntExtra("customerId", -1);

            setSpinnerValue(spinnerCustomerType, getIntent().getStringExtra("customerType"));
            etFirstName.setText(getIntent().getStringExtra("firstName"));
            etLastName.setText(getIntent().getStringExtra("lastName"));
            etBusinessName.setText(getIntent().getStringExtra("businessName"));
            etEmail.setText(getIntent().getStringExtra("email"));
            etHomePhone.setText(getIntent().getStringExtra("homePhone"));
            setSpinnerValue(spinnerStatus, getIntent().getStringExtra("status"));
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
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveCustomer());
    }

    private void saveCustomer() {
        String customerType = spinnerCustomerType.getSelectedItem().toString();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String businessName = etBusinessName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String homePhone = etHomePhone.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(homePhone)) {
            etHomePhone.setError("Phone is required");
            etHomePhone.requestFocus();
            return;
        }

        if ("Business".equalsIgnoreCase(customerType) && TextUtils.isEmpty(businessName)) {
            etBusinessName.setError("Business name is required for business customer");
            etBusinessName.requestFocus();
            return;
        }

        SaveCustomerRequest request = new SaveCustomerRequest(
                customerType,
                TextUtils.isEmpty(firstName) ? null : firstName,
                TextUtils.isEmpty(lastName) ? null : lastName,
                TextUtils.isEmpty(businessName) ? null : businessName,
                email,
                homePhone,
                status
        );

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if ("edit".equalsIgnoreCase(mode) && customerId > 0) {
            apiService.updateCustomer(customerId, request).enqueue(new Callback<CustomerResponse>() {
                @Override
                public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(CustomerFormActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
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
            apiService.createCustomer(request).enqueue(new Callback<CustomerResponse>() {
                @Override
                public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(CustomerFormActivity.this, "Created successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(CustomerFormActivity.this,
                                "Create failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<CustomerResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(CustomerFormActivity.this,
                            "Unable to create customer",
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
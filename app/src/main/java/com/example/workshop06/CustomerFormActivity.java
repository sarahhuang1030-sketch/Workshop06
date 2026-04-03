package com.example.workshop06;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.CreateCustomerRequest;
import com.example.workshop06.model.CreateCustomerResponse;
import com.example.workshop06.model.CustomerResponse;
import com.example.workshop06.util.FormFormatUtils;
import com.example.workshop06.util.ValidationUtils;

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

    private ProgressBar progressBar;

    private String mode = "add";
    private int customerId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_form);

        initViews();
        setupSpinners();
        attachFormatters();
        loadIntentData();
        setupTypeBehavior();
        updateCustomerTypeUI();
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

    private void attachFormatters() {
        FormFormatUtils.attachCanadianPhoneFormatter(etHomePhone);
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

    private void setupTypeBehavior() {
        spinnerCustomerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCustomerTypeUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateCustomerTypeUI() {
        String customerType = spinnerCustomerType.getSelectedItem() != null
                ? spinnerCustomerType.getSelectedItem().toString()
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
        String customerType = spinnerCustomerType.getSelectedItem() != null
                ? spinnerCustomerType.getSelectedItem().toString()
                : "Individual";

        boolean isBusiness = "Business".equalsIgnoreCase(customerType);

        if (!ValidationUtils.email(etEmail)) {
            return false;
        }

        if (!ValidationUtils.phone(etHomePhone)) {
            return false;
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

        String customerType = spinnerCustomerType.getSelectedItem().toString();
        boolean isBusiness = "Business".equalsIgnoreCase(customerType);

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String businessName = etBusinessName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String homePhone = etHomePhone.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

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
                "Canada"    // country
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

                                // ✅ ONLY BUTTON
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

                                    // ✅ go back after copying
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
}
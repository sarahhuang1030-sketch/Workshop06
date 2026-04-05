package com.example.workshop06;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.CustomerAddressResponse;
import com.example.workshop06.model.SaveCustomerAddressRequest;
import com.example.workshop06.util.FormFormatUtils;
import com.example.workshop06.util.ValidationUtils;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerAddressFormActivity extends AppCompatActivity {

    private static final String TAG = "CustomerAddressForm";

    private MaterialAutoCompleteTextView spinnerAddressType;
    private EditText etStreet1, etStreet2, etCity, etProvince, etPostalCode, etCountry;
    private Button btnSave;
    private ProgressBar progressBar;

    private int customerId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_address_form);

        customerId = getIntent().getIntExtra("customerId", -1);

        initViews();
        setupSpinner();
        setupButtons();

        FormFormatUtils.attachCanadianPostalCodeFormatter(etPostalCode);

        if (etCountry.getText() == null || etCountry.getText().toString().trim().isEmpty()) {
            etCountry.setText("Canada");
        }

        if (customerId <= 0) {
            Toast.makeText(this, "Invalid customer ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadAddress();
    }

    private void initViews() {
        spinnerAddressType = findViewById(R.id.spinnerAddressType);
        etStreet1 = findViewById(R.id.etStreet1);
        etStreet2 = findViewById(R.id.etStreet2);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etPostalCode = findViewById(R.id.etPostalCode);
        etCountry = findViewById(R.id.etCountry);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinner() {
        String[] addressTypes = {"Billing", "Service"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                addressTypes
        );

        spinnerAddressType.setAdapter(adapter);
        spinnerAddressType.setText(addressTypes[0], false);
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void loadAddress() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getCustomerAddress(customerId).enqueue(new Callback<CustomerAddressResponse>() {
            @Override
            public void onResponse(Call<CustomerAddressResponse> call, Response<CustomerAddressResponse> response) {
                showLoading(false);

                Log.d(TAG, "loadAddress code=" + response.code() + ", customerId=" + customerId);

                if (!response.isSuccessful()) {
                    Toast.makeText(CustomerAddressFormActivity.this,
                            "No saved address returned. Code: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                CustomerAddressResponse address = response.body();
                if (address == null) {
                    Toast.makeText(CustomerAddressFormActivity.this,
                            "Address response is empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Loaded address: type=" + address.getAddressType()
                        + ", street1=" + address.getStreet1()
                        + ", city=" + address.getCity()
                        + ", province=" + address.getProvince()
                        + ", postal=" + address.getPostalCode()
                        + ", country=" + address.getCountry());

                setDropdownValue(address.getAddressType());

                etStreet1.setText(safe(address.getStreet1()));
                etStreet2.setText(safe(address.getStreet2()));
                etCity.setText(safe(address.getCity()));
                etProvince.setText(safe(address.getProvince()));
                etPostalCode.setText(safe(address.getPostalCode()));
                etCountry.setText(safe(address.getCountry(), "Canada"));
            }

            @Override
            public void onFailure(Call<CustomerAddressResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "loadAddress failed", t);
                Toast.makeText(CustomerAddressFormActivity.this,
                        "Unable to load address: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setDropdownValue(String value) {
        if (value == null || value.trim().isEmpty()) return;
        spinnerAddressType.setText(value, false);
    }

    private boolean validateForm() {
        if (spinnerAddressType.getText() == null ||
                spinnerAddressType.getText().toString().trim().isEmpty()) {
            spinnerAddressType.setError("Address type is required");
            return false;
        }
        spinnerAddressType.setError(null);

        if (!ValidationUtils.required(etStreet1, "Street 1 is required")) {
            return false;
        }

        if (!ValidationUtils.required(etCity, "City is required")) {
            return false;
        }

        if (!ValidationUtils.required(etProvince, "Province is required")) {
            return false;
        }

        if (!ValidationUtils.canadianPostalCode(etPostalCode)) {
            return false;
        }

        if (!ValidationUtils.required(etCountry, "Country is required")) {
            return false;
        }

        return true;
    }

    private void saveAddress() {
        if (!validateForm()) {
            return;
        }

        String addressType = spinnerAddressType.getText().toString().trim();
        String street1 = etStreet1.getText().toString().trim();
        String street2 = etStreet2.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String province = etProvince.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim().toUpperCase();
        String country = etCountry.getText().toString().trim();

        SaveCustomerAddressRequest request = new SaveCustomerAddressRequest(
                addressType,
                street1,
                street2.isEmpty() ? null : street2,
                city,
                province,
                postalCode,
                country
        );

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.saveCustomerAddress(customerId, request).enqueue(new Callback<CustomerAddressResponse>() {
            @Override
            public void onResponse(Call<CustomerAddressResponse> call, Response<CustomerAddressResponse> response) {
                showLoading(false);

                Log.d(TAG, "saveAddress code=" + response.code() + ", customerId=" + customerId);

                if (response.isSuccessful()) {
                    Toast.makeText(CustomerAddressFormActivity.this, "Address saved", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(CustomerAddressFormActivity.this,
                            "Save failed. Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CustomerAddressResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "saveAddress failed", t);
                Toast.makeText(CustomerAddressFormActivity.this,
                        "Unable to save address",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
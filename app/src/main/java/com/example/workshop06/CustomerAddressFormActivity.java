package com.example.workshop06;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.CustomerAddressResponse;
import com.example.workshop06.model.SaveCustomerAddressRequest;
import com.example.workshop06.util.FormFormatUtils;
import com.example.workshop06.util.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerAddressFormActivity extends AppCompatActivity {

    private TextView tvTitle;
    private Spinner spinnerAddressType;
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

        String customerName = getIntent().getStringExtra("customerName");
        if (tvTitle != null && customerName != null && !customerName.trim().isEmpty()) {
            tvTitle.setText("Address • " + customerName);
        }

        if (customerId <= 0) {
            Toast.makeText(this, "Invalid customer ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadAddress();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Billing", "Service"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddressType.setAdapter(adapter);
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

                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                CustomerAddressResponse address = response.body();
                setSpinnerValue(address.getAddressType());
                etStreet1.setText(address.getStreet1());
                etStreet2.setText(address.getStreet2());
                etCity.setText(address.getCity());
                etProvince.setText(address.getProvince());
                etPostalCode.setText(address.getPostalCode());
                etCountry.setText(address.getCountry());
            }

            @Override
            public void onFailure(Call<CustomerAddressResponse> call, Throwable t) {
                showLoading(false);
            }
        });
    }

    private void setSpinnerValue(String value) {
        if (value == null) return;
        for (int i = 0; i < spinnerAddressType.getCount(); i++) {
            String item = String.valueOf(spinnerAddressType.getItemAtPosition(i));
            if (item.equalsIgnoreCase(value)) {
                spinnerAddressType.setSelection(i);
                break;
            }
        }
    }

    private boolean validateForm() {
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

        String addressType = spinnerAddressType.getSelectedItem().toString();
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
                Toast.makeText(CustomerAddressFormActivity.this,
                        "Unable to save address",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        btnSave.setEnabled(!isLoading);
    }
}
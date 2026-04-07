package com.example.workshop06;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private boolean readOnly = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_address_form);

        initViews();

        customerId = getIntent().getIntExtra("customerId", -1);
        readOnly = getIntent().getBooleanExtra("readOnly", false);

        setupAddressType();

        if (customerId <= 0) {
            Toast.makeText(this, "Invalid customer id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (readOnly) {
            makeReadOnly();
        } else {
            setupButtons();
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

    private void setupAddressType() {
        String[] types = {"Billing", "Service"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                types
        );
        spinnerAddressType.setAdapter(adapter);
        spinnerAddressType.setText("Billing", false);
    }

    private void loadAddress() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        Call<CustomerAddressResponse> call = readOnly
                ? apiService.getCustomerAddressForTechnician(customerId)
                : apiService.getCustomerAddress(customerId);

        call.enqueue(new Callback<CustomerAddressResponse>() {
            @Override
            public void onResponse(Call<CustomerAddressResponse> call, Response<CustomerAddressResponse> response) {
                showLoading(false);

                Log.d(TAG, "loadAddress code=" + response.code() + ", customerId=" + customerId + ", readOnly=" + readOnly);

                if (!response.isSuccessful()) {
                    Toast.makeText(CustomerAddressFormActivity.this,
                            "No saved address returned. Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                CustomerAddressResponse data = response.body();
                if (data == null) return;

                if (data.getAddressType() != null) spinnerAddressType.setText(data.getAddressType(), false);
                if (data.getStreet1() != null) etStreet1.setText(data.getStreet1());
                if (data.getStreet2() != null) etStreet2.setText(data.getStreet2());
                if (data.getCity() != null) etCity.setText(data.getCity());
                if (data.getProvince() != null) etProvince.setText(data.getProvince());
                if (data.getPostalCode() != null) etPostalCode.setText(data.getPostalCode());
                if (data.getCountry() != null) etCountry.setText(data.getCountry());
            }

            @Override
            public void onFailure(Call<CustomerAddressResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CustomerAddressFormActivity.this,
                        "Unable to load address",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        if (readOnly) {
            Toast.makeText(this, "Technicians can only view addresses", Toast.LENGTH_SHORT).show();
            return;
        }

        SaveCustomerAddressRequest request = new SaveCustomerAddressRequest(
                spinnerAddressType.getText() != null ? spinnerAddressType.getText().toString().trim() : "Billing",
                etStreet1.getText().toString().trim(),
                etStreet2.getText().toString().trim(),
                etCity.getText().toString().trim(),
                etProvince.getText().toString().trim(),
                etPostalCode.getText().toString().trim(),
                etCountry.getText().toString().trim()
        );

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.saveCustomerAddress(customerId, request).enqueue(new Callback<CustomerAddressResponse>() {
            @Override
            public void onResponse(Call<CustomerAddressResponse> call, Response<CustomerAddressResponse> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(CustomerAddressFormActivity.this,
                            "Address saved",
                            Toast.LENGTH_SHORT).show();
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

    private void makeReadOnly() {
        spinnerAddressType.setEnabled(false);
        etStreet1.setEnabled(false);
        etStreet2.setEnabled(false);
        etCity.setEnabled(false);
        etProvince.setEnabled(false);
        etPostalCode.setEnabled(false);
        etCountry.setEnabled(false);

        if (btnSave != null) {
            btnSave.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (btnSave != null) {
            btnSave.setEnabled(!isLoading && !readOnly);
        }
    }
}
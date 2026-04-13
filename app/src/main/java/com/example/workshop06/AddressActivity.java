package com.example.workshop06;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.AddressRequest;
import com.example.workshop06.model.AddressResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressActivity extends BaseActivity {

    private ImageView btnBack;
    private EditText etStreet1, etStreet2, etCity, etProvince, etPostalCode, etCountry;
    private Button btnSave;

    private SessionManager sessionManager;
    private ApiService apiService;
    private String token;
    private boolean hasExistingAddress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        btnBack = findViewById(R.id.btnBack);
        etStreet1 = findViewById(R.id.etStreet1);
        etStreet2 = findViewById(R.id.etStreet2);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etPostalCode = findViewById(R.id.etPostalCode);
        etCountry = findViewById(R.id.etCountry);
        btnSave = findViewById(R.id.btnSave);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();
        token = sessionManager.getToken();

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveAddress());

        loadAddress();
    }

    private void loadAddress() {
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService.getBillingAddress("Bearer " + token).enqueue(new Callback<AddressResponse>() {
            @Override
            public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AddressResponse address = response.body();
                    hasExistingAddress = true;

                    if (address.getStreet1() != null) etStreet1.setText(address.getStreet1());
                    if (address.getStreet2() != null) etStreet2.setText(address.getStreet2());
                    if (address.getCity() != null) etCity.setText(address.getCity());
                    if (address.getProvince() != null) etProvince.setText(address.getProvince());
                    if (address.getPostalCode() != null) etPostalCode.setText(address.getPostalCode());
                    if (address.getCountry() != null) etCountry.setText(address.getCountry());

                } else if (response.code() == 404 || response.code() == 204 || response.code() == 409) {
                    hasExistingAddress = false;
                } else {
                    Toast.makeText(AddressActivity.this, "Failed to load address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddressResponse> call, Throwable t) {
                Toast.makeText(AddressActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAddress() {
        String street1 = etStreet1.getText().toString().trim();
        String street2 = etStreet2.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String province = etProvince.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();
        String country = etCountry.getText().toString().trim();

        if (TextUtils.isEmpty(street1)) {
            etStreet1.setError("Street address is required");
            etStreet1.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(city)) {
            etCity.setError("City is required");
            etCity.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(province)) {
            etProvince.setError("Province is required");
            etProvince.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(postalCode)) {
            etPostalCode.setError("Postal code is required");
            etPostalCode.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(country)) {
            etCountry.setError("Country is required");
            etCountry.requestFocus();
            return;
        }

        AddressRequest request = new AddressRequest(
                street1,
                street2,
                city,
                province,
                postalCode,
                country
        );

        if (hasExistingAddress) {
            updateAddress(request);
        } else {
            createAddress(request);
        }
    }

    private void createAddress(AddressRequest request) {
        apiService.createBillingAddress("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddressActivity.this, "Address saved", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddressActivity.this, "Failed to save address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddressActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAddress(AddressRequest request) {
        apiService.updateBillingAddress("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddressActivity.this, "Address updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddressActivity.this, "Failed to update address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddressActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
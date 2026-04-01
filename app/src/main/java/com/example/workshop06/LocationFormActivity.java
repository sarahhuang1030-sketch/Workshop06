package com.example.workshop06;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.LocationRequest;
import com.example.workshop06.model.LocationResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationFormActivity extends AppCompatActivity {

    private EditText etLocationName, etLocationType, etStreet1, etStreet2, etCity,
            etProvince, etPostalCode, etCountry, etPhone;
    private CheckBox cbActive;
    private Button btnSave;

    private Integer locationId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_form);

        etLocationName = findViewById(R.id.etLocationName);
        etLocationType = findViewById(R.id.etLocationType);
        etStreet1 = findViewById(R.id.etStreet1);
        etStreet2 = findViewById(R.id.etStreet2);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etPostalCode = findViewById(R.id.etPostalCode);
        etCountry = findViewById(R.id.etCountry);
        etPhone = findViewById(R.id.etPhone);
        cbActive = findViewById(R.id.cbActive);
        btnSave = findViewById(R.id.btnSaveLocation);

        if (getIntent() != null && getIntent().hasExtra("locationId")) {
            locationId = getIntent().getIntExtra("locationId", -1);
            if (locationId != -1) {
                loadLocation(locationId);
            }
        }

        btnSave.setOnClickListener(v -> saveLocation());
    }

    private void loadLocation(int id) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getLocationById(id).enqueue(new Callback<LocationResponse>() {
            @Override
            public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                Toast.makeText(LocationFormActivity.this,
                        "GET by id code = " + response.code(), Toast.LENGTH_SHORT).show();

                if (response.isSuccessful() && response.body() != null) {
                    LocationResponse item = response.body();

                    Toast.makeText(LocationFormActivity.this,
                            "Loaded: " + item.getLocationName(), Toast.LENGTH_SHORT).show();
                    //LocationResponse item = response.body();
                    etLocationName.setText(item.getLocationName());
                    etLocationType.setText(item.getLocationType());
                    etStreet1.setText(item.getStreet1());
                    etStreet2.setText(item.getStreet2());
                    etCity.setText(item.getCity());
                    etProvince.setText(item.getProvince());
                    etPostalCode.setText(item.getPostalCode());
                    etCountry.setText(item.getCountry());
                    etPhone.setText(item.getPhone());
                    cbActive.setChecked(Boolean.TRUE.equals(item.getIsActive()));
                }
            }

            @Override
            public void onFailure(Call<LocationResponse> call, Throwable t) {
                Toast.makeText(LocationFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveLocation() {
        LocationRequest request = new LocationRequest(
                etLocationName.getText().toString().trim(),
                etLocationType.getText().toString().trim(),
                etStreet1.getText().toString().trim(),
                etStreet2.getText().toString().trim(),
                etCity.getText().toString().trim(),
                etProvince.getText().toString().trim(),
                etPostalCode.getText().toString().trim(),
                etCountry.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                cbActive.isChecked()
        );

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if (locationId == null) {
            apiService.createLocation(request).enqueue(new Callback<LocationResponse>() {
                @Override
                public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(LocationFormActivity.this, "Location created", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(LocationFormActivity.this, "Create failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LocationResponse> call, Throwable t) {
                    Toast.makeText(LocationFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            apiService.updateLocation(locationId, request).enqueue(new Callback<LocationResponse>() {
                @Override
                public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(LocationFormActivity.this, "Location updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(LocationFormActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LocationResponse> call, Throwable t) {
                    Toast.makeText(LocationFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
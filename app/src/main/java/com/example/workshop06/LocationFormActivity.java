package com.example.workshop06;

import android.os.Bundle;
import android.util.Log;
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
import com.example.workshop06.util.FormFormatUtils;
import com.example.workshop06.util.ValidationUtils;

import android.widget.ArrayAdapter;
import android.widget.Spinner;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationFormActivity extends AppCompatActivity {

    private EditText etLocationName, etStreet1, etStreet2, etCity,
            etProvince, etPostalCode, etCountry, etPhone;
    private CheckBox cbActive;
    private Button btnSave;

    private Integer locationId = null;

    private Spinner spinnerLocationType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_form);

        spinnerLocationType = findViewById(R.id.spinnerLocationType);

        String[] locationTypes = {"SalesPoint", "Warehouse", "Office"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locationTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocationType.setAdapter(adapter);

        etLocationName = findViewById(R.id.etLocationName);
        //  etLocationType = findViewById(R.id.etLocationType);
        etStreet1 = findViewById(R.id.etStreet1);
        etStreet2 = findViewById(R.id.etStreet2);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        FormFormatUtils.attachCanadianPhoneFormatter(etPhone);
        FormFormatUtils.attachCanadianPostalCodeFormatter(etPostalCode);
        etCountry = findViewById(R.id.etCountry);
        cbActive = findViewById(R.id.cbActive);
        btnSave = findViewById(R.id.btnSaveLocation);
        etPostalCode = findViewById(R.id.etPostalCode);
        etPhone = findViewById(R.id.etPhone);

        if (getIntent() != null && getIntent().hasExtra("locationId")) {
            locationId = getIntent().getIntExtra("locationId", -1);
            if (locationId != -1) {
                loadLocation(locationId);
            }
        }

        FormFormatUtils.attachCanadianPhoneFormatter(etPhone);
        FormFormatUtils.attachCanadianPostalCodeFormatter(etPostalCode);

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
//                    etLocationType.setText(item.getLocationType());
                    setSpinnerSelection(item.getLocationType());

                    etStreet1.setText(item.getStreet1() != null ? item.getStreet1() : "");
                    etStreet2.setText(item.getStreet2() != null ? item.getStreet2() : "");
                    etPostalCode.setText(item.getPostalCode() != null ? item.getPostalCode() : "");
                    etCity.setText(item.getCity());
                    etProvince.setText(item.getProvince());
                    etCountry.setText(item.getCountry());
                    etPhone.setText(item.getPhone() != null ? item.getPhone() : "");
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

        // ✅ VALIDATION FIRST
        if (!ValidationUtils.required(etLocationName, "Location name is required")) return;
        if (!ValidationUtils.required(etStreet1, "Street 1 is required")) return;
        if (!ValidationUtils.required(etCity, "City is required")) return;
        if (!ValidationUtils.required(etProvince, "Province is required")) return;
        if (!ValidationUtils.canadianPostalCode(etPostalCode)) return;
        if (!ValidationUtils.required(etCountry, "Country is required")) return;
        if (!ValidationUtils.phone(etPhone)) return;

        // ✅ SPINNER VALUE
        String locationType = spinnerLocationType.getSelectedItem() != null
                ? spinnerLocationType.getSelectedItem().toString()
                : "";

        if (locationType.isEmpty()) {
            Toast.makeText(this, "Please select a location type", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ CREATE REQUEST (AFTER validation)
        LocationRequest request = new LocationRequest(
                etLocationName.getText().toString().trim(),
                locationType,
                etStreet1.getText().toString().trim(),
                etStreet2.getText().toString().trim(),
                etCity.getText().toString().trim(),
                etProvince.getText().toString().trim(),
                etPostalCode.getText().toString().trim().toUpperCase(),
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
                        setResult(RESULT_OK);
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
                        setResult(RESULT_OK);
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

    private void setSpinnerSelection(String locationType) {
        if (locationType == null) return;

        for (int i = 0; i < spinnerLocationType.getCount(); i++) {
            String value = spinnerLocationType.getItemAtPosition(i).toString();
            if (value.equalsIgnoreCase(locationType)) {
                spinnerLocationType.setSelection(i);
                break;
            }
        }
    }
}
package com.example.workshop06;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.LocationRequest;
import com.example.workshop06.model.LocationResponse;
import com.example.workshop06.util.FormFormatUtils;
import com.example.workshop06.util.ValidationUtils;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationFormActivity extends BaseActivity {

    private EditText etLocationName, etStreet1, etStreet2, etCity,
            etProvince, etPostalCode, etCountry, etPhone;
    private MaterialAutoCompleteTextView spinnerLocationType, spinnerStatus;
    private Button btnSave;

    private Integer locationId = null;

    private ImageButton btnBack;

    @Override
    protected void onRefresh() {}

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_form);

        spinnerLocationType = findViewById(R.id.spinnerLocationType);
        spinnerStatus = findViewById(R.id.spinnerStatus);

        etLocationName = findViewById(R.id.etLocationName);
        etStreet1 = findViewById(R.id.etStreet1);
        etStreet2 = findViewById(R.id.etStreet2);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etPostalCode = findViewById(R.id.etPostalCode);
        etCountry = findViewById(R.id.etCountry);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSaveLocation);
        btnBack = findViewById(R.id.btnBack);

        setupLocationTypeDropdown();
        setupStatusDropdown();

        FormFormatUtils.attachCanadianPhoneFormatter(etPhone);
        FormFormatUtils.attachCanadianPostalCodeFormatter(etPostalCode);

        if (getIntent() != null && getIntent().hasExtra("locationId")) {
            int id = getIntent().getIntExtra("locationId", -1);
            if (id != -1) {
                locationId = id;
                loadLocation(id);
            }
        }
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveLocation());
    }

    private void setupLocationTypeDropdown() {
        String[] locationTypes = {"SalesPoint", "Warehouse", "Office"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                locationTypes
        );

        spinnerLocationType.setAdapter(adapter);

        spinnerLocationType.setOnClickListener(v -> spinnerLocationType.showDropDown());
        spinnerLocationType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                spinnerLocationType.showDropDown();
            }
        });
    }

    private void setupStatusDropdown() {
        String[] statuses = {"Active", "Inactive"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statuses
        );

        spinnerStatus.setAdapter(adapter);
        spinnerStatus.setText("Active", false);

        spinnerStatus.setOnClickListener(v -> spinnerStatus.showDropDown());
        spinnerStatus.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                spinnerStatus.showDropDown();
            }
        });
    }

    private void loadLocation(int id) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getLocationById(id).enqueue(new Callback<LocationResponse>() {
            @Override
            public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LocationResponse item = response.body();

                    etLocationName.setText(item.getLocationName() != null ? item.getLocationName() : "");
                    setLocationTypeSelection(item.getLocationType());

                    etStreet1.setText(item.getStreet1() != null ? item.getStreet1() : "");
                    etStreet2.setText(item.getStreet2() != null ? item.getStreet2() : "");
                    etCity.setText(item.getCity() != null ? item.getCity() : "");
                    etProvince.setText(item.getProvince() != null ? item.getProvince() : "");
                    etPostalCode.setText(item.getPostalCode() != null ? item.getPostalCode() : "");
                    etCountry.setText(item.getCountry() != null ? item.getCountry() : "");
                    etPhone.setText(item.getPhone() != null ? item.getPhone() : "");

                    boolean isActive = Boolean.TRUE.equals(item.getIsActive());
                    spinnerStatus.setText(isActive ? "Active" : "Inactive", false);
                } else {
                    Toast.makeText(LocationFormActivity.this, "Failed to load location", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LocationResponse> call, Throwable t) {
                Toast.makeText(LocationFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveLocation() {
        if (!ValidationUtils.required(etLocationName, "Location name is required")) return;
        if (!ValidationUtils.required(etStreet1, "Street 1 is required")) return;
        if (!ValidationUtils.required(etCity, "City is required")) return;
        if (!ValidationUtils.required(etProvince, "Province is required")) return;
        if (!ValidationUtils.canadianPostalCode(etPostalCode)) return;
        if (!ValidationUtils.required(etCountry, "Country is required")) return;
        if (!ValidationUtils.phone(etPhone)) return;

        String locationType = spinnerLocationType.getText() != null
                ? spinnerLocationType.getText().toString().trim()
                : "";

        String status = spinnerStatus.getText() != null
                ? spinnerStatus.getText().toString().trim()
                : "";

        if (locationType.isEmpty()) {
            spinnerLocationType.setError("Please select a location type");
            spinnerLocationType.requestFocus();
            return;
        }

        if (status.isEmpty()) {
            spinnerStatus.setError("Please select a status");
            spinnerStatus.requestFocus();
            return;
        }

        boolean isActive = status.equalsIgnoreCase("Active");

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
                isActive
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

    private void setLocationTypeSelection(String locationType) {
        if (locationType == null || locationType.trim().isEmpty()) return;
        spinnerLocationType.setText(locationType, false);
    }
}
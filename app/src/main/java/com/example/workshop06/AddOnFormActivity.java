package com.example.workshop06;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.AddOnRequest;
import com.example.workshop06.model.AddOnResponse;
import com.example.workshop06.model.ServiceTypeResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddOnFormActivity extends AppCompatActivity {

    private EditText etAddOnName;
    private EditText etMonthlyPrice;
    private EditText etDescription;
    private CheckBox cbActive;
    private Button btnSaveAddOn;
    private MaterialAutoCompleteTextView spinnerServiceType;

    private Integer addOnId = null;
    private AddOnResponse loadedAddOn = null;

    private final List<ServiceTypeResponse> serviceTypes = new ArrayList<>();
    private ArrayAdapter<String> serviceTypeAdapter;
    private int selectedServiceTypePosition = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_form);

        spinnerServiceType = findViewById(R.id.spinnerServiceType);
        etAddOnName = findViewById(R.id.etAddOnName);
        etMonthlyPrice = findViewById(R.id.etMonthlyPrice);
        etDescription = findViewById(R.id.etDescription);
        cbActive = findViewById(R.id.cbActive);
        btnSaveAddOn = findViewById(R.id.btnSaveAddOn);

        serviceTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        spinnerServiceType.setAdapter(serviceTypeAdapter);

        spinnerServiceType.setOnItemClickListener((parent, view, position, id) -> {
            selectedServiceTypePosition = position;
            spinnerServiceType.setError(null);
        });

        if (getIntent() != null && getIntent().hasExtra("addOnId")) {
            int id = getIntent().getIntExtra("addOnId", -1);
            if (id != -1) {
                addOnId = id;
                loadAddOn(id);
            }
        }

        loadServiceTypes();
        btnSaveAddOn.setOnClickListener(v -> saveAddOn());
    }

    private void loadAddOn(int id) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getAddOnById(id).enqueue(new Callback<AddOnResponse>() {
            @Override
            public void onResponse(Call<AddOnResponse> call, Response<AddOnResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadedAddOn = response.body();

                    etAddOnName.setText(loadedAddOn.getAddOnName() != null ? loadedAddOn.getAddOnName() : "");
                    etMonthlyPrice.setText(loadedAddOn.getMonthlyPrice() != null
                            ? String.valueOf(loadedAddOn.getMonthlyPrice()) : "");
                    etDescription.setText(loadedAddOn.getDescription() != null ? loadedAddOn.getDescription() : "");
                    cbActive.setChecked(Boolean.TRUE.equals(loadedAddOn.getIsActive()));

                    setServiceTypeSelectionForLoadedAddOn();
                } else {
                    Toast.makeText(AddOnFormActivity.this, "Failed to load add-on", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddOnResponse> call, Throwable t) {
                Toast.makeText(AddOnFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadServiceTypes() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getServiceTypes().enqueue(new Callback<List<ServiceTypeResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceTypeResponse>> call, Response<List<ServiceTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    serviceTypes.clear();
                    serviceTypes.addAll(response.body());

                    List<String> names = new ArrayList<>();
                    for (ServiceTypeResponse item : serviceTypes) {
                        names.add(item.getName());
                    }

                    serviceTypeAdapter.clear();
                    serviceTypeAdapter.addAll(names);
                    serviceTypeAdapter.notifyDataSetChanged();

                    setServiceTypeSelectionForLoadedAddOn();
                } else {
                    Toast.makeText(AddOnFormActivity.this, "Failed to load service types", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServiceTypeResponse>> call, Throwable t) {
                Toast.makeText(AddOnFormActivity.this, "Failed to load service types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setServiceTypeSelectionForLoadedAddOn() {
        if (loadedAddOn == null || loadedAddOn.getServiceTypeId() == null || serviceTypes.isEmpty()) {
            return;
        }

        for (int i = 0; i < serviceTypes.size(); i++) {
            Integer serviceTypeId = serviceTypes.get(i).getServiceTypeId();
            if (loadedAddOn.getServiceTypeId().equals(serviceTypeId)) {
                selectedServiceTypePosition = i;
                spinnerServiceType.setText(serviceTypes.get(i).getName(), false);
                break;
            }
        }
    }

    private void saveAddOn() {
        String name = etAddOnName.getText() != null ? etAddOnName.getText().toString().trim() : "";
        String monthlyPriceText = etMonthlyPrice.getText() != null ? etMonthlyPrice.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        if (selectedServiceTypePosition < 0 || selectedServiceTypePosition >= serviceTypes.size()) {
            spinnerServiceType.setError("Please select a service type");
            spinnerServiceType.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            etAddOnName.setError("Add-On name is required");
            etAddOnName.requestFocus();
            return;
        }

        if (monthlyPriceText.isEmpty()) {
            etMonthlyPrice.setError("Monthly price is required");
            etMonthlyPrice.requestFocus();
            return;
        }

        Double monthlyPrice;
        try {
            monthlyPrice = Double.parseDouble(monthlyPriceText);
        } catch (NumberFormatException e) {
            etMonthlyPrice.setError("Invalid monthly price");
            etMonthlyPrice.requestFocus();
            return;
        }

        Integer serviceTypeId = serviceTypes.get(selectedServiceTypePosition).getServiceTypeId();

        AddOnRequest request = new AddOnRequest(
                serviceTypeId,
                name,
                monthlyPrice,
                description,
                cbActive.isChecked(),
                null,
                null
        );

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if (addOnId == null) {
            apiService.createAddOn(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddOnFormActivity.this, "Add-on created", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddOnFormActivity.this, "Create failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(AddOnFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            apiService.updateAddOn(addOnId, request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddOnFormActivity.this, "Add-on updated", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddOnFormActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(AddOnFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
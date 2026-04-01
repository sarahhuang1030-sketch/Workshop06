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
import com.example.workshop06.model.AddOnRequest;
import com.example.workshop06.model.AddOnResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddOnFormActivity extends AppCompatActivity {

    private EditText etServiceTypeId;
    private EditText etAddOnName;
    private EditText etMonthlyPrice;
    private EditText etDescription;
    private CheckBox cbActive;
    private Button btnSaveAddOn;

    private Integer addOnId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_form);

        etServiceTypeId = findViewById(R.id.etServiceTypeId);
        etAddOnName = findViewById(R.id.etAddOnName);
        etMonthlyPrice = findViewById(R.id.etMonthlyPrice);
        etDescription = findViewById(R.id.etDescription);
        cbActive = findViewById(R.id.cbActive);
        btnSaveAddOn = findViewById(R.id.btnSaveAddOn);

        if (getIntent() != null && getIntent().hasExtra("addOnId")) {
            int id = getIntent().getIntExtra("addOnId", -1);
            if (id != -1) {
                addOnId = id;
                loadAddOn(id);
            }
        }

        btnSaveAddOn.setOnClickListener(v -> saveAddOn());
    }

    private void loadAddOn(int id) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getAddOnById(id).enqueue(new Callback<AddOnResponse>() {
            @Override
            public void onResponse(Call<AddOnResponse> call, Response<AddOnResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AddOnResponse item = response.body();

                    etServiceTypeId.setText(item.getServiceTypeId() != null
                            ? String.valueOf(item.getServiceTypeId()) : "");

                    etAddOnName.setText(item.getAddOnName() != null ? item.getAddOnName() : "");
                    etMonthlyPrice.setText(item.getMonthlyPrice() != null
                            ? String.valueOf(item.getMonthlyPrice()) : "");
                    etDescription.setText(item.getDescription() != null ? item.getDescription() : "");
                    cbActive.setChecked(Boolean.TRUE.equals(item.getIsActive()));
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

    private void saveAddOn() {
        String serviceTypeText = etServiceTypeId.getText().toString().trim();
        String name = etAddOnName.getText().toString().trim();
        String monthlyPriceText = etMonthlyPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

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

        Integer serviceTypeId = null;
        if (!serviceTypeText.isEmpty()) {
            try {
                serviceTypeId = Integer.parseInt(serviceTypeText);
            } catch (NumberFormatException e) {
                etServiceTypeId.setError("Invalid Service Type Id");
                etServiceTypeId.requestFocus();
                return;
            }
        }

        Double monthlyPrice;
        try {
            monthlyPrice = Double.parseDouble(monthlyPriceText);
        } catch (NumberFormatException e) {
            etMonthlyPrice.setError("Invalid monthly price");
            etMonthlyPrice.requestFocus();
            return;
        }

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
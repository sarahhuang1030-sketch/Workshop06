package com.example.workshop06;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.AddOnResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanAddOnFormActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView spinnerAddOn;
    private Button btnSave;

    private final List<AddOnResponse> addOns = new ArrayList<>();
    private int selectedAddOnPosition = -1;

    private int planId = -1;

    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_add_on_form);

        planId = getIntent().getIntExtra("planId", -1);

        spinnerAddOn = findViewById(R.id.spinnerAddOn);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        spinnerAddOn.setOnItemClickListener((parent, view, position, id) -> {
            selectedAddOnPosition = position;
            spinnerAddOn.setError(null);
        });

        btnSave.setOnClickListener(v -> attachAddOn());
        btnBack.setOnClickListener(v -> finish());
        loadAvailableAddOns();
    }

    private void loadAvailableAddOns() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getAddOns().enqueue(new Callback<List<AddOnResponse>>() {
            @Override
            public void onResponse(Call<List<AddOnResponse>> call, Response<List<AddOnResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    addOns.clear();
                    addOns.addAll(response.body());

                    List<String> names = new ArrayList<>();
                    for (AddOnResponse item : addOns) {
                        names.add(item.getAddOnName() != null ? item.getAddOnName() : "Unnamed Add-on");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            PlanAddOnFormActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            names
                    );
                    spinnerAddOn.setAdapter(adapter);
                } else {
                    Toast.makeText(PlanAddOnFormActivity.this, "Failed to load add-ons", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AddOnResponse>> call, Throwable t) {
                Toast.makeText(PlanAddOnFormActivity.this, "Unable to load add-ons", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attachAddOn() {
        if (planId <= 0) {
            Toast.makeText(this, "Invalid plan id", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedAddOnPosition < 0 || selectedAddOnPosition >= addOns.size()) {
            spinnerAddOn.setError("Please select an add-on");
            spinnerAddOn.requestFocus();
            return;
        }

        Integer addOnId = addOns.get(selectedAddOnPosition).getAddOnId();
        if (addOnId == null) {
            Toast.makeText(this, "Invalid add-on", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.attachAddOnToPlan(planId, addOnId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PlanAddOnFormActivity.this, "Add-on attached", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(PlanAddOnFormActivity.this,
                            "Attach failed. Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PlanAddOnFormActivity.this, "Unable to attach add-on", Toast.LENGTH_LONG).show();
            }
        });
    }
}
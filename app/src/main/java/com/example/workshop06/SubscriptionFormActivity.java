package com.example.workshop06;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.SubscriptionRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionFormActivity extends AppCompatActivity {

    private EditText etCustomerId, etPlanId, etStartDate, etEndDate, etStatus, etBillingCycleDay, etNotes;
    private Button btnSaveSubscription;

    private Integer subscriptionId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_form);

        etCustomerId = findViewById(R.id.etCustomerId);
        etPlanId = findViewById(R.id.etPlanId);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etStatus = findViewById(R.id.etStatus);
        etBillingCycleDay = findViewById(R.id.etBillingCycleDay);
        etNotes = findViewById(R.id.etNotes);
        btnSaveSubscription = findViewById(R.id.btnSaveSubscription);

        if (getIntent() != null && getIntent().hasExtra("subscriptionId")) {
            int id = getIntent().getIntExtra("subscriptionId", -1);
            if (id != -1) {
                subscriptionId = id;
                loadSubscription(id);
            }
        }

        btnSaveSubscription.setOnClickListener(v -> saveSubscription());
    }

    private void loadSubscription(int id) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getSubscriptionById(id).enqueue(new Callback<SubscriptionRequest>() {
            @Override
            public void onResponse(Call<SubscriptionRequest> call, Response<SubscriptionRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SubscriptionRequest item = response.body();

                    etCustomerId.setText(item.getCustomerId() != null ? String.valueOf(item.getCustomerId()) : "");
                    etPlanId.setText(item.getPlanId() != null ? String.valueOf(item.getPlanId()) : "");
                    etStartDate.setText(item.getStartDate() != null ? item.getStartDate() : "");
                    etEndDate.setText(item.getEndDate() != null ? item.getEndDate() : "");
                    etStatus.setText(item.getStatus() != null ? item.getStatus() : "");
                    etBillingCycleDay.setText(item.getBillingCycleDay() != null ? String.valueOf(item.getBillingCycleDay()) : "");
                    etNotes.setText(item.getNotes() != null ? item.getNotes() : "");
                } else {
                    Toast.makeText(SubscriptionFormActivity.this, "Failed to load subscription", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SubscriptionRequest> call, Throwable t) {
                Toast.makeText(SubscriptionFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveSubscription() {
        String customerIdText = etCustomerId.getText().toString().trim();
        String planIdText = etPlanId.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String status = etStatus.getText().toString().trim();
        String billingCycleDayText = etBillingCycleDay.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (customerIdText.isEmpty()) {
            etCustomerId.setError("Customer Id is required");
            etCustomerId.requestFocus();
            return;
        }

        if (planIdText.isEmpty()) {
            etPlanId.setError("Plan Id is required");
            etPlanId.requestFocus();
            return;
        }

        Integer customerId;
        Integer planId;
        Integer billingCycleDay = null;

        try {
            customerId = Integer.parseInt(customerIdText);
            planId = Integer.parseInt(planIdText);
            if (!billingCycleDayText.isEmpty()) {
                billingCycleDay = Integer.parseInt(billingCycleDayText);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Customer Id, Plan Id, and Billing Cycle Day must be numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        SubscriptionRequest request = new SubscriptionRequest(
                subscriptionId,
                customerId,
                planId,
                startDate.isEmpty() ? null : startDate,
                endDate.isEmpty() ? null : endDate,
                status.isEmpty() ? null : status,
                billingCycleDay,
                notes.isEmpty() ? null : notes
        );

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if (subscriptionId == null) {
            apiService.createSubscription(request).enqueue(new Callback<SubscriptionRequest>() {
                @Override
                public void onResponse(Call<SubscriptionRequest> call, Response<SubscriptionRequest> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(SubscriptionFormActivity.this, "Subscription created", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(SubscriptionFormActivity.this, "Create failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SubscriptionRequest> call, Throwable t) {
                    Toast.makeText(SubscriptionFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            apiService.updateSubscription(subscriptionId, request).enqueue(new Callback<SubscriptionRequest>() {
                @Override
                public void onResponse(Call<SubscriptionRequest> call, Response<SubscriptionRequest> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(SubscriptionFormActivity.this, "Subscription updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(SubscriptionFormActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SubscriptionRequest> call, Throwable t) {
                    Toast.makeText(SubscriptionFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
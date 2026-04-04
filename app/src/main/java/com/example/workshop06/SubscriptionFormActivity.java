package com.example.workshop06;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.SubscriptionRequest;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionFormActivity extends AppCompatActivity {

    private EditText etCustomerName, etPlanName, etStartDate, etEndDate, etBillingCycleDay, etNotes;
    private Spinner spinnerStatus;
    private Button btnSaveSubscription;
    private ImageButton btnStartDatePicker, btnEndDatePicker;

    private Integer subscriptionId = null;
    private Integer customerId = null;
    private Integer planId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_form);

        etCustomerName = findViewById(R.id.etCustomerName);
        etPlanName = findViewById(R.id.etPlanName);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etBillingCycleDay = findViewById(R.id.etBillingCycleDay);
        etNotes = findViewById(R.id.etNotes);

        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnStartDatePicker = findViewById(R.id.btnStartDatePicker);
        btnEndDatePicker = findViewById(R.id.btnEndDatePicker);
        btnSaveSubscription = findViewById(R.id.btnSaveSubscription);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Active", "Inactive"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        btnStartDatePicker.setOnClickListener(v -> showDatePicker(etStartDate));
        btnEndDatePicker.setOnClickListener(v -> showDatePicker(etEndDate));
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        if (getIntent() != null) {
            if (getIntent().hasExtra("subscriptionId")) {
                int id = getIntent().getIntExtra("subscriptionId", -1);
                if (id != -1) {
                    subscriptionId = id;
                }
            }

            if (getIntent().hasExtra("customerId")) {
                int id = getIntent().getIntExtra("customerId", -1);
                if (id != -1) customerId = id;
            }

            if (getIntent().hasExtra("planId")) {
                int id = getIntent().getIntExtra("planId", -1);
                if (id != -1) planId = id;
            }

            String customerName = getIntent().getStringExtra("customerName");
            String planName = getIntent().getStringExtra("planName");

            etCustomerName.setText(customerName != null ? customerName : "");
            etPlanName.setText(planName != null ? planName : "");
        }

        if (subscriptionId != null) {
            loadSubscription(subscriptionId);
        }

        btnSaveSubscription.setOnClickListener(v -> saveSubscription());
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );
                    targetEditText.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void loadSubscription(int id) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getSubscriptionById(id).enqueue(new Callback<SubscriptionRequest>() {
            @Override
            public void onResponse(Call<SubscriptionRequest> call, Response<SubscriptionRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SubscriptionRequest item = response.body();

                    customerId = item.getCustomerId();
                    planId = item.getPlanId();

                    etStartDate.setText(item.getStartDate() != null ? item.getStartDate() : "");
                    etEndDate.setText(item.getEndDate() != null ? item.getEndDate() : "");
                    etBillingCycleDay.setText(item.getBillingCycleDay() != null
                            ? String.valueOf(item.getBillingCycleDay())
                            : "");
                    etNotes.setText(item.getNotes() != null ? item.getNotes() : "");

                    String status = item.getStatus() != null ? item.getStatus().trim() : "";
                    if (status.equalsIgnoreCase("Active")) {
                        spinnerStatus.setSelection(0);
                    } else if (status.equalsIgnoreCase("Inactive")) {
                        spinnerStatus.setSelection(1);
                    }

                    if (etCustomerName.getText().toString().trim().isEmpty()) {
                        String customerName = getIntent().getStringExtra("customerName");
                        etCustomerName.setText(customerName != null ? customerName : "");
                    }

                    if (etPlanName.getText().toString().trim().isEmpty()) {
                        String planName = getIntent().getStringExtra("planName");
                        etPlanName.setText(planName != null ? planName : "");
                    }

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
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem() != null
                ? spinnerStatus.getSelectedItem().toString()
                : "";
        String billingCycleDayText = etBillingCycleDay.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (customerId == null) {
            Toast.makeText(this, "Customer is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (planId == null) {
            Toast.makeText(this, "Plan is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer billingCycleDay = null;
        try {
            if (!billingCycleDayText.isEmpty()) {
                billingCycleDay = Integer.parseInt(billingCycleDayText);
            }
        } catch (NumberFormatException e) {
            etBillingCycleDay.setError("Billing Cycle Day must be a number");
            etBillingCycleDay.requestFocus();
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
                        setResult(RESULT_OK);
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
                        setResult(RESULT_OK);
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
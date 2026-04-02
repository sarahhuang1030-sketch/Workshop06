package com.example.workshop06;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.ServiceAppointmentCreateUpdateRequest;
import com.example.workshop06.model.ServiceAppointmentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceAppointmentFormActivity extends AppCompatActivity {

    private EditText etTechnicianUserId, etAddressId, etLocationId, etScheduledStart, etScheduledEnd, etNotes;
    private Spinner spinnerLocationType, spinnerStatus;
    private Button btnSave;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private String mode = "add";
    private int requestId = -1;
    private int appointmentId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_appointment_form);

        initViews();
        setupSpinners();
        loadIntentData();
        setupButtons();
    }

    private void initViews() {
        etTechnicianUserId = findViewById(R.id.etTechnicianUserId);
        etAddressId = findViewById(R.id.etAddressId);
        etLocationId = findViewById(R.id.etLocationId);
        etScheduledStart = findViewById(R.id.etScheduledStart);
        etScheduledEnd = findViewById(R.id.etScheduledEnd);
        etNotes = findViewById(R.id.etNotes);
        spinnerLocationType = findViewById(R.id.spinnerLocationType);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinners() {
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"ONSITE", "INSTORE", "REMOTE"}
        );
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocationType.setAdapter(locationAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Scheduled", "Completed", "Cancelled", "Pending"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void loadIntentData() {
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        requestId = getIntent().getIntExtra("requestId", -1);
        appointmentId = getIntent().getIntExtra("appointmentId", -1);

        int technicianUserId = getIntent().getIntExtra("technicianUserId", Integer.MIN_VALUE);
        int addressId = getIntent().getIntExtra("addressId", Integer.MIN_VALUE);
        int locationId = getIntent().getIntExtra("locationId", Integer.MIN_VALUE);

        String locationType = getIntent().getStringExtra("locationType");
        String scheduledStart = getIntent().getStringExtra("scheduledStart");
        String scheduledEnd = getIntent().getStringExtra("scheduledEnd");
        String status = getIntent().getStringExtra("status");
        String notes = getIntent().getStringExtra("notes");

        if (technicianUserId != Integer.MIN_VALUE) etTechnicianUserId.setText(String.valueOf(technicianUserId));
        if (addressId != Integer.MIN_VALUE) etAddressId.setText(String.valueOf(addressId));
        if (locationId != Integer.MIN_VALUE) etLocationId.setText(String.valueOf(locationId));
        if (scheduledStart != null) etScheduledStart.setText(scheduledStart);
        if (scheduledEnd != null) etScheduledEnd.setText(scheduledEnd);
        if (notes != null) etNotes.setText(notes);

        setSpinnerValue(spinnerLocationType, locationType);
        setSpinnerValue(spinnerStatus, status);
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            String item = String.valueOf(spinner.getItemAtPosition(i));
            if (item.equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveAppointment());
    }

    private void saveAppointment() {
        if (requestId <= 0) {
            Toast.makeText(this, "Invalid request ID", Toast.LENGTH_LONG).show();
            return;
        }

        String technicianText = etTechnicianUserId.getText().toString().trim();
        String addressText = etAddressId.getText().toString().trim();
        String locationText = etLocationId.getText().toString().trim();
        String startText = etScheduledStart.getText().toString().trim();
        String endText = etScheduledEnd.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String locationType = spinnerLocationType.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();

        if (TextUtils.isEmpty(startText)) {
            etScheduledStart.setError("Start is required");
            etScheduledStart.requestFocus();
            return;
        }

        Integer technicianUserId = TextUtils.isEmpty(technicianText) ? null : Integer.parseInt(technicianText);
        Integer addressId = TextUtils.isEmpty(addressText) ? null : Integer.parseInt(addressText);
        Integer locationId = TextUtils.isEmpty(locationText) ? null : Integer.parseInt(locationText);

        ServiceAppointmentCreateUpdateRequest request =
                new ServiceAppointmentCreateUpdateRequest(
                        technicianUserId,
                        addressId,
                        locationId,
                        locationType,
                        startText,
                        endText,
                        status,
                        notes
                );

        showLoading(true);
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if ("edit".equalsIgnoreCase(mode) && appointmentId > 0) {
            apiService.updateServiceAppointment(requestId, appointmentId, request)
                    .enqueue(new Callback<ServiceAppointmentResponse>() {
                        @Override
                        public void onResponse(Call<ServiceAppointmentResponse> call, Response<ServiceAppointmentResponse> response) {
                            showLoading(false);
                            if (response.isSuccessful()) {
                                Toast.makeText(ServiceAppointmentFormActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(ServiceAppointmentFormActivity.this,
                                        "Update failed. Code: " + response.code(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ServiceAppointmentResponse> call, Throwable t) {
                            showLoading(false);
                            Toast.makeText(ServiceAppointmentFormActivity.this,
                                    "Unable to update appointment",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            apiService.createServiceAppointment(requestId, request)
                    .enqueue(new Callback<ServiceAppointmentResponse>() {
                        @Override
                        public void onResponse(Call<ServiceAppointmentResponse> call, Response<ServiceAppointmentResponse> response) {
                            showLoading(false);
                            if (response.isSuccessful()) {
                                Toast.makeText(ServiceAppointmentFormActivity.this, "Created successfully", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(ServiceAppointmentFormActivity.this,
                                        "Create failed. Code: " + response.code(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ServiceAppointmentResponse> call, Throwable t) {
                            showLoading(false);
                            Toast.makeText(ServiceAppointmentFormActivity.this,
                                    "Unable to create appointment",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
    }
}
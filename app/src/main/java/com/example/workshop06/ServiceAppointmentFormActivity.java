package com.example.workshop06;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.CustomerAddressResponse;
import com.example.workshop06.model.EmployeeResponse;
import com.example.workshop06.model.ServiceAppointmentCreateUpdateRequest;
import com.example.workshop06.model.ServiceAppointmentResponse;
import com.example.workshop06.model.ServiceRequestResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceAppointmentFormActivity extends AppCompatActivity {

    private TextInputLayout layoutRequestSpinner, layoutTechnicianSpinner, layoutAddressSpinner;
    private TextInputLayout tilScheduledStart, tilScheduledEnd;

    private MaterialAutoCompleteTextView spinnerRequest, spinnerTechnician, spinnerAddress;
    private MaterialAutoCompleteTextView spinnerLocationType, spinnerStatus;

    private EditText etTechnicianName, etAddressText;
    private EditText etTechnicianUserId, etAddressId, etLocationId, etScheduledStart, etScheduledEnd, etNotes;

    private Button btnSave;
    private ProgressBar progressBar;

    private String mode = "add";
    private int requestId = -1;
    private int appointmentId = -1;

    private final List<RequestOption> requestOptions = new ArrayList<>();
    private final List<TechnicianOption> technicianOptions = new ArrayList<>();
    private final List<AddressOption> addressOptions = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_appointment_form);

        initViews();
        setupDropdowns();
        loadIntentData();
        setupButtons();

        if ("edit".equalsIgnoreCase(mode)) {
            showEditMode();
        } else {
            showAddMode();
            loadServiceRequests();
            loadTechnicians();
        }
    }

    private void initViews() {
        layoutRequestSpinner = findViewById(R.id.layoutRequestSpinner);
        layoutTechnicianSpinner = findViewById(R.id.layoutTechnicianSpinner);
        layoutAddressSpinner = findViewById(R.id.layoutAddressSpinner);

        tilScheduledStart = findViewById(R.id.tilScheduledStart);
        tilScheduledEnd = findViewById(R.id.tilScheduledEnd);

        spinnerRequest = findViewById(R.id.spinnerRequest);
        spinnerTechnician = findViewById(R.id.spinnerTechnician);
        spinnerAddress = findViewById(R.id.spinnerAddress);
        spinnerLocationType = findViewById(R.id.spinnerLocationType);
        spinnerStatus = findViewById(R.id.spinnerStatus);

        etTechnicianName = findViewById(R.id.etTechnicianName);
        etAddressText = findViewById(R.id.etAddressText);

        etTechnicianUserId = findViewById(R.id.etTechnicianUserId);
        etAddressId = findViewById(R.id.etAddressId);
        etLocationId = findViewById(R.id.etLocationId);
        etScheduledStart = findViewById(R.id.etScheduledStart);
        etScheduledEnd = findViewById(R.id.etScheduledEnd);
        etNotes = findViewById(R.id.etNotes);

        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupDropdowns() {
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"ONSITE", "INSTORE", "REMOTE"}
        );
        spinnerLocationType.setAdapter(locationAdapter);
        spinnerLocationType.setOnClickListener(v -> spinnerLocationType.showDropDown());
        spinnerLocationType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) spinnerLocationType.showDropDown();
        });

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Scheduled", "Completed", "Cancelled", "Pending"}
        );
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setOnClickListener(v -> spinnerStatus.showDropDown());
        spinnerStatus.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) spinnerStatus.showDropDown();
        });
    }

    private void loadIntentData() {
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        requestId = getIntent().getIntExtra("requestId", -1);
        appointmentId = getIntent().getIntExtra("appointmentId", -1);

        int technicianUserId = getIntent().getIntExtra("technicianUserId", Integer.MIN_VALUE);
        int addressId = getIntent().getIntExtra("addressId", Integer.MIN_VALUE);
        int locationId = getIntent().getIntExtra("locationId", Integer.MIN_VALUE);

        String technicianName = getIntent().getStringExtra("technicianName");
        String addressText = getIntent().getStringExtra("addressText");
        String locationType = getIntent().getStringExtra("locationType");
        String scheduledStart = getIntent().getStringExtra("scheduledStart");
        String scheduledEnd = getIntent().getStringExtra("scheduledEnd");
        String status = getIntent().getStringExtra("status");
        String notes = getIntent().getStringExtra("notes");

        if (technicianUserId != Integer.MIN_VALUE) {
            etTechnicianUserId.setText(String.valueOf(technicianUserId));
        }
        if (addressId != Integer.MIN_VALUE) {
            etAddressId.setText(String.valueOf(addressId));
        }
        if (locationId != Integer.MIN_VALUE) {
            etLocationId.setText(String.valueOf(locationId));
        }

        if (scheduledStart != null) etScheduledStart.setText(scheduledStart);
        if (scheduledEnd != null) etScheduledEnd.setText(scheduledEnd);
        if (notes != null) etNotes.setText(notes);

        setDropdownValue(spinnerLocationType, locationType);
        setDropdownValue(spinnerStatus, status);

        etTechnicianName.setText(!TextUtils.isEmpty(technicianName) ? technicianName : "—");
        etAddressText.setText(!TextUtils.isEmpty(addressText) ? addressText : "—");
    }

    private void showAddMode() {
        layoutRequestSpinner.setVisibility(View.VISIBLE);
        layoutTechnicianSpinner.setVisibility(View.VISIBLE);
        layoutAddressSpinner.setVisibility(View.VISIBLE);

        etTechnicianName.setVisibility(View.GONE);
        etAddressText.setVisibility(View.GONE);
    }

    private void showEditMode() {
        layoutRequestSpinner.setVisibility(View.GONE);
        layoutTechnicianSpinner.setVisibility(View.GONE);
        layoutAddressSpinner.setVisibility(View.GONE);

        etTechnicianName.setVisibility(View.VISIBLE);
        etAddressText.setVisibility(View.VISIBLE);
    }

    private void setDropdownValue(MaterialAutoCompleteTextView dropdown, String value) {
        if (value == null || value.trim().isEmpty()) return;
        dropdown.setText(value, false);
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> saveAppointment());

        etScheduledStart.setOnClickListener(v -> showDateTimePicker(etScheduledStart));
        etScheduledEnd.setOnClickListener(v -> showDateTimePicker(etScheduledEnd));

        tilScheduledStart.setEndIconOnClickListener(v -> showDateTimePicker(etScheduledStart));
        tilScheduledEnd.setEndIconOnClickListener(v -> showDateTimePicker(etScheduledEnd));
    }

    private void showDateTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                String formatted = String.format(
                                        Locale.US,
                                        "%04d-%02d-%02dT%02d:%02d:00",
                                        year, month + 1, dayOfMonth, hourOfDay, minute
                                );
                                target.setText(formatted);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void loadServiceRequests() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getServiceRequests().enqueue(new Callback<List<ServiceRequestResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceRequestResponse>> call, Response<List<ServiceRequestResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                requestOptions.clear();
                List<String> labels = new ArrayList<>();

                for (ServiceRequestResponse item : response.body()) {
                    Integer reqId = item.getRequestId();
                    Integer customerId = item.getCustomerId();
                    String customerName = item.getCustomerName() != null ? item.getCustomerName().trim() : "Unknown Customer";
                    String requestType = item.getRequestType() != null ? item.getRequestType().trim() : "Request";

                    if (reqId != null && customerId != null) {
                        RequestOption option = new RequestOption(reqId, customerId, customerName, requestType);
                        requestOptions.add(option);
                        labels.add(option.label);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ServiceAppointmentFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        labels
                );
                spinnerRequest.setAdapter(adapter);

                if (requestId > 0) {
                    for (int i = 0; i < requestOptions.size(); i++) {
                        if (requestOptions.get(i).requestId == requestId) {
                            spinnerRequest.setText(requestOptions.get(i).label, false);
                            loadAddressesForCustomer(requestOptions.get(i).customerId);
                            break;
                        }
                    }
                }

                spinnerRequest.setOnClickListener(v -> spinnerRequest.showDropDown());
                spinnerRequest.setOnItemClickListener((parent, view, position, id) -> {
                    RequestOption selected = requestOptions.get(position);
                    requestId = selected.requestId;
                    loadAddressesForCustomer(selected.customerId);
                });
            }

            @Override
            public void onFailure(Call<List<ServiceRequestResponse>> call, Throwable t) {
                Toast.makeText(ServiceAppointmentFormActivity.this, "Unable to load service requests", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadTechnicians() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getEmployees().enqueue(new Callback<List<EmployeeResponse>>() {
            @Override
            public void onResponse(Call<List<EmployeeResponse>> call, Response<List<EmployeeResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                technicianOptions.clear();
                List<String> labels = new ArrayList<>();

                for (EmployeeResponse emp : response.body()) {
                    String role = emp.getRole() != null ? emp.getRole().trim() : "";
                    String firstName = emp.getFirstName() != null ? emp.getFirstName().trim() : "";
                    String lastName = emp.getLastName() != null ? emp.getLastName().trim() : "";
                    String fullName = (firstName + " " + lastName).trim();

                    if ((role.equalsIgnoreCase("SERVICE_TECHNICIAN")
                            || role.equalsIgnoreCase("Service Technician")
                            || role.equalsIgnoreCase("Technician"))
                            && emp.getEmployeeId() != null
                            && !fullName.isEmpty()) {

                        technicianOptions.add(new TechnicianOption(emp.getEmployeeId(), fullName));
                        labels.add(fullName);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ServiceAppointmentFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        labels
                );
                spinnerTechnician.setAdapter(adapter);

                spinnerTechnician.setOnClickListener(v -> spinnerTechnician.showDropDown());
                spinnerTechnician.setOnItemClickListener((parent, view, position, id) -> {
                    TechnicianOption selected = technicianOptions.get(position);
                    etTechnicianUserId.setText(String.valueOf(selected.technicianUserId));
                });
            }

            @Override
            public void onFailure(Call<List<EmployeeResponse>> call, Throwable t) {
                Toast.makeText(ServiceAppointmentFormActivity.this, "Unable to load technicians", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAddressesForCustomer(int customerId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getCustomerAddress(customerId).enqueue(new Callback<CustomerAddressResponse>() {
            @Override
            public void onResponse(Call<CustomerAddressResponse> call, Response<CustomerAddressResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    etAddressId.setText("");
                    spinnerAddress.setText("", false);
                    etAddressText.setText("—");

                    Toast.makeText(ServiceAppointmentFormActivity.this, "No address found", Toast.LENGTH_SHORT).show();
                    return;
                }

                CustomerAddressResponse address = response.body();

                if (address.getAddressId() != null) {
                    etAddressId.setText(String.valueOf(address.getAddressId()));
                } else {
                    etAddressId.setText("");
                }

                String addressLabel = buildAddressLabel(address);

                addressOptions.clear();
                if (address.getAddressId() != null) {
                    addressOptions.add(new AddressOption(address.getAddressId(), addressLabel));
                }

                spinnerAddress.setAdapter(new ArrayAdapter<>(
                        ServiceAppointmentFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        new String[]{addressLabel}
                ));
                spinnerAddress.setText(addressLabel, false);
                etAddressText.setText(addressLabel);

                spinnerAddress.setOnClickListener(v -> spinnerAddress.showDropDown());
                spinnerAddress.setOnItemClickListener((parent, view, position, id) -> {
                    AddressOption selected = addressOptions.get(position);
                    etAddressId.setText(String.valueOf(selected.addressId));
                    spinnerAddress.setText(selected.label, false);
                });
            }

            @Override
            public void onFailure(Call<CustomerAddressResponse> call, Throwable t) {
                etAddressId.setText("");
                spinnerAddress.setText("", false);
                etAddressText.setText("—");

                Toast.makeText(ServiceAppointmentFormActivity.this, "Failed to load address", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String buildAddressLabel(CustomerAddressResponse a) {
        String line1 = a.getStreet1() != null ? a.getStreet1().trim() : "";
        String city = a.getCity() != null ? a.getCity().trim() : "";
        String province = a.getProvince() != null ? a.getProvince().trim() : "";
        String postal = a.getPostalCode() != null ? a.getPostalCode().trim() : "";

        StringBuilder sb = new StringBuilder();
        if (!line1.isEmpty()) sb.append(line1);
        if (!city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        if (!province.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(province);
        }
        if (!postal.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(postal);
        }
        return sb.length() == 0 ? "Address #" + a.getAddressId() : sb.toString();
    }

    private void saveAppointment() {
        if (requestId <= 0) {
            Toast.makeText(this, "Please select a service request", Toast.LENGTH_LONG).show();
            return;
        }

        String technicianText = etTechnicianUserId.getText().toString().trim();
        String addressTextId = etAddressId.getText().toString().trim();
        String locationText = etLocationId.getText().toString().trim();
        String startText = etScheduledStart.getText().toString().trim();
        String endText = etScheduledEnd.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        String locationType = spinnerLocationType.getText() != null
                ? spinnerLocationType.getText().toString().trim()
                : "";
        String status = spinnerStatus.getText() != null
                ? spinnerStatus.getText().toString().trim()
                : "";

        if ("add".equalsIgnoreCase(mode)) {
            if (TextUtils.isEmpty(technicianText)) {
                Toast.makeText(this, "Please select a technician", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(addressTextId)) {
                Toast.makeText(this, "Please select an address", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (TextUtils.isEmpty(locationType)) {
            spinnerLocationType.setError("Please select a location type");
            spinnerLocationType.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(status)) {
            spinnerStatus.setError("Please select a status");
            spinnerStatus.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(startText)) {
            etScheduledStart.setError("Start is required");
            etScheduledStart.requestFocus();
            return;
        }

        Integer technicianUserId = TextUtils.isEmpty(technicianText) ? null : Integer.parseInt(technicianText);
        Integer addressId = TextUtils.isEmpty(addressTextId) ? null : Integer.parseInt(addressTextId);
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

    private static class RequestOption {
        final int requestId;
        final int customerId;
        final String label;

        RequestOption(int requestId, int customerId, String customerName, String requestType) {
            this.requestId = requestId;
            this.customerId = customerId;
            this.label = "Request #" + requestId + " - " + customerName + " - " + requestType;
        }
    }

    private static class TechnicianOption {
        final int technicianUserId;
        final String label;

        TechnicianOption(int technicianUserId, String label) {
            this.technicianUserId = technicianUserId;
            this.label = label;
        }
    }

    private static class AddressOption {
        final int addressId;
        final String label;

        AddressOption(int addressId, String label) {
            this.addressId = addressId;
            this.label = label;
        }
    }
}
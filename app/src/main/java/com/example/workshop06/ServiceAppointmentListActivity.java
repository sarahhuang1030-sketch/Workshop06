package com.example.workshop06;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.ServiceAppointmentAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.EmployeeResponse;
import com.example.workshop06.model.ServiceAppointmentResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceAppointmentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvSubtitle;
    private SearchView searchView;
    private FloatingActionButton fabAdd;

    private ServiceAppointmentAdapter adapter;
    private int requestId = -1;

    private MaterialAutoCompleteTextView spinnerStatusFilter;
    private MaterialAutoCompleteTextView spinnerLocationTypeFilter;
    private MaterialAutoCompleteTextView spinnerTechnicianFilter;

    private String currentSearch = "";
    private String selectedStatus = "All";
    private String selectedLocationType = "All";
    private String selectedTechnician = "All";

    private boolean isTechnician = false;

    private ImageButton btnBack;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadAppointments());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_appointment_list);

        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "");
        isTechnician = "Service Technician".equalsIgnoreCase(role);

        requestId = getIntent().getIntExtra("requestId", -1);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        initViews();
        setupRecyclerView();
        setupSearch();
        setupButtons();
        BottomNavHelper.setup(this,0);
        setupStaticFilters();
        setupTechnicianFilter();

        String customerName = getIntent().getStringExtra("customerName");
        String requestType = getIntent().getStringExtra("requestType");
        if (tvSubtitle != null) {
            tvSubtitle.setText((customerName != null ? customerName : "Service Request")
                    + (requestType != null ? " • " + requestType : ""));
        }

        if (requestId <= 0) {
            Toast.makeText(this, "Invalid request ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadAppointments();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewAppointments);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        searchView = findViewById(R.id.searchViewAppointment);
        fabAdd = findViewById(R.id.fabAdd);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        spinnerLocationTypeFilter = findViewById(R.id.spinnerLocationTypeFilter);
        spinnerTechnicianFilter = findViewById(R.id.spinnerTechnicianFilter);
    }

    private void setupRecyclerView() {
        adapter = new ServiceAppointmentAdapter(new ServiceAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onEdit(ServiceAppointmentResponse item) {
                if (isTechnician) return;

                Intent intent = new Intent(ServiceAppointmentListActivity.this, ServiceAppointmentFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("requestId", requestId);
                intent.putExtra("appointmentId", item.getAppointmentId());
                intent.putExtra("technicianUserId", item.getTechnicianUserId());
                intent.putExtra("addressId", item.getAddressId());
                intent.putExtra("locationId", item.getLocationId());
                intent.putExtra("locationType", item.getLocationType());
                intent.putExtra("scheduledStart", item.getScheduledStart());
                intent.putExtra("scheduledEnd", item.getScheduledEnd());
                intent.putExtra("status", item.getStatus());
                intent.putExtra("notes", item.getNotes());
                intent.putExtra("technicianName", item.getTechnicianName());
                intent.putExtra("addressText", item.getAddressText());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(ServiceAppointmentResponse item) {
                if (isTechnician || item.getAppointmentId() == null) return;

                new AlertDialog.Builder(ServiceAppointmentListActivity.this)
                        .setTitle("Delete Appointment")
                        .setMessage("Are you sure you want to delete this appointment?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteAppointment(item.getAppointmentId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        adapter.setReadOnlyMode(isTechnician);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchView == null) return;

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearch = query == null ? "" : query;
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearch = newText == null ? "" : newText;
                applyFilters();
                return true;
            }
        });
    }

    private void setupButtons() {
        if (fabAdd != null) {
            fabAdd.setVisibility(isTechnician ? View.GONE : View.VISIBLE);
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(ServiceAppointmentListActivity.this, ServiceAppointmentFormActivity.class);
                intent.putExtra("mode", "add");
                intent.putExtra("requestId", requestId);
                formLauncher.launch(intent);
            });
        }
    }

    private void loadAppointments() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getServiceAppointments(requestId).enqueue(new Callback<List<ServiceAppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceAppointmentResponse>> call, Response<List<ServiceAppointmentResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    showError("Failed to load appointments. Code: " + response.code());
                    return;
                }

                List<ServiceAppointmentResponse> data = response.body();
                adapter.setData(data);
                applyFilters();
            }

            @Override
            public void onFailure(Call<List<ServiceAppointmentResponse>> call, Throwable t) {
                showLoading(false);
                adapter.setData(null);
                showError("Unable to load appointments");
            }
        });
    }

    private void deleteAppointment(int appointmentId) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteServiceAppointment(requestId, appointmentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(ServiceAppointmentListActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadAppointments();
                } else {
                    showError("Delete failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Unable to delete appointment");
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (isLoading) tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(boolean isEmpty) {
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (progressBar.getVisibility() != View.VISIBLE) {
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void updateEmptyState() {
        showEmpty(adapter == null || adapter.getItemCount() == 0);
    }

    private void showError(String message) {
        updateEmptyState();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setupStaticFilters() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Scheduled", "Completed", "Cancelled", "Pending"}
        );
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setText("All", false);
        spinnerStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatus = parent.getItemAtPosition(position).toString();
            applyFilters();
        });

        ArrayAdapter<String> locationTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "ONSITE", "INSTORE", "REMOTE"}
        );
        spinnerLocationTypeFilter.setAdapter(locationTypeAdapter);
        spinnerLocationTypeFilter.setText("All", false);
        spinnerLocationTypeFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedLocationType = parent.getItemAtPosition(position).toString();
            applyFilters();
        });
    }

    private void setupTechnicianFilter() {
        if (spinnerTechnicianFilter == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getEmployees().enqueue(new Callback<List<EmployeeResponse>>() {
            @Override
            public void onResponse(Call<List<EmployeeResponse>> call, Response<List<EmployeeResponse>> response) {
                List<String> technicianList = new ArrayList<>();
                technicianList.add("All");

                if (response.isSuccessful() && response.body() != null) {
                    for (EmployeeResponse emp : response.body()) {
                        String role = emp.getRole() != null ? emp.getRole().trim() : "";

                        String firstName = emp.getFirstName() != null ? emp.getFirstName().trim() : "";
                        String lastName = emp.getLastName() != null ? emp.getLastName().trim() : "";
                        String fullName = (firstName + " " + lastName).trim();

                        if (role.equalsIgnoreCase("SERVICE_TECHNICIAN")
                                || role.equalsIgnoreCase("Service Technician")
                                || role.equalsIgnoreCase("Technician")) {

                            if (!fullName.isEmpty() && !technicianList.contains(fullName)) {
                                technicianList.add(fullName);
                            }
                        }
                    }
                }

                ArrayAdapter<String> technicianAdapter = new ArrayAdapter<>(
                        ServiceAppointmentListActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        technicianList
                );

                spinnerTechnicianFilter.setAdapter(technicianAdapter);
                spinnerTechnicianFilter.setText(
                        selectedTechnician != null && !selectedTechnician.trim().isEmpty()
                                ? selectedTechnician
                                : "All",
                        false
                );

                spinnerTechnicianFilter.setOnItemClickListener((parent, view, position, id) -> {
                    selectedTechnician = parent.getItemAtPosition(position).toString();
                    applyFilters();
                });
            }

            @Override
            public void onFailure(Call<List<EmployeeResponse>> call, Throwable t) {
                ArrayAdapter<String> technicianAdapter = new ArrayAdapter<>(
                        ServiceAppointmentListActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        new String[]{"All"}
                );
                spinnerTechnicianFilter.setAdapter(technicianAdapter);
                spinnerTechnicianFilter.setText("All", false);
            }
        });
    }

    private void applyFilters() {
        if (adapter == null) return;
        adapter.applyFilters(currentSearch, selectedStatus, selectedLocationType, selectedTechnician);
        showEmpty(adapter.getItemCount() == 0);
    }
}
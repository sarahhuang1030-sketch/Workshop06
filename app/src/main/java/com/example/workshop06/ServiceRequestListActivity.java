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
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.ServiceRequestAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.EmployeeResponse;
import com.example.workshop06.model.ServiceRequestResponse;
import com.example.workshop06.model.ServiceTicketDTO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceRequestListActivity extends BaseActivity {

    @Override
    protected void onRefresh() {
        loadServiceRequests();
    }

    private boolean isTechnician = false;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView txtServiceRequestTitle;
    private SearchView searchViewServiceRequest;
    private FloatingActionButton fabAdd;
    private MaterialAutoCompleteTextView spinnerTechnicianFilter;
    private MaterialAutoCompleteTextView spinnerPriorityFilter;
    private ImageButton btnBack;

    private ServiceRequestAdapter adapter;

    private String currentSearch = "";
    private String selectedTechnician = "All";
    private String selectedPriority = "All";

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> loadServiceRequests()
            );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_request_list);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "");
        isTechnician =
                "Service Technician".equalsIgnoreCase(role)
                        || "SERVICE_TECHNICIAN".equalsIgnoreCase(role)
                        || "Technician".equalsIgnoreCase(role);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupButtons();
        setupStaticPriorityFilter();
        setupTechnicianFilter();
        loadServiceRequests();

        BottomNavHelper.setup(this, 0);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewServiceRequests);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewServiceRequest = findViewById(R.id.searchViewServiceRequest);
        fabAdd = findViewById(R.id.fabAdd);
        spinnerTechnicianFilter = findViewById(R.id.spinnerTechnicianFilter);
        spinnerPriorityFilter = findViewById(R.id.spinnerPriorityFilter);
        txtServiceRequestTitle = findViewById(R.id.txtServiceRequestTitle);

        String viewMode = getIntent().getStringExtra("ViewMode");
        if (viewMode != null && !viewMode.trim().isEmpty() && !"Empty".equalsIgnoreCase(viewMode)) {
            txtServiceRequestTitle.setText(viewMode + " Requests");
        } else {
            txtServiceRequestTitle.setText("Service Requests");
        }
    }

    private void setupRecyclerView() {
        adapter = new ServiceRequestAdapter(new ServiceRequestAdapter.OnRequestActionListener() {
            @Override
            public void onEdit(ServiceRequestResponse item) {
                Intent intent = new Intent(ServiceRequestListActivity.this, ServiceRequestFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("requestId", item.getRequestId());
                intent.putExtra("customerId", item.getCustomerId());
                intent.putExtra("createdByUserId", item.getCreatedByUserId());
                intent.putExtra("assignedTechnicianUserId", item.getAssignedTechnicianUserId());
                intent.putExtra("requestType", item.getRequestType());
                intent.putExtra("priority", item.getPriority());
                intent.putExtra("status", item.getStatus());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("customerName", item.getCustomerName());
                intent.putExtra("createdByName", item.getCreatedByName());
                intent.putExtra("technicianName", item.getTechnicianName());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(ServiceRequestResponse item) {
                if (item.getRequestId() == null) return;

                new AlertDialog.Builder(ServiceRequestListActivity.this)
                        .setTitle("Delete Service Request")
                        .setMessage("Are you sure you want to delete this service request?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteServiceRequest(item.getRequestId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onAppointments(ServiceRequestResponse item) {
                if (item.getRequestId() == null) return;

                Intent intent = new Intent(ServiceRequestListActivity.this, ServiceAppointmentListActivity.class);
                intent.putExtra("requestId", item.getRequestId());
                intent.putExtra("customerName", item.getCustomerName());
                intent.putExtra("requestType", item.getRequestType());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("status", item.getStatus());
                intent.putExtra("assignedTechnicianUserId", item.getAssignedTechnicianUserId());
                intent.putExtra("technicianName", item.getTechnicianName());
                intent.putExtra("ViewMode", "");
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchViewServiceRequest == null) return;

        searchViewServiceRequest.clearFocus();
        searchViewServiceRequest.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(ServiceRequestListActivity.this, ServiceRequestFormActivity.class);
                intent.putExtra("mode", "add");
                formLauncher.launch(intent);
            });
        }
    }

    private void setupStaticPriorityFilter() {
        if (spinnerPriorityFilter == null) return;

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Low", "Medium", "High"}
        );

        spinnerPriorityFilter.setAdapter(priorityAdapter);
        spinnerPriorityFilter.setText("All", false);
        spinnerPriorityFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedPriority = parent.getItemAtPosition(position).toString();
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
                        ServiceRequestListActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        technicianList
                );

                spinnerTechnicianFilter.setAdapter(technicianAdapter);
                spinnerTechnicianFilter.setText(
                        selectedTechnician == null || selectedTechnician.trim().isEmpty()
                                ? "All"
                                : selectedTechnician,
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
                        ServiceRequestListActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        new String[]{"All"}
                );
                spinnerTechnicianFilter.setAdapter(technicianAdapter);
                spinnerTechnicianFilter.setText("All", false);
            }
        });
    }

    private void loadServiceRequests() {
        showLoading(true);
        showEmpty(false);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if (isTechnician) {
            apiService.getMyTickets().enqueue(new Callback<List<ServiceTicketDTO>>() {
                @Override
                public void onResponse(Call<List<ServiceTicketDTO>> call, Response<List<ServiceTicketDTO>> response) {
                    showLoading(false);

                    if (!response.isSuccessful() || response.body() == null) {
                        adapter.setData(null);
                        showError("Failed to load tickets. Code: " + response.code());
                        return;
                    }

                    List<ServiceRequestResponse> converted = new ArrayList<>();
                    for (ServiceTicketDTO t : response.body()) {
                        ServiceRequestResponse r = new ServiceRequestResponse();
                        r.setRequestId(t.getRequestId());
                        r.setCustomerId(t.getCustomerId());
                        r.setCustomerName(t.getCustomerName());
                        r.setRequestType(t.getRequestType());
                        r.setPriority(t.getPriority());
                        r.setStatus(t.getStatus());
                        r.setDescription(t.getDescription());
                        r.setAssignedTechnicianUserId(t.getTechnicianUserId());
                        r.setTechnicianName(t.getTechnicianName());
                        converted.add(r);
                    }

                    adapter.setData(converted);
                    applyFilters();
                }

                @Override
                public void onFailure(Call<List<ServiceTicketDTO>> call, Throwable t) {
                    showLoading(false);
                    adapter.setData(null);
                    showError("Unable to load tickets");
                }
            });
        } else {
            apiService.getServiceRequests().enqueue(new Callback<List<ServiceRequestResponse>>() {
                @Override
                public void onResponse(Call<List<ServiceRequestResponse>> call, Response<List<ServiceRequestResponse>> response) {
                    showLoading(false);

                    if (!response.isSuccessful()) {
                        adapter.setData(null);
                        showError("Failed to load service requests. Code: " + response.code());
                        return;
                    }

                    List<ServiceRequestResponse> data = response.body();
                    String viewMode = getIntent().getStringExtra("ViewMode");

                    if (data != null && "Completed".equalsIgnoreCase(viewMode)) {
                        List<ServiceRequestResponse> remove = new ArrayList<>();
                        for (ServiceRequestResponse sr : data) {
                            if (!"Completed".equalsIgnoreCase(sr.getStatus())) {
                                remove.add(sr);
                            }
                        }
                        data.removeAll(remove);
                    }

                    adapter.setData(data);
                    applyFilters();
                }

                @Override
                public void onFailure(Call<List<ServiceRequestResponse>> call, Throwable t) {
                    showLoading(false);
                    adapter.setData(null);
                    showError("Unable to load service requests");
                }
            });
        }
    }

    private void applyFilters() {
        if (adapter == null) return;
        adapter.applyFilters(currentSearch, selectedTechnician, selectedPriority);
        updateEmptyState();
    }

    private void deleteServiceRequest(int requestId) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteServiceRequest(requestId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(ServiceRequestListActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadServiceRequests();
                } else {
                    showError("Delete failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Unable to delete service request");
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        if (recyclerView != null) {
            recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }

        if (tvEmpty != null && isLoading) {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmpty(boolean isEmpty) {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        if (recyclerView != null && progressBar != null && progressBar.getVisibility() != View.VISIBLE) {
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
}
package com.example.workshop06;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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

import com.example.workshop06.adapter.EmployeeAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.EmployeeResponse;
import com.example.workshop06.model.LocationResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchViewEmployee;
    private FloatingActionButton fabAdd;

    private MaterialAutoCompleteTextView spinnerStatusFilter;
    private MaterialAutoCompleteTextView spinnerRoleFilter;

    private EmployeeAdapter adapter;
    private final List<EmployeeResponse> allEmployees = new ArrayList<>();
    private final Map<Integer, String> managerNameMap = new HashMap<>();
    private final Map<Integer, String> locationNameMap = new HashMap<>();
    private boolean filtersReady = false;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadEmployees());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupFilterControls();
        setupButtons();
        loadEmployees();

        BottomNavHelper.setup(this, 0);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewEmployees);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewEmployee = findViewById(R.id.searchViewEmployee);
        fabAdd = findViewById(R.id.fabAdd);

        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        spinnerRoleFilter = findViewById(R.id.spinnerRoleFilter);
    }

    private void setupRecyclerView() {
        adapter = new EmployeeAdapter(new EmployeeAdapter.OnEmployeeActionListener() {
            @Override
            public void onEdit(EmployeeResponse item) {
                Intent intent = new Intent(EmployeeListActivity.this, EmployeeFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("employeeId", item.getEmployeeId());
                intent.putExtra("primaryLocationId", item.getPrimaryLocationId());
                intent.putExtra("firstName", item.getFirstName());
                intent.putExtra("lastName", item.getLastName());
                intent.putExtra("email", item.getEmail());
                intent.putExtra("phone", item.getPhone());
                intent.putExtra("role", item.getRole());
                intent.putExtra("salary", item.getSalary() != null ? item.getSalary() : Double.NaN);
                intent.putExtra("hireDate", item.getHireDate());
                intent.putExtra("status", item.getStatus());
                intent.putExtra("active", item.getActive() != null ? item.getActive() : Integer.MIN_VALUE);
                intent.putExtra("managerId", item.getManagerId() != null ? item.getManagerId() : Integer.MIN_VALUE);
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(EmployeeResponse item) {
                if (item.getEmployeeId() == null) return;

                new AlertDialog.Builder(EmployeeListActivity.this)
                        .setTitle("Delete Employee")
                        .setMessage("Are you sure you want to delete this employee?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteEmployee(item.getEmployeeId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchViewEmployee == null) return;

        searchViewEmployee.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });
    }

    private void setupFilterControls() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Active", "Inactive"}
        );
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setText("All", false);

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All"}
        );
        spinnerRoleFilter.setAdapter(roleAdapter);
        spinnerRoleFilter.setText("All", false);

        spinnerStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            if (filtersReady) applyFilters();
        });

        spinnerRoleFilter.setOnItemClickListener((parent, view, position, id) -> {
            if (filtersReady) applyFilters();
        });

        filtersReady = true;
    }

    private void updateRoleDropdown(List<EmployeeResponse> data) {
        Set<String> roles = new LinkedHashSet<>();
        roles.add("All");

        if (data != null) {
            for (EmployeeResponse item : data) {
                if (item.getRole() != null && !item.getRole().trim().isEmpty()) {
                    roles.add(item.getRole().trim());
                }
            }
        }

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(roles)
        );
        spinnerRoleFilter.setAdapter(roleAdapter);

        String current = spinnerRoleFilter.getText() != null
                ? spinnerRoleFilter.getText().toString().trim()
                : "";

        if (current.isEmpty()) {
            spinnerRoleFilter.setText("All", false);
        }
    }

    private void setupButtons() {
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(EmployeeListActivity.this, EmployeeFormActivity.class);
                intent.putExtra("mode", "add");
                formLauncher.launch(intent);
            });
        }
    }

    private void loadEmployees() {
        showLoading(true);
        showEmpty(false);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getEmployees().enqueue(new Callback<List<EmployeeResponse>>() {
            @Override
            public void onResponse(Call<List<EmployeeResponse>> call, Response<List<EmployeeResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    showError("Failed to load employees. Code: " + response.code());
                    return;
                }

                List<EmployeeResponse> data = response.body();
                allEmployees.clear();
                managerNameMap.clear();

                if (data != null) {
                    allEmployees.addAll(data);

                    for (EmployeeResponse item : data) {
                        if (item.getEmployeeId() != null) {
                            String firstName = item.getFirstName() != null ? item.getFirstName().trim() : "";
                            String lastName = item.getLastName() != null ? item.getLastName().trim() : "";
                            String fullName = (firstName + " " + lastName).trim();

                            if (fullName.isEmpty()) {
                                fullName = item.getEmail() != null ? item.getEmail() : "Employee #" + item.getEmployeeId();
                            }

                            managerNameMap.put(item.getEmployeeId(), fullName);
                        }
                    }
                }

                adapter.setManagerNameMap(managerNameMap);
                updateRoleDropdown(allEmployees);
                loadLocationsForNames();
            }

            @Override
            public void onFailure(Call<List<EmployeeResponse>> call, Throwable t) {
                showLoading(false);
                allEmployees.clear();
                adapter.setData(null);
                showError("Unable to load employees");
            }
        });
    }

    private void loadLocationsForNames() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getLocations().enqueue(new Callback<List<LocationResponse>>() {
            @Override
            public void onResponse(Call<List<LocationResponse>> call, Response<List<LocationResponse>> response) {
                locationNameMap.clear();

                if (response.isSuccessful() && response.body() != null) {
                    for (LocationResponse item : response.body()) {
                        if (item.getLocationId() != null) {
                            locationNameMap.put(
                                    item.getLocationId(),
                                    item.getLocationName() != null && !item.getLocationName().trim().isEmpty()
                                            ? item.getLocationName().trim()
                                            : "Location #" + item.getLocationId()
                            );
                        }
                    }
                }

                adapter.setLocationNameMap(locationNameMap);
                applyFilters();
            }

            @Override
            public void onFailure(Call<List<LocationResponse>> call, Throwable t) {
                adapter.setLocationNameMap(locationNameMap);
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        String query = searchViewEmployee.getQuery() != null
                ? searchViewEmployee.getQuery().toString().trim().toLowerCase()
                : "";

        String selectedStatus = spinnerStatusFilter.getText() != null
                ? spinnerStatusFilter.getText().toString().trim()
                : "All";

        String selectedRole = spinnerRoleFilter.getText() != null
                ? spinnerRoleFilter.getText().toString().trim()
                : "All";

        List<EmployeeResponse> filtered = new ArrayList<>();

        for (EmployeeResponse item : allEmployees) {
            String firstName = safe(item.getFirstName());
            String lastName = safe(item.getLastName());
            String fullName = (firstName + " " + lastName).trim();
            String email = safe(item.getEmail());
            String phone = safe(item.getPhone());
            String role = safe(item.getRole());
            String status = safe(item.getStatus());

            boolean matchesSearch =
                    query.isEmpty()
                            || fullName.toLowerCase().contains(query)
                            || firstName.toLowerCase().contains(query)
                            || lastName.toLowerCase().contains(query)
                            || email.toLowerCase().contains(query)
                            || phone.toLowerCase().contains(query);

            boolean matchesStatus =
                    selectedStatus.isEmpty()
                            || selectedStatus.equalsIgnoreCase("All")
                            || status.equalsIgnoreCase(selectedStatus);

            boolean matchesRole =
                    selectedRole.isEmpty()
                            || selectedRole.equalsIgnoreCase("All")
                            || role.equalsIgnoreCase(selectedRole);

            if (matchesSearch && matchesStatus && matchesRole) {
                filtered.add(item);
            }
        }

        adapter.setData(filtered);
        updateEmptyState();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void deleteEmployee(int employeeId) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteEmployee(employeeId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(EmployeeListActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadEmployees();
                } else {
                    showError("Delete failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Unable to delete employee");
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
package com.example.workshop06;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.workshop06.adapter.CustomerAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.CustomerResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerListActivity extends AppCompatActivity {

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadCustomers();
            refreshHandler.postDelayed(this, 30000);
        }
    };

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchViewCustomer;
    private BottomNavigationView bottomNavigation;

    private MaterialAutoCompleteTextView spinnerStatusFilter;
    private MaterialAutoCompleteTextView spinnerTypeFilter;

    private FloatingActionButton fabAdd;
    private CustomerAdapter adapter;

    private String currentSearchText = "";
    private String currentStatusFilter = "All Status";
    private String currentTypeFilter = "All Types";

    private ImageButton btnBack;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadCustomers());

    @Override
    protected void onResume() {
        super.onResume();
        refreshHandler.postDelayed(refreshRunnable, 30000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnBack = findViewById(R.id.btnBack);

        initViews();
        setupRecyclerView();

        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "");
        boolean isTechnician = "Service Technician".equalsIgnoreCase(role);

        adapter.setReadOnlyMode(isTechnician);

        if (fabAdd != null) {
            fabAdd.setVisibility(isTechnician ? View.GONE : View.VISIBLE);
        }
        btnBack.setOnClickListener(v -> finish());
        setupFilterDropdowns();
        setupSearch();
        setupFilterInputs();
        setupButtons();
        loadCustomers();
        BottomNavHelper.setup(this, R.id.nav_customers);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewCustomers);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewCustomer = findViewById(R.id.searchViewCustomer);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        spinnerTypeFilter = findViewById(R.id.spinnerTypeFilter);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        adapter = new CustomerAdapter(new CustomerAdapter.OnCustomerActionListener() {
            @Override
            public void onEdit(CustomerResponse item) {
                Intent intent = new Intent(CustomerListActivity.this, CustomerFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("customerId", item.getCustomerId());
                intent.putExtra("customerType", item.getCustomerType());
                intent.putExtra("firstName", item.getFirstName());
                intent.putExtra("lastName", item.getLastName());
                intent.putExtra("businessName", item.getBusinessName());
                intent.putExtra("email", item.getEmail());
                intent.putExtra("homePhone", item.getHomePhone());
                intent.putExtra("status", item.getStatus());
                intent.putExtra("assignedEmployeeId", item.getAssignedEmployeeId());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(CustomerResponse item) {
                if (item.getCustomerId() == null) return;

                new AlertDialog.Builder(CustomerListActivity.this)
                        .setTitle("Delete Customer")
                        .setMessage("Are you sure you want to delete this customer?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteCustomer(item.getCustomerId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onAddress(CustomerResponse item) {
                if (item.getCustomerId() == null) return;

                SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
                String role = prefs.getString("user_role", "");
                boolean isTechnician = "Service Technician".equalsIgnoreCase(role);

                String customerName;
                if ("Business".equalsIgnoreCase(item.getCustomerType())) {
                    customerName = item.getBusinessName() != null ? item.getBusinessName() : "";
                } else {
                    customerName = ((item.getFirstName() != null ? item.getFirstName() : "") + " "
                            + (item.getLastName() != null ? item.getLastName() : "")).trim();
                }

                Intent intent = new Intent(CustomerListActivity.this, CustomerAddressFormActivity.class);
                intent.putExtra("customerId", item.getCustomerId());
                intent.putExtra("customerName", customerName);
                intent.putExtra("readOnly", isTechnician);

                if (isTechnician) {
                    startActivity(intent);
                } else {
                    formLauncher.launch(intent);
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterDropdowns() {
        String[] statusItems = {"All Status", "Active", "Inactive"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, statusItems);
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setText("All Status", false);

        String[] typeItems = {"All Types", "Individual", "Business"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, typeItems);
        spinnerTypeFilter.setAdapter(typeAdapter);
        spinnerTypeFilter.setText("All Types", false);

        spinnerStatusFilter.setOnClickListener(v -> spinnerStatusFilter.showDropDown());
        spinnerTypeFilter.setOnClickListener(v -> spinnerTypeFilter.showDropDown());
    }

    private void setupSearch() {
        searchViewCustomer.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchText = query != null ? query : "";
                applyCurrentFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchText = newText != null ? newText : "";
                applyCurrentFilters();
                return true;
            }
        });
    }

    private void setupFilterInputs() {
        if (spinnerStatusFilter != null) {
            spinnerStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
                currentStatusFilter = parent.getItemAtPosition(position).toString();
                applyCurrentFilters();
            });

            spinnerStatusFilter.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentStatusFilter = s != null && !s.toString().trim().isEmpty()
                            ? s.toString().trim()
                            : "All Status";
                    applyCurrentFilters();
                }
            });
        }

        if (spinnerTypeFilter != null) {
            spinnerTypeFilter.setOnItemClickListener((parent, view, position, id) -> {
                currentTypeFilter = parent.getItemAtPosition(position).toString();
                applyCurrentFilters();
            });

            spinnerTypeFilter.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentTypeFilter = s != null && !s.toString().trim().isEmpty()
                            ? s.toString().trim()
                            : "All Types";
                    applyCurrentFilters();
                }
            });
        }
    }

    private void setupButtons() {
        if (fabAdd != null && fabAdd.getVisibility() == View.VISIBLE) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerListActivity.this, CustomerFormActivity.class);
                intent.putExtra("mode", "add");
                formLauncher.launch(intent);
            });
        }
    }

    private void applyCurrentFilters() {
        currentStatusFilter = spinnerStatusFilter != null && spinnerStatusFilter.getText() != null
                && !spinnerStatusFilter.getText().toString().trim().isEmpty()
                ? spinnerStatusFilter.getText().toString().trim()
                : "All Status";

        currentTypeFilter = spinnerTypeFilter != null && spinnerTypeFilter.getText() != null
                && !spinnerTypeFilter.getText().toString().trim().isEmpty()
                ? spinnerTypeFilter.getText().toString().trim()
                : "All Types";

        adapter.applyFilters(currentSearchText, currentStatusFilter, currentTypeFilter);
        updateEmptyState();
    }

    private void loadCustomers() {
        if (progressBar.getVisibility() != View.VISIBLE && (adapter == null || adapter.getItemCount() == 0)) {
            showLoading(true);
            showEmpty(false);
        }

        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "");
        boolean isTechnician = "Service Technician".equalsIgnoreCase(role);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        Call<List<CustomerResponse>> call = isTechnician
                ? apiService.getCustomersForTechnician()
                : apiService.getCustomers();

        call.enqueue(new Callback<List<CustomerResponse>>() {
            @Override
            public void onResponse(Call<List<CustomerResponse>> call, Response<List<CustomerResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    showError("Failed to load customers. Code: " + response.code());
                    return;
                }

                List<CustomerResponse> data = response.body();
                adapter.setData(data);
                applyCurrentFilters();
                showEmpty(adapter.getItemCount() == 0);
            }

            @Override
            public void onFailure(Call<List<CustomerResponse>> call, Throwable t) {
                showLoading(false);
                adapter.setData(null);
                showError("Unable to load customers");
            }
        });
    }

    private void deleteCustomer(int customerId) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteCustomer(customerId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(CustomerListActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadCustomers();
                } else {
                    showError("Delete failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Unable to delete customer");
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
}
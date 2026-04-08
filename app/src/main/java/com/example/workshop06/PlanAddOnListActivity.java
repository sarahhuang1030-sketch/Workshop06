package com.example.workshop06;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import com.example.workshop06.adapter.PlanAddOnAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.AddOnResponse;
import com.example.workshop06.model.ServiceTypeResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanAddOnListActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private FloatingActionButton fabAdd;

    private MaterialAutoCompleteTextView spinnerStatusFilter;
    private MaterialAutoCompleteTextView spinnerServiceTypeFilter;

    private PlanAddOnAdapter adapter;

    private ImageButton btnBack;

    private final List<ServiceTypeResponse> serviceTypes = new ArrayList<>();

    private int planId = -1;
    private String planName = "Plan Add-ons";

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadPlanAddOns());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_add_on_list);
        btnBack = findViewById(R.id.btnBack);
        readExtras();
        initViews();
        setupRecyclerView();
        setupSearch();
        setupButtons();
        setupFilters();
        loadServiceTypes();
        loadPlanAddOns();
        btnBack.setOnClickListener(v -> finish());
        BottomNavHelper.setup(this, 0);
    }

    private void readExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            planId = intent.getIntExtra("planId", -1);
            String extraPlanName = intent.getStringExtra("planName");
            if (extraPlanName != null && !extraPlanName.trim().isEmpty()) {
                planName = extraPlanName;
            }
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerViewPlanAddOns);
        searchView = findViewById(R.id.searchViewAddOn);
        fabAdd = findViewById(R.id.fabAdd);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        spinnerServiceTypeFilter = findViewById(R.id.spinnerServiceTypeFilter);

        tvTitle.setText(planName);
        tvSubtitle.setText("Manage add-ons for this plan");
    }

    private void setupRecyclerView() {
        adapter = new PlanAddOnAdapter(new PlanAddOnAdapter.OnPlanAddOnActionListener() {
            @Override
            public void onRemove(AddOnResponse item) {
                if (item.getAddOnId() == null) return;

                new AlertDialog.Builder(PlanAddOnListActivity.this)
                        .setTitle("Remove Add-on")
                        .setMessage("Are you sure you want to remove this add-on from the plan?")
                        .setPositiveButton("Remove", (dialog, which) -> removeAddOnFromPlan(item.getAddOnId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchView == null) return;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    private void setupButtons() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(PlanAddOnListActivity.this, PlanAddOnFormActivity.class);
            intent.putExtra("planId", planId);
            intent.putExtra("planName", planName);
            formLauncher.launch(intent);
        });
    }

    private void setupFilters() {
        android.widget.ArrayAdapter<String> statusAdapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Active", "Inactive"}
        );
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setText("All", false);
        spinnerStatusFilter.setOnItemClickListener((parent, view, position, id) -> applyFilters());
    }

    private void loadServiceTypes() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getServiceTypes().enqueue(new Callback<List<ServiceTypeResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceTypeResponse>> call, Response<List<ServiceTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    serviceTypes.clear();
                    serviceTypes.addAll(response.body());
                    adapter.setServiceTypes(serviceTypes);

                    List<String> names = new ArrayList<>();
                    names.add("All");
                    for (ServiceTypeResponse item : serviceTypes) {
                        names.add(item.getName());
                    }

                    android.widget.ArrayAdapter<String> serviceTypeAdapter = new android.widget.ArrayAdapter<>(
                            PlanAddOnListActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            names
                    );
                    spinnerServiceTypeFilter.setAdapter(serviceTypeAdapter);
                    spinnerServiceTypeFilter.setText("All", false);
                    spinnerServiceTypeFilter.setOnItemClickListener((parent, view, position, id) -> applyFilters());
                }
            }

            @Override
            public void onFailure(Call<List<ServiceTypeResponse>> call, Throwable t) {
                Toast.makeText(PlanAddOnListActivity.this, "Failed to load service types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlanAddOns() {
        if (planId <= 0) {
            Toast.makeText(this, "Invalid plan id", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        showLoading(true);
        showEmpty(false);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getAddOnsByPlanId(planId).enqueue(new Callback<List<AddOnResponse>>() {
            @Override
            public void onResponse(Call<List<AddOnResponse>> call, Response<List<AddOnResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    adapter.setData(null);
                    showError("Failed to load add-ons. Code: " + response.code());
                    return;
                }

                adapter.setData(response.body());
                applyFilters();
            }

            @Override
            public void onFailure(Call<List<AddOnResponse>> call, Throwable t) {
                showLoading(false);
                adapter.setData(null);
                showError("Unable to load add-ons");
            }
        });
    }

    private void removeAddOnFromPlan(int addOnId) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.removeAddOnFromPlan(planId, addOnId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(PlanAddOnListActivity.this, "Removed successfully", Toast.LENGTH_SHORT).show();
                    loadPlanAddOns();
                } else {
                    showError("Remove failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Unable to remove add-on");
            }
        });
    }

    private void applyFilters() {
        String query = searchView.getQuery() != null ? searchView.getQuery().toString().trim().toLowerCase() : "";
        String status = spinnerStatusFilter.getText() != null ? spinnerStatusFilter.getText().toString().trim() : "All";
        String serviceType = spinnerServiceTypeFilter.getText() != null ? spinnerServiceTypeFilter.getText().toString().trim() : "All";

        List<AddOnResponse> source = new ArrayList<>(getAdapterData());
        List<AddOnResponse> filtered = new ArrayList<>();

        for (AddOnResponse item : source) {
            String name = item.getAddOnName() != null ? item.getAddOnName().toLowerCase() : "";
            String description = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
            String itemServiceType = getServiceTypeName(item.getServiceTypeId());

            boolean matchesQuery = query.isEmpty() || name.contains(query) || description.contains(query);

            boolean matchesStatus = true;
            if ("Active".equalsIgnoreCase(status)) {
                matchesStatus = Boolean.TRUE.equals(item.getIsActive());
            } else if ("Inactive".equalsIgnoreCase(status)) {
                matchesStatus = !Boolean.TRUE.equals(item.getIsActive());
            }

            boolean matchesServiceType = "All".equalsIgnoreCase(serviceType)
                    || serviceType.equalsIgnoreCase(itemServiceType);

            if (matchesQuery && matchesStatus && matchesServiceType) {
                filtered.add(item);
            }
        }

        adapter.setData(filtered);
        updateEmptyState();
    }

    private List<AddOnResponse> getAdapterData() {
        try {
            java.lang.reflect.Field field = PlanAddOnAdapter.class.getDeclaredField("fullList");
            field.setAccessible(true);
            Object value = field.get(adapter);
            if (value instanceof List) {
                return (List<AddOnResponse>) value;
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }

    private String getServiceTypeName(Integer id) {
        if (id == null) return "";
        for (ServiceTypeResponse s : serviceTypes) {
            if (id.equals(s.getServiceTypeId())) {
                return s.getName() != null ? s.getName() : "";
            }
        }
        return "";
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (isLoading) {
            tvEmpty.setVisibility(View.GONE);
        }
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
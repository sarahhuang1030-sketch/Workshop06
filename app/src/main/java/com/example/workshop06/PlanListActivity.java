package com.example.workshop06;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

import com.example.workshop06.adapter.PlanManagerAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.PlanResponse;
import com.example.workshop06.model.ServiceTypeResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanListActivity extends AppCompatActivity {

    private List<ServiceTypeResponse> serviceTypes;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchViewPlan;
    private FloatingActionButton fabAdd;

    private EditText etMinAmount, etMaxAmount;
    private MaterialAutoCompleteTextView spinnerStatus, spinnerTerm;
    private BottomNavigationView bottomNavigation;
    private PlanManagerAdapter adapter;
    private boolean filtersReady = false;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadPlans());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupFilterControls();
        setupButtons();
        loadPlans();
        loadServiceTypes();
        BottomNavHelper.setup(this, R.id.nav_plans);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewPlans);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewPlan = findViewById(R.id.searchViewPlan);
        fabAdd = findViewById(R.id.fabAdd);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        etMinAmount = findViewById(R.id.etMinAmount);
        etMaxAmount = findViewById(R.id.etMaxAmount);

        spinnerStatus = findViewById(R.id.spinnerStatusFilter);
        spinnerTerm = findViewById(R.id.spinnerTerm);
    }

    private void setupRecyclerView() {
        adapter = new PlanManagerAdapter(new PlanManagerAdapter.OnPlanActionListener() {
            @Override
            public void onEdit(PlanResponse item) {
                Intent intent = new Intent(PlanListActivity.this, PlanFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("planId", item.getPlanId());
                intent.putExtra("serviceTypeId", item.getServiceTypeId());
                intent.putExtra("planName", item.getPlanName());
                intent.putExtra("monthlyPrice", item.getMonthlyPrice() != null ? item.getMonthlyPrice() : Double.NaN);
                intent.putExtra("contractTermMonths", item.getContractTermMonths() != null ? item.getContractTermMonths() : Integer.MIN_VALUE);
                intent.putExtra("description", item.getDescription());
                intent.putExtra("isActive", item.getIsActive() != null ? item.getIsActive() : Integer.MIN_VALUE);
                intent.putExtra("tagline", item.getTagline());
                intent.putExtra("badge", item.getBadge());
                intent.putExtra("iconKey", item.getIconKey());
                intent.putExtra("themeKey", item.getThemeKey());
                intent.putExtra("dataLabel", item.getDataLabel());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(PlanResponse item) {
                if (item.getPlanId() == null) return;

                new AlertDialog.Builder(PlanListActivity.this)
                        .setTitle("Delete Plan")
                        .setMessage("Are you sure you want to delete this plan?")
                        .setPositiveButton("Delete", (dialog, which) -> deletePlan(item.getPlanId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onManageAddOns(PlanResponse item) {
                if (item.getPlanId() == null) return;

                Intent intent = new Intent(PlanListActivity.this, PlanAddOnListActivity.class);
                intent.putExtra("planId", item.getPlanId());
                intent.putExtra("planName", item.getPlanName());
                startActivity(intent);
            }
        });



        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchViewPlan == null) return;

        searchViewPlan.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                Arrays.asList("All", "Active", "Inactive")
        );
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setText("All", false);

        ArrayAdapter<String> termAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Arrays.asList("All", "1", "6", "12", "24", "36")
        );
        spinnerTerm.setAdapter(termAdapter);
        spinnerTerm.setText("All", false);

        TextWatcher amountWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (filtersReady) applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        etMinAmount.addTextChangedListener(amountWatcher);
        etMaxAmount.addTextChangedListener(amountWatcher);

        spinnerStatus.setOnItemClickListener((parent, view, position, id) -> {
            if (filtersReady) applyFilters();
        });

        spinnerTerm.setOnItemClickListener((parent, view, position, id) -> {
            if (filtersReady) applyFilters();
        });

        filtersReady = true;
    }

    private void setupButtons() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(PlanListActivity.this, PlanFormActivity.class);
            intent.putExtra("mode", "add");
            formLauncher.launch(intent);
        });
    }

    private void loadPlans() {
        showLoading(true);
        showEmpty(false);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getPlansManager().enqueue(new Callback<List<PlanResponse>>() {
            @Override
            public void onResponse(Call<List<PlanResponse>> call, Response<List<PlanResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    showError("Failed to load plans. Code: " + response.code());
                    return;
                }

                List<PlanResponse> data = response.body();
                adapter.setData(data);
                applyFilters();
            }

            @Override
            public void onFailure(Call<List<PlanResponse>> call, Throwable t) {
                showLoading(false);
                adapter.setData(null);
                showError("Unable to load plans");
            }
        });
    }

    private void deletePlan(int planId) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deletePlanManager(planId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(PlanListActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadPlans();
                } else {
                    showError("Delete failed. This plan may be in use. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Unable to delete plan");
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

    private void applyFilters() {
        String query = searchViewPlan.getQuery() != null
                ? searchViewPlan.getQuery().toString().trim()
                : "";

        Double minAmount = null;
        Double maxAmount = null;
        String status = "All";
        Integer contractTerm = null;

        try {
            String minText = etMinAmount.getText().toString().trim();
            if (!minText.isEmpty()) {
                minAmount = Double.parseDouble(minText);
            }
        } catch (Exception ignored) { }

        try {
            String maxText = etMaxAmount.getText().toString().trim();
            if (!maxText.isEmpty()) {
                maxAmount = Double.parseDouble(maxText);
            }
        } catch (Exception ignored) { }

        String selectedStatus = spinnerStatus.getText() != null
                ? spinnerStatus.getText().toString().trim()
                : "";
        if (!selectedStatus.isEmpty()) {
            status = selectedStatus;
        }

        String selectedTerm = spinnerTerm.getText() != null
                ? spinnerTerm.getText().toString().trim()
                : "";
        if (!selectedTerm.isEmpty() && !selectedTerm.equalsIgnoreCase("All")) {
            try {
                contractTerm = Integer.parseInt(selectedTerm);
            } catch (Exception ignored) { }
        }

        adapter.applyFilters(query, minAmount, maxAmount, status, contractTerm);
        updateEmptyState();
    }

    private void loadServiceTypes() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getServiceTypes().enqueue(new Callback<List<ServiceTypeResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceTypeResponse>> call, Response<List<ServiceTypeResponse>> response) {
                if (response.isSuccessful()) {
                    serviceTypes = response.body();
                    adapter.setServiceTypes(serviceTypes); // 👈 important
                }
            }

            @Override
            public void onFailure(Call<List<ServiceTypeResponse>> call, Throwable t) {
                Toast.makeText(PlanListActivity.this, "Failed to load service types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlans();
    }
}
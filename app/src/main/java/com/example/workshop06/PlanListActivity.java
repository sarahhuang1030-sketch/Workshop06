package com.example.workshop06;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.example.workshop06.model.PlanFeatureResponse;
import com.example.workshop06.model.PlanResponse;
import com.example.workshop06.model.ServiceTypeResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanListActivity extends AppCompatActivity {

    private List<ServiceTypeResponse> serviceTypes = new ArrayList<>();

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchViewPlan;
    private FloatingActionButton fabAdd;

    private EditText etMinAmount, etMaxAmount;
    private MaterialAutoCompleteTextView spinnerStatus, spinnerTerm;

    private PlanManagerAdapter adapter;
    private ImageButton btnBack;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> loadPlans()
            );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(PlanListActivity.this, EmployeeDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        initViews();
        setupRecyclerView();
        setupSearch();
        setupFilterControls();
        setupButtons();

        loadServiceTypes();
        loadPlans();

        BottomNavHelper.setup(this, R.id.nav_plans);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewPlans);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewPlan = findViewById(R.id.searchViewPlan);
        fabAdd = findViewById(R.id.fabAdd);

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
                intent.putExtra("monthlyPrice",
                        item.getMonthlyPrice() != null ? item.getMonthlyPrice() : Double.NaN);
                intent.putExtra("contractTermMonths",
                        item.getContractTermMonths() != null ? item.getContractTermMonths() : Integer.MIN_VALUE);
                intent.putExtra("description", item.getDescription());
                intent.putExtra("isActive",
                        item.getIsActive() != null ? item.getIsActive() : Integer.MIN_VALUE);
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(PlanResponse item) {
                if (item.getPlanId() == null) return;

                new AlertDialog.Builder(PlanListActivity.this)
                        .setTitle("Delete Plan")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (d, w) -> deletePlan(item.getPlanId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlanFormActivity.class);
            intent.putExtra("mode", "add");
            formLauncher.launch(intent);
        });
    }

    private void loadPlans() {
        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        api.getPlansManager().enqueue(new Callback<List<PlanResponse>>() {
            @Override
            public void onResponse(Call<List<PlanResponse>> call, Response<List<PlanResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showLoading(false);
                    showError("Failed to load plans");
                    adapter.setData(new ArrayList<>());
                    return;
                }

                List<PlanResponse> data = response.body();
                adapter.setData(data);
                applyFilters();

                if (data == null || data.isEmpty()) {
                    showLoading(false);
                    tvEmpty.setVisibility(View.VISIBLE);
                    return;
                }

                final int[] remaining = {0};

                for (PlanResponse plan : data) {
                    if (plan.getPlanId() != null) {
                        remaining[0]++;
                    }
                }

                if (remaining[0] == 0) {
                    showLoading(false);
                    return;
                }

                for (PlanResponse plan : data) {
                    if (plan.getPlanId() == null) {
                        continue;
                    }

                    api.getPlanFeaturesByPlanId(plan.getPlanId())
                            .enqueue(new Callback<List<PlanFeatureResponse>>() {
                                @Override
                                public void onResponse(Call<List<PlanFeatureResponse>> call,
                                                       Response<List<PlanFeatureResponse>> res) {
                                    if (res.isSuccessful() && res.body() != null) {
                                        plan.setFeatures(res.body());
                                    }

                                    adapter.notifyDataSetChanged();
                                    remaining[0]--;

                                    if (remaining[0] <= 0) {
                                        showLoading(false);
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<PlanFeatureResponse>> call, Throwable t) {
                                    remaining[0]--;

                                    if (remaining[0] <= 0) {
                                        showLoading(false);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onFailure(Call<List<PlanResponse>> call, Throwable t) {
                showLoading(false);
                showError("Unable to load plans");
                adapter.setData(new ArrayList<>());
            }
        });
    }

    private void deletePlan(int planId) {
        ApiService api = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        api.deletePlanManager(planId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadPlans();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showError("Unable to delete plan");
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        tvEmpty.setText(msg != null ? msg : "No plans found");
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void applyFilters() {
        String keyword = searchViewPlan.getQuery() != null
                ? searchViewPlan.getQuery().toString().trim()
                : "";

        Double minAmount = parseDouble(etMinAmount.getText() != null
                ? etMinAmount.getText().toString().trim()
                : "");

        Double maxAmount = parseDouble(etMaxAmount.getText() != null
                ? etMaxAmount.getText().toString().trim()
                : "");

        String status = spinnerStatus.getText() != null
                ? spinnerStatus.getText().toString().trim()
                : "All";

        Integer contractTerm = parseInteger(spinnerTerm.getText() != null
                ? spinnerTerm.getText().toString().trim()
                : "");

        adapter.applyFilters(keyword, minAmount, maxAmount, status, contractTerm);

        if (adapter.getItemCount() == 0) {
            tvEmpty.setText("No plans found");
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void setupSearch() {
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
                new String[]{"All", "Active", "Inactive"}
        );
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setText("All", false);
        spinnerStatus.setOnClickListener(v -> spinnerStatus.showDropDown());
        spinnerStatus.setOnItemClickListener((parent, view, position, id) -> applyFilters());

        ArrayAdapter<String> termAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"", "12", "24", "36"}
        );
        spinnerTerm.setAdapter(termAdapter);
        spinnerTerm.setOnClickListener(v -> spinnerTerm.showDropDown());
        spinnerTerm.setOnItemClickListener((parent, view, position, id) -> applyFilters());

        etMinAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) applyFilters();
        });

        etMaxAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) applyFilters();
        });
    }

    private void loadServiceTypes() {
        ApiService api = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        api.getServiceTypes().enqueue(new Callback<List<ServiceTypeResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceTypeResponse>> call,
                                   Response<List<ServiceTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    serviceTypes = response.body();
                    adapter.setServiceTypes(serviceTypes);
                }
            }

            @Override
            public void onFailure(Call<List<ServiceTypeResponse>> call, Throwable t) {
                // no-op
            }
        });
    }

    private Double parseDouble(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return null;
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return null;
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlans();
    }
}
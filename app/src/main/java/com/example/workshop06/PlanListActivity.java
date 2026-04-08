package com.example.workshop06;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.example.workshop06.model.PlanResponse;
import com.example.workshop06.model.ServiceTypeResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

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

    private ImageButton btnBack;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadPlans());

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

        ApiService api = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        api.getPlansManager().enqueue(new Callback<List<PlanResponse>>() {
            @Override
            public void onResponse(Call<List<PlanResponse>> call, Response<List<PlanResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    showError("Failed to load plans");
                    return;
                }

                List<PlanResponse> data = response.body();
                adapter.setData(data);
                applyFilters();
            }

            @Override
            public void onFailure(Call<List<PlanResponse>> call, Throwable t) {
                showLoading(false);
                showError("Unable to load plans");
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
            public void onFailure(Call<Void> call, Throwable t) { }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void applyFilters() {
        adapter.applyFilters("", null, null, "All", null);
    }

    private void setupSearch() {}
    private void setupFilterControls() {}
    private void loadServiceTypes() {}

    @Override
    protected void onResume() {
        super.onResume();
        loadPlans();
    }
}
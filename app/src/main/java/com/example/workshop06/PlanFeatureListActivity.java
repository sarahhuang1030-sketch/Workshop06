package com.example.workshop06;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.PlanFeatureAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.PlanFeatureResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanFeatureListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchViewPlanFeature;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNavigation;

    private PlanFeatureAdapter adapter;

    private ImageButton btnBack;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadPlanFeatures());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_feature_list);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        initViews();
        setupRecyclerView();
        setupSearch();
        setupButtons();
        setupBottomNavigation();
        loadPlanFeatures();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewPlanFeatures);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewPlanFeature = findViewById(R.id.searchViewPlanFeature);
        fabAdd = findViewById(R.id.fabAdd);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        adapter = new PlanFeatureAdapter(new PlanFeatureAdapter.OnPlanFeatureActionListener() {
            @Override
            public void onEdit(PlanFeatureResponse item) {
                Intent intent = new Intent(PlanFeatureListActivity.this, PlanFeatureFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("featureId", item.getFeatureId());
                intent.putExtra("planId", item.getPlanId());
                intent.putExtra("featureName", item.getFeatureName());
                intent.putExtra("featureValue", item.getFeatureValue());
                intent.putExtra("unit", item.getUnit());
                intent.putExtra("sortOrder", item.getSortOrder());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(PlanFeatureResponse item) {
                if (item.getFeatureId() == null) return;

                new AlertDialog.Builder(PlanFeatureListActivity.this)
                        .setTitle("Delete Plan Feature")
                        .setMessage("Are you sure you want to delete this plan feature?")
                        .setPositiveButton("Delete", (dialog, which) -> deletePlanFeature(item.getFeatureId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchViewPlanFeature == null) return;

        searchViewPlanFeature.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                updateEmptyState();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                updateEmptyState();
                return true;
            }
        });
    }

    private void setupButtons() {
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(PlanFeatureListActivity.this, PlanFeatureFormActivity.class);
                intent.putExtra("mode", "add");
                formLauncher.launch(intent);
            });
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;

        bottomNavigation.setSelectedItemId(R.id.nav_plans);

        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    startActivity(new Intent(PlanFeatureListActivity.this, EmployeeDashboardActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_customers) {
                    startActivity(new Intent(PlanFeatureListActivity.this, CustomerListActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_plans) {
                    startActivity(new Intent(PlanFeatureListActivity.this, PlanListActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(PlanFeatureListActivity.this, EmployeeProfileActivity.class));
                    finish();
                    return true;
                }

                return false;
            }
        });
    }

    private void loadPlanFeatures() {
        showLoading(true);
        showEmpty(false);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getPlanFeatures().enqueue(new Callback<List<PlanFeatureResponse>>() {
            @Override
            public void onResponse(Call<List<PlanFeatureResponse>> call, Response<List<PlanFeatureResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    showError("Failed to load plan features. Code: " + response.code());
                    return;
                }

                List<PlanFeatureResponse> data = response.body();
                adapter.setData(data);
                showEmpty(data == null || data.isEmpty());
            }

            @Override
            public void onFailure(Call<List<PlanFeatureResponse>> call, Throwable t) {
                showLoading(false);
                adapter.setData(null);
                showError("Unable to load plan features");
            }
        });
    }

    private void deletePlanFeature(int featureId) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deletePlanFeature(featureId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(PlanFeatureListActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadPlanFeatures();
                } else {
                    showError("Delete failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Unable to delete plan feature");
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
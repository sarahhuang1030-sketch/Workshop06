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

import com.example.workshop06.adapter.PlanManagerAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.PlanResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchViewPlan;
    private ImageButton btnBack;
    private FloatingActionButton fabAdd;

    private PlanManagerAdapter adapter;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadPlans());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupButtons();
        loadPlans();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewPlans);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewPlan = findViewById(R.id.searchViewPlan);
        btnBack = findViewById(R.id.btnBack);
        fabAdd = findViewById(R.id.fabAdd);
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
        btnBack.setOnClickListener(v -> finish());

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
                showEmpty(data == null || data.isEmpty());
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
}
package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.EmployeeDashboardResponse;
import com.example.workshop06.api.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private TextView tvAgentName;
    private TextView tvBranchesCount;
    private TextView tvAddonsCount;
    private TextView tvSubscriptionsCount;
    private TextView tvInvoicesCount;
    private TextView tvReportsSubtitle;
    private TextView tvLogsSubtitle;
    private TextView tvPlanFeatureCount;

    private BottomNavigationView bottomNavigation;

    private MaterialCardView cardBranches;
    private MaterialCardView cardAddons;
    private MaterialCardView cardSubscriptions;
    private MaterialCardView cardInvoices;
    private MaterialCardView cardReports;
    private MaterialCardView cardActivityLogs;

    private MaterialCardView cardPlanFeatures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        tvAgentName = findViewById(R.id.tvAgentName);
        tvBranchesCount = findViewById(R.id.tvBranchesCount);
        tvAddonsCount = findViewById(R.id.tvAddonsCount);
        tvSubscriptionsCount = findViewById(R.id.tvSubscriptionsCount);
        tvInvoicesCount = findViewById(R.id.tvInvoicesCount);
        tvReportsSubtitle = findViewById(R.id.tvReportsSubtitle);
        tvLogsSubtitle = findViewById(R.id.tvLogsSubtitle);
        tvPlanFeatureCount = findViewById(R.id.tvPlanFeatureCount);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        cardBranches = findViewById(R.id.cardBranches);
        cardAddons = findViewById(R.id.cardAddons);
        cardSubscriptions = findViewById(R.id.cardSubscriptions);
        cardInvoices = findViewById(R.id.cardInvoices);
        cardReports = findViewById(R.id.cardReports);
        cardActivityLogs = findViewById(R.id.cardActivityLogs);
        cardPlanFeatures = findViewById(R.id.cardPlanFeatures);

        setupNav();
        setupQuickMenu();
        loadDashboard();
    }

    private void loadDashboard() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getEmployeeDashboard().enqueue(new Callback<EmployeeDashboardResponse>() {
            @Override
            public void onResponse(Call<EmployeeDashboardResponse> call,
                                   Response<EmployeeDashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeDashboardResponse data = response.body();

                    if (data.getFirstName() != null && !data.getFirstName().isEmpty()) {
                        tvAgentName.setText(data.getFirstName() + "'s Dashboard");
                    } else {
                        tvAgentName.setText("Agent Dashboard");
                    }

                    tvBranchesCount.setText(data.getActiveBranches() + " active");
                    tvAddonsCount.setText(data.getAvailableAddons() + " available");
                    tvSubscriptionsCount.setText(data.getActiveSubscriptions() + " plans");
                    tvInvoicesCount.setText(data.getPendingInvoices() + " pending");
                    tvReportsSubtitle.setText("Monthly");
                    tvLogsSubtitle.setText(data.getRecentLogs() + " logs");
                    tvPlanFeatureCount.setText(data.getPlanFeatures() + " plan features");
                } else {
                    tvAgentName.setText("Agent Dashboard");
                    Toast.makeText(EmployeeDashboardActivity.this,
                            "Failed to load dashboard", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EmployeeDashboardResponse> call, Throwable t) {
                tvAgentName.setText("Agent Dashboard");
                Toast.makeText(EmployeeDashboardActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupNav() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            }

            if (id == R.id.nav_customers) {
                startActivity(new Intent(this, CustomerListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_plans) {
                startActivity(new Intent(this, PlanListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, EmployeeProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void setupQuickMenu() {
        cardBranches.setOnClickListener(v ->
                startActivity(new Intent(this, LocationListActivity.class)));

        cardAddons.setOnClickListener(v ->
                startActivity(new Intent(this, AddOnListActivity.class)));

        cardSubscriptions.setOnClickListener(v ->
                startActivity(new Intent(this, SubscriptionListActivity.class)));

        cardInvoices.setOnClickListener(v ->
                startActivity(new Intent(this, InvoiceListActivity.class)));

        cardReports.setOnClickListener(v ->
                startActivity(new Intent(this, LocationListActivity.class)));

        cardActivityLogs.setOnClickListener(v ->
                startActivity(new Intent(this, LocationListActivity.class)));

        cardReports.setOnClickListener(v ->
                startActivity(new Intent(this, LocationListActivity.class)));

        cardPlanFeatures.setOnClickListener(v ->
                startActivity(new Intent(this, LocationListActivity.class)));



//        cardReports.setOnClickListener(v ->
//                startActivity(new Intent(this, ReportListActivity.class)));
//
//        cardActivityLogs.setOnClickListener(v ->
//                startActivity(new Intent(this, ActivityLogListActivity.class)));
//
//        cardReports.setOnClickListener(v ->
//                startActivity(new Intent(this, ServiceRequestListActivity.class)));
//
//        cardPlanFeatures.setOnClickListener(v ->
//                startActivity(new Intent(this, PlantFeatureListActivity.class)));
    }
}
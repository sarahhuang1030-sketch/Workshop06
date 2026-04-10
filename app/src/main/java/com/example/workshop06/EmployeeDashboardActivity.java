package com.example.workshop06;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.DashboardMenuAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.DashboardMenuItem;
import com.example.workshop06.model.ManagerSummaryResponse;
import com.example.workshop06.model.MeResponse;
import com.example.workshop06.model.ServiceDashboardSummaryResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private TextView tvAgentName;
    private RecyclerView rvDashboardCards;
    private BottomNavigationView bottomNavigation;
    private TextView tvStatus;
    private View statusDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        tvAgentName = findViewById(R.id.tvAgentName);
        rvDashboardCards = findViewById(R.id.rvDashboardCards);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        tvStatus = findViewById(R.id.tvStatus);
        statusDot = findViewById(R.id.statusDot);

        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "");
        String firstName = prefs.getString("first_name", "");
        String lastName = prefs.getString("last_name", "");

        if (firstName != null && !firstName.isEmpty()) {
            tvAgentName.setText(firstName + " " + lastName);
        } else if ("Service Technician".equalsIgnoreCase(role)) {
            tvAgentName.setText("Technician Dashboard");
        } else {
            tvAgentName.setText("Employee Dashboard");
        }

        loadEmployeeStatus();
        setupDashboardCards();

        tvAgentName.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeDashboardActivity.this, EmployeeProfileActivity.class);
            startActivity(intent);
        });

        if ("Service Technician".equalsIgnoreCase(role)) {
            BottomNavHelper.setup(this, R.id.nav_home);
        } else {
            BottomNavHelper.setup(this, R.id.nav_home);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEmployeeStatus();
    }

    private void loadEmployeeStatus() {
        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null || token.trim().isEmpty()) {
            tvStatus.setText("Unknown");
            statusDot.setBackgroundResource(R.drawable.bg_status_dot_red);
            Toast.makeText(this, "Missing JWT token", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getMe("Bearer " + token).enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(@NonNull Call<MeResponse> call,
                                   @NonNull Response<MeResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    MeResponse me = response.body();
                    String status = me.getStatus();

                    if ("Active".equalsIgnoreCase(status)) {
                        tvStatus.setText("Active");
                        statusDot.setBackgroundResource(R.drawable.bg_status_dot_green);
                    } else {
                        tvStatus.setText(status != null && !status.trim().isEmpty() ? status : "Inactive");
                        statusDot.setBackgroundResource(R.drawable.bg_status_dot_red);
                    }
                } else {
                    tvStatus.setText("Unknown");
                    statusDot.setBackgroundResource(R.drawable.bg_status_dot_red);
                    Toast.makeText(EmployeeDashboardActivity.this,
                            "Failed to load status: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MeResponse> call, @NonNull Throwable t) {
                tvStatus.setText("Unknown");
                statusDot.setBackgroundResource(R.drawable.bg_status_dot_red);
                Toast.makeText(EmployeeDashboardActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDashboardCards() {
        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "");

        rvDashboardCards.setLayoutManager(new GridLayoutManager(this, 2));

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if ("Service Technician".equalsIgnoreCase(role)) {
            apiService.getServiceSummary().enqueue(new Callback<ServiceDashboardSummaryResponse>() {
                @Override
                public void onResponse(@NonNull Call<ServiceDashboardSummaryResponse> call,
                                       @NonNull Response<ServiceDashboardSummaryResponse> response) {

                    List<DashboardMenuItem> items = new ArrayList<>();

                    if (response.isSuccessful() && response.body() != null) {
                        ServiceDashboardSummaryResponse data = response.body();

                        items.add(new DashboardMenuItem(
                                "📅",
                                "Jobs",
                                data.getTodayAppointments() + " today",
                                R.drawable.bg_card_top_accent_blue,
                                R.drawable.bg_icon_blue,
                                ServiceAppointmentListActivity.class
                        ));

                        items.add(new DashboardMenuItem(
                                "🧑‍🤝‍🧑",
                                "Requests",
                                data.getOpenRequests() + " open",
                                R.drawable.bg_card_top_accent_magenta,
                                R.drawable.bg_icon_lavender,
                                ServiceRequestListActivity.class
                        ));

                        items.add(new DashboardMenuItem(
                                "🛠️",
                                "Assigned",
                                data.getAssignedRequests() + " assigned",
                                R.drawable.bg_card_top_accent_purple,
                                R.drawable.bg_icon_lavender,
                                ServiceRequestListActivity.class
                        ));

                        items.add(new DashboardMenuItem(
                                "✅",
                                "Completed",
                                data.getCompletedRequests() + " done",
                                R.drawable.bg_card_top_accent_pink,
                                R.drawable.bg_icon_pink,
                                ServiceRequestListActivity.class
                        ));

                        DashboardMenuAdapter adapter = new DashboardMenuAdapter(EmployeeDashboardActivity.this, items);
                        rvDashboardCards.setAdapter(adapter);
                    } else {
                        Toast.makeText(EmployeeDashboardActivity.this,
                                "Failed to load technician summary",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ServiceDashboardSummaryResponse> call, @NonNull Throwable t) {
                    Toast.makeText(EmployeeDashboardActivity.this,
                            "Error: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        apiService.getManagerSummary().enqueue(new Callback<ManagerSummaryResponse>() {
            @Override
            public void onResponse(@NonNull Call<ManagerSummaryResponse> call,
                                   @NonNull Response<ManagerSummaryResponse> response) {

                List<DashboardMenuItem> items = new ArrayList<>();

                if (response.isSuccessful() && response.body() != null) {
                    ManagerSummaryResponse data = response.body();

                    items.add(new DashboardMenuItem(
                            "👥",
                            "Customers",
                            "Manage customer base",
                            R.drawable.bg_card_top_accent_blue,
                            R.drawable.bg_icon_blue,
                            CustomerListActivity.class
                    ));

                    items.add(new DashboardMenuItem(
                            "💳",
                            "Invoices",
                            "View and manage invoices",
                            R.drawable.bg_card_top_accent_red,
                            R.drawable.bg_icon_red,
                            InvoiceListActivity.class
                    ));

                    DashboardMenuItem quotesItem = new DashboardMenuItem(
                            "📄",
                            "Quotes",
                            "View customer quotes",
                            R.drawable.bg_card_top_accent_purple,
                            R.drawable.bg_icon_lavender,
                            SubscriptionListActivity.class
                    );
                    quotesItem.setExtra("statusFilter", "Pending");
                    items.add(quotesItem);

                    DashboardMenuItem revenueItem = new DashboardMenuItem(
                            "💰",
                            "Monthly Revenue",
                            String.format("$%.2f", data.getEstimatedMonthlyRevenue()),
                            R.drawable.bg_card_top_accent_pink,
                            R.drawable.bg_icon_pink,
                            InvoiceListActivity.class
                    );
                    revenueItem.setExtra("monthlyRevenueMode", "true");
                    items.add(revenueItem);

                    items.add(new DashboardMenuItem(
                            "🖥️",
                            "Subscriptions",
                            data.getActiveSubscriptions() + " active",
                            R.drawable.bg_card_top_accent_pink,
                            R.drawable.bg_icon_pink,
                            SubscriptionListActivity.class
                    ));

                    items.add(new DashboardMenuItem(
                            "➕",
                            "Add-Ons",
                            data.getTotalAddons() + " available",
                            R.drawable.bg_card_top_accent_blue,
                            R.drawable.bg_icon_blue,
                            AddOnListActivity.class
                    ));

                    items.add(new DashboardMenuItem(
                            "🧩",
                            "Plan Features",
                            data.getTotalPlanFeatures() + " active",
                            R.drawable.bg_card_top_accent_magenta,
                            R.drawable.bg_icon_lavender,
                            PlanFeatureListActivity.class
                    ));

                    items.add(new DashboardMenuItem(
                            "🛠️",
                            "Services",
                            "Manage service requests",
                            R.drawable.bg_card_top_accent_magenta,
                            R.drawable.bg_icon_lavender,
                            ServiceRequestListActivity.class
                    ));

                    DashboardMenuItem pastDueItem = new DashboardMenuItem(
                            "⚠️",
                            "Past Due",
                            data.getOpenInvoices() + " past due",
                            R.drawable.bg_card_top_accent_red,
                            R.drawable.bg_icon_red,
                            InvoiceListActivity.class
                    );
                    pastDueItem.setExtra("pastDueFilter", "true");
                    items.add(pastDueItem);

                    items.add(new DashboardMenuItem(
                            "📦",
                            "Custom Bundle",
                            "Create and send bundles",
                            R.drawable.bg_card_top_accent_purple,
                            R.drawable.bg_icon_lavender,
                            CustomBundleActivity.class
                    ));

                    items.add(new DashboardMenuItem(
                            "📊",
                            "Employee Sales",
                            "See Agents' Performance",
                            R.drawable.bg_card_top_accent_pink,
                            R.drawable.bg_icon_blue,
                            EmployeeSalesActivity.class
                    ));

                    if ("Manager".equalsIgnoreCase(role)) {
                        items.add(new DashboardMenuItem(
                                "🏢",
                                "Branches\nLocation",
                                data.getTotalLocations() + " active",
                                R.drawable.bg_card_top_accent_purple,
                                R.drawable.bg_icon_lavender,
                                LocationListActivity.class
                        ));

                        items.add(new DashboardMenuItem(
                                "👨‍💼",
                                "Employee",
                                data.getTotalEmployees() + " employees",
                                R.drawable.bg_card_top_accent_blue,
                                R.drawable.bg_icon_blue,
                                EmployeeListActivity.class
                        ));
                    }

                } else {
                    Toast.makeText(EmployeeDashboardActivity.this,
                            "Failed to load summary",
                            Toast.LENGTH_SHORT).show();
                }

                DashboardMenuAdapter adapter = new DashboardMenuAdapter(EmployeeDashboardActivity.this, items);
                rvDashboardCards.setAdapter(adapter);
            }

            @Override
            public void onFailure(@NonNull Call<ManagerSummaryResponse> call, @NonNull Throwable t) {
                Toast.makeText(EmployeeDashboardActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
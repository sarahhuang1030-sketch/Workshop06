package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.DashboardMenuAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.DashboardMenuItem;
import com.example.workshop06.model.ManagerSummaryResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.Toast;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private TextView tvAgentName;
    private RecyclerView rvDashboardCards;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        tvAgentName = findViewById(R.id.tvAgentName);
        rvDashboardCards = findViewById(R.id.rvDashboardCards);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "");
        String firstName = prefs.getString("first_name", "");
        String lastName = prefs.getString("last_name", "");

        if ("Manager".equalsIgnoreCase(role)) {
            tvAgentName.setText(firstName + " " + lastName);
        } else if (firstName != null && !firstName.isEmpty()) {
            tvAgentName.setText(firstName + " " + lastName);
        } else {
            tvAgentName.setText("Employee Dashboard");
        }

        setupDashboardCards();
        setupBottomNavigation();
    }

    private void setupDashboardCards() {

        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "");

        rvDashboardCards.setLayoutManager(new GridLayoutManager(this, 2));

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getManagerSummary().enqueue(new Callback<ManagerSummaryResponse>() {
            @Override
            public void onResponse(Call<ManagerSummaryResponse> call, Response<ManagerSummaryResponse> response) {

                List<DashboardMenuItem> items = new ArrayList<>();

                if (response.isSuccessful() && response.body() != null) {

                    ManagerSummaryResponse data = response.body();


                    if ("Manager".equalsIgnoreCase(role)) {
                        items.add(new DashboardMenuItem(
                                "👥",
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

                    items.add(new DashboardMenuItem(
                            "🖥️",
                            "Subscriptions",
                            data.getActiveSubscriptions() + " active",
                            R.drawable.bg_card_top_accent_pink,
                            R.drawable.bg_icon_pink,
                            SubscriptionListActivity.class
                    ));

                    items.add(new DashboardMenuItem(
                            "💳",
                            "Invoices",
                            data.getOpenInvoices() + " past due",
                            R.drawable.bg_card_top_accent_red,
                            R.drawable.bg_icon_red,
                            InvoiceListActivity.class
                    ));

                    items.add(new DashboardMenuItem(
                            "📄",
                            "Add-Ons",
                            data.getTotalAddons() + " available",
                            R.drawable.bg_card_top_accent_blue,
                            R.drawable.bg_icon_blue,
                            AddOnListActivity.class
                    ));

                    if ("Manager".equalsIgnoreCase(role)) {
                        items.add(new DashboardMenuItem(
                                "📊",
                                "Agents Reports",
                                "See Agents' Performance",
                                R.drawable.bg_card_top_accent_pink,
                                R.drawable.bg_icon_blue,
                                EmployeeSalesActivity.class
                        ));
                    }

                    items.add(new DashboardMenuItem(
                            "🧑‍🤝‍🧑",
                            "Service Requests",
                            "Live feed",
                            R.drawable.bg_card_top_accent_magenta,
                            R.drawable.bg_icon_lavender,
                            ServiceRequestListActivity.class
                    ));

                    if ("Manager".equalsIgnoreCase(role)) {
                        items.add(new DashboardMenuItem(
                                "🧑‍🤝‍🧑",
                                "Plan Features",
                                data.getTotalPlanFeatures() + " active",
                                R.drawable.bg_card_top_accent_magenta,
                                R.drawable.bg_icon_lavender,
                                PlanFeatureListActivity.class
                        ));
                    }

                } else {
                    Toast.makeText(EmployeeDashboardActivity.this, "Failed to load summary", Toast.LENGTH_SHORT).show();
                }

                DashboardMenuAdapter adapter = new DashboardMenuAdapter(EmployeeDashboardActivity.this, items);
                rvDashboardCards.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<ManagerSummaryResponse> call, Throwable t) {
                Toast.makeText(EmployeeDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) return true;

            else if (itemId == R.id.nav_customers) {
                startActivity(new Intent(this, CustomerListActivity.class));
                return true;
            } else if (itemId == R.id.nav_plans) {
                startActivity(new Intent(this, PlanListActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, EmployeeProfileActivity.class));
                return true;
            }

            return false;
        });
    }
}
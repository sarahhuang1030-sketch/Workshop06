package com.example.workshop06;

import android.content.SharedPreferences;
import android.os.Bundle;
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

        if (firstName != null && !firstName.isEmpty()) {
            tvAgentName.setText(firstName + " " + lastName);
        } else if ("Service Technician".equalsIgnoreCase(role)) {
            tvAgentName.setText("Technician Dashboard");
        } else {
            tvAgentName.setText("Employee Dashboard");
        }

        setupDashboardCards();

        if ("Service Technician".equalsIgnoreCase(role)) {
            BottomNavHelper.setup(this, R.id.nav_requests);
        } else {
            BottomNavHelper.setup(this, R.id.nav_home);
        }
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
                                "🧩",
                                "Plan Features",
                                data.getTotalPlanFeatures() + " active",
                                R.drawable.bg_card_top_accent_magenta,
                                R.drawable.bg_icon_lavender,
                                PlanFeatureListActivity.class
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
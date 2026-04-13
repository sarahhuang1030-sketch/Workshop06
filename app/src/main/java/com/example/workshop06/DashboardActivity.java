package com.example.workshop06;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.CurrentPlanResponse;
import com.example.workshop06.model.MeResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DashboardActivity extends BaseActivity {

    private Button btnTopUp, btnPayBill;
    private BottomNavigationView bottomNavigation;
    private TextView tvUserName, tvPlanName, tvBalance;
    private SessionManager sessionManager;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnTopUp       = findViewById(R.id.btnTopUp);
        btnPayBill     = findViewById(R.id.btnPayBill);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        tvUserName     = findViewById(R.id.tvUserName);
        tvPlanName     = findViewById(R.id.tvPlanName);
        tvBalance      = findViewById(R.id.tvBalance);

        sessionManager = new SessionManager(this);
        token          = sessionManager.getToken();

        checkLogin();
        loadCurrentUser();
        loadCurrentPlan();

        btnTopUp.setOnClickListener(v ->
                Toast.makeText(this, "Top Up clicked", Toast.LENGTH_SHORT).show());
        btnPayBill.setOnClickListener(v ->
                Toast.makeText(this, "Pay Bill clicked", Toast.LENGTH_SHORT).show());

        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            else if (id == R.id.nav_usage) {
                startActivity(new Intent(this, UsageActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_bills) {
                startActivity(new Intent(this, BillingActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_support) {
                startActivity(new Intent(this, SupportActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            }
            return false;
        });
    }

    // Refresh user info and plan every 30 seconds
    @Override
    protected void onRefresh() {
        loadCurrentUser();
        loadCurrentPlan();
    }

    private void checkLogin() {
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void loadCurrentUser() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance(this);
        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getMe("Bearer " + token).enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MeResponse user = response.body();
                    SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("firstName", user.getDisplayName())
                            .putString("email", user.getEmail())
                            .apply();
                    tvUserName.setText(user.getDisplayName());
                } else if (response.code() == 401) {
                    Toast.makeText(DashboardActivity.this,
                            "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
                    sessionManager.clearToken();
                    startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(DashboardActivity.this,
                            "Failed to load user info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                Toast.makeText(DashboardActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadCurrentPlan() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance(this);
        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getCurrentPlan("Bearer " + token).enqueue(new Callback<CurrentPlanResponse>() {
            @Override
            public void onResponse(Call<CurrentPlanResponse> call, Response<CurrentPlanResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CurrentPlanResponse plan = response.body();
                    tvPlanName.setText(plan.getPlanName() != null && !plan.getPlanName().isEmpty()
                            ? plan.getPlanName() : "No Active Plan");
                    if (plan.getTotalMonthlyPrice() != null)
                        tvBalance.setText(String.format("$%.2f", plan.getTotalMonthlyPrice()));
                    else if (plan.getMonthlyPrice() != null)
                        tvBalance.setText(String.format("$%.2f", plan.getMonthlyPrice()));
                    else tvBalance.setText("$0.00");
                } else {
                    tvPlanName.setText("No Active Plan");
                    tvBalance.setText("$0.00");
                }
            }

            @Override
            public void onFailure(Call<CurrentPlanResponse> call, Throwable t) {
                tvPlanName.setText("Unable to load plan");
                tvBalance.setText("$0.00");
            }
        });
    }
}
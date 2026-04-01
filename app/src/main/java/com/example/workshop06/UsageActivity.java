package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.workshop06.adapter.PlanPagerAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.CurrentPlanItemResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UsageActivity extends AppCompatActivity {

    private ViewPager2 planPager;
    private BottomNavigationView bottomNavigation;
    private ImageView btnBack;

    private SessionManager sessionManager;
    private String token;

    private final List<CurrentPlanItemResponse> plans = new ArrayList<>();
    private PlanPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);

        planPager = findViewById(R.id.planPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnBack = findViewById(R.id.btnBack);

        sessionManager = new SessionManager(this);
        token = sessionManager.getToken();

        checkLogin();

        adapter = new PlanPagerAdapter(plans);
        planPager.setAdapter(adapter);

        loadPlans();

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });

        bottomNavigation.setSelectedItemId(R.id.nav_usage);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_usage) {
                return true;
            } else if (id == R.id.nav_bills) {
                startActivity(new Intent(this, BillingActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_support) {
                startActivity(new Intent(this, SupportActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void checkLogin() {
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void loadPlans() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance(this);
        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getMyPlans("Bearer " + token).enqueue(new Callback<List<CurrentPlanItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<CurrentPlanItemResponse>> call,
                                   @NonNull Response<List<CurrentPlanItemResponse>> response) {

                Toast.makeText(UsageActivity.this,
                        "Code: " + response.code(),
                        Toast.LENGTH_SHORT).show();

                if (response.isSuccessful() && response.body() != null) {
                    plans.clear();
                    plans.addAll(response.body());

                    Toast.makeText(UsageActivity.this,
                            "Plans count: " + plans.size(),
                            Toast.LENGTH_LONG).show();

                    adapter.notifyDataSetChanged();
                } else if (response.code() == 401) {
                    Toast.makeText(UsageActivity.this,
                            "Session expired. Please login again.",
                            Toast.LENGTH_SHORT).show();

                    sessionManager.clearToken();
                    startActivity(new Intent(UsageActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(UsageActivity.this,
                            "Failed to load plans. Code = " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CurrentPlanItemResponse>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(UsageActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
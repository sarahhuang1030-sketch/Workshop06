package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.MeResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private BottomNavigationView bottomNavigation;
    private Button btnEditProfile, btnLogout;

    private LinearLayout itemPersonalInfo, itemSecurity, itemPayment, itemNotifications, itemDarkMode, itemLanguage;
    private SwitchMaterial switchNotifications, switchDarkMode;

    private TextView tvProfileName, tvProfileEmail;

    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBack = findViewById(R.id.btnBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        itemPersonalInfo = findViewById(R.id.itemPersonalInfo);
        itemSecurity = findViewById(R.id.itemSecurity);
        itemPayment = findViewById(R.id.itemPayment);
        itemNotifications = findViewById(R.id.itemNotifications);
        itemDarkMode = findViewById(R.id.itemDarkMode);
        itemLanguage = findViewById(R.id.itemLanguage);

        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        // ✅ initialize these
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class)));

        itemPersonalInfo.setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, AddressActivity.class)));

        itemSecurity.setOnClickListener(v ->
                Toast.makeText(this, "Security & Password clicked", Toast.LENGTH_SHORT).show());

        itemPayment.setOnClickListener(v ->
                Toast.makeText(this, "Payment Methods clicked", Toast.LENGTH_SHORT).show());

        itemLanguage.setOnClickListener(v ->
                Toast.makeText(this, "Language clicked", Toast.LENGTH_SHORT).show());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this,
                        isChecked ? "Notifications enabled" : "Notifications disabled",
                        Toast.LENGTH_SHORT).show());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this,
                        isChecked ? "Dark mode enabled" : "Dark mode disabled",
                        Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

            // ✅ clear JWT
            sessionManager.clearToken();

            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        bottomNavigation.setSelectedItemId(R.id.nav_settings);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_usage) {
                startActivity(new Intent(this, UsageActivity.class));
                overridePendingTransition(0, 0);
                finish();
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
                return true; // ✅ FIX: don’t reopen itself
            }

            return false;
        });

        // ✅ IMPORTANT: call it here
        loadProfile();
    }

    private void loadProfile() {
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No token found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getMe("Bearer " + token).enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MeResponse me = response.body();

                    tvProfileName.setText(me.getDisplayName());
                    tvProfileEmail.setText(me.getEmail());
                } else {
                    Toast.makeText(SettingsActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
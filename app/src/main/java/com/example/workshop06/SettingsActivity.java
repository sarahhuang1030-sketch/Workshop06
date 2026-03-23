package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private BottomNavigationView bottomNavigation;
    private Button btnEditProfile, btnLogout;

    private LinearLayout itemPersonalInfo, itemSecurity, itemPayment, itemNotifications, itemDarkMode, itemLanguage;
    private SwitchMaterial switchNotifications, switchDarkMode;

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

        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show());

        itemPersonalInfo.setOnClickListener(v ->
                Toast.makeText(this, "Personal Information clicked", Toast.LENGTH_SHORT).show());

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
                startActivity(new Intent(this,SettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }
}
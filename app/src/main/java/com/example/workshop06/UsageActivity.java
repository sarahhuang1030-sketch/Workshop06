package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class UsageActivity extends AppCompatActivity {

    private ImageView btnBack;
    private BottomNavigationView bottomNavigation;
    private Button btnManagePlan;
    private MaterialCardView cardExtraData, cardGlobalRoaming;
    private LinearLayout cardUpgrade1, cardUpgrade2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);

        btnBack = findViewById(R.id.btnBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnManagePlan = findViewById(R.id.btnManagePlan);
        cardExtraData = findViewById(R.id.cardExtraData);
        cardGlobalRoaming = findViewById(R.id.cardGlobalRoaming);
        cardUpgrade1 = findViewById(R.id.cardUpgrade1);
        cardUpgrade2 = findViewById(R.id.cardUpgrade2);

        btnBack.setOnClickListener(v -> finish());

        btnManagePlan.setOnClickListener(v ->
                Toast.makeText(this, "Manage Plan clicked", Toast.LENGTH_SHORT).show());

        cardExtraData.setOnClickListener(v ->
                Toast.makeText(this, "Extra 5GB Data selected", Toast.LENGTH_SHORT).show());

        cardGlobalRoaming.setOnClickListener(v ->
                Toast.makeText(this, "Global Roaming selected", Toast.LENGTH_SHORT).show());

        cardUpgrade1.setOnClickListener(v ->
                Toast.makeText(this, "Unlimited Max selected", Toast.LENGTH_SHORT).show());

        cardUpgrade2.setOnClickListener(v ->
                Toast.makeText(this, "Family Share Gold selected", Toast.LENGTH_SHORT).show());

        bottomNavigation.setSelectedItemId(R.id.nav_usage);

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
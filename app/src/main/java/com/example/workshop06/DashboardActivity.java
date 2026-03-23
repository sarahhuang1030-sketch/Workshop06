package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    private Button btnTopUp, btnPayBill;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnTopUp = findViewById(R.id.btnTopUp);
        btnPayBill = findViewById(R.id.btnPayBill);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnTopUp.setOnClickListener(v ->
                Toast.makeText(this, "Top Up clicked", Toast.LENGTH_SHORT).show());

        btnPayBill.setOnClickListener(v ->
                Toast.makeText(this, "Pay Bill clicked", Toast.LENGTH_SHORT).show());

        bottomNavigation.setSelectedItemId(R.id.nav_home);

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
package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class BillingActivity extends BaseActivity {

    @Override protected void onRefresh() {}

    private ImageView btnBack;
    private BottomNavigationView bottomNavigation;
    private Button btnPayNow;
    private TextView tvAddNew;
    private MaterialCardView cardVisa, cardApplePay;
    private android.widget.LinearLayout historySept, historyAug, historyJuly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        btnBack = findViewById(R.id.btnBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnPayNow = findViewById(R.id.btnPayNow);
        tvAddNew = findViewById(R.id.tvAddNew);
        cardVisa = findViewById(R.id.cardVisa);
        cardApplePay = findViewById(R.id.cardApplePay);
        historySept = findViewById(R.id.historySept);
        historyAug = findViewById(R.id.historyAug);
        historyJuly = findViewById(R.id.historyJuly);

        btnBack.setOnClickListener(v -> finish());

        btnPayNow.setOnClickListener(v ->
                Toast.makeText(this, "Pay Now clicked", Toast.LENGTH_SHORT).show());

        tvAddNew.setOnClickListener(v ->
                Toast.makeText(this, "Add New payment method", Toast.LENGTH_SHORT).show());

        cardVisa.setOnClickListener(v ->
                Toast.makeText(this, "Visa selected", Toast.LENGTH_SHORT).show());

        cardApplePay.setOnClickListener(v ->
                Toast.makeText(this, "Apple Pay selected", Toast.LENGTH_SHORT).show());

        historySept.setOnClickListener(v ->
                Toast.makeText(this, "September Bill opened", Toast.LENGTH_SHORT).show());

        historyAug.setOnClickListener(v ->
                Toast.makeText(this, "August Bill opened", Toast.LENGTH_SHORT).show());

        historyJuly.setOnClickListener(v ->
                Toast.makeText(this, "July Bill opened", Toast.LENGTH_SHORT).show());

        bottomNavigation.setSelectedItemId(R.id.nav_bills);

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
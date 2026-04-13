package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class SupportActivity extends BaseActivity {

    @Override protected void onRefresh() {}

    private ImageView btnBack;
    private BottomNavigationView bottomNavigation;

    private MaterialCardView cardLiveChat, cardCallSupport, cardEmail, cardFindStore;
    private LinearLayout faq1, faq2, faq3, faq4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        btnBack = findViewById(R.id.btnBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        cardLiveChat = findViewById(R.id.cardLiveChat);
        cardCallSupport = findViewById(R.id.cardCallSupport);
        cardEmail = findViewById(R.id.cardEmail);
        cardFindStore = findViewById(R.id.cardFindStore);

        faq1 = findViewById(R.id.faq1);
        faq2 = findViewById(R.id.faq2);
        faq3 = findViewById(R.id.faq3);
        faq4 = findViewById(R.id.faq4);

        btnBack.setOnClickListener(v -> finish());

        cardLiveChat.setOnClickListener(v ->
                Toast.makeText(this, "Opening Live Chat...", Toast.LENGTH_SHORT).show());

        cardCallSupport.setOnClickListener(v ->
                Toast.makeText(this, "Calling Support...", Toast.LENGTH_SHORT).show());

        cardEmail.setOnClickListener(v ->
                Toast.makeText(this, "Opening Email Support...", Toast.LENGTH_SHORT).show());

        cardFindStore.setOnClickListener(v ->
                Toast.makeText(this, "Finding nearest store...", Toast.LENGTH_SHORT).show());

        faq1.setOnClickListener(v ->
                Toast.makeText(this, "FAQ: Upgrade current data plan", Toast.LENGTH_SHORT).show());

        faq2.setOnClickListener(v ->
                Toast.makeText(this, "FAQ: Billing and payment issues", Toast.LENGTH_SHORT).show());

        faq3.setOnClickListener(v ->
                Toast.makeText(this, "FAQ: International roaming", Toast.LENGTH_SHORT).show());

        faq4.setOnClickListener(v ->
                Toast.makeText(this, "FAQ: Device insurance & repairs", Toast.LENGTH_SHORT).show());

        bottomNavigation.setSelectedItemId(R.id.nav_support);

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
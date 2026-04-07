package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NavMapActivity extends AppCompatActivity {

    private View infoOverlay;
    private FloatingActionButton fabInfo;
    private FloatingActionButton fabMyLocation;
    private MaterialButton btnCloseOverlay;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_map);

        infoOverlay = findViewById(R.id.infoOverlay);
        fabInfo = findViewById(R.id.fabInfo);
        fabMyLocation = findViewById(R.id.fabMyLocation);
//        btnCloseOverlay = findViewById(R.id.btnCloseOverlay);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        setupOverlay();
        setupBottomNavigation();
        setupActions();
    }

    private void setupOverlay() {
        if (infoOverlay != null) {
            infoOverlay.setVisibility(View.GONE);
            infoOverlay.setOnClickListener(v -> infoOverlay.setVisibility(View.GONE));
        }

        if (fabInfo != null) {
            fabInfo.setOnClickListener(v -> {
                if (infoOverlay != null) {
                    infoOverlay.setVisibility(View.VISIBLE);
                }
            });
        }

        if (btnCloseOverlay != null) {
            btnCloseOverlay.setOnClickListener(v -> {
                if (infoOverlay != null) {
                    infoOverlay.setVisibility(View.GONE);
                }
            });
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;

        bottomNavigation.setSelectedItemId(R.id.nav_maps);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_jobs) {
                startActivity(new Intent(this, ServiceAppointmentListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_requests) {
                startActivity(new Intent(this, ServiceRequestListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_maps) {
                return true;
            } else if (id == R.id.nav_customers) {
                startActivity(new Intent(this, CustomerListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, EmployeeProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void setupActions() {
        if (fabMyLocation != null) {
            fabMyLocation.setOnClickListener(v -> {
                // TODO: move camera to current location
                // add your Google Map current-location logic here
            });
        }
    }
}
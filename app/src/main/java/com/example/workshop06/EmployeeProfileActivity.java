package com.example.workshop06;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EmployeeProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private TextView tvFirstName;
    private TextView tvUsername;
    private TextView tvRole;
    private TextView tvEmail;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_profile);
        BottomNavHelper.setup(this, R.id.nav_profile);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvUsername = findViewById(R.id.tvUsername);
        tvRole = findViewById(R.id.tvRole);
        tvEmail = findViewById(R.id.tvEmail);
        btnLogout = findViewById(R.id.btnLogout);

        loadProfile();
        setupNav();
        setupLogout();
    }

    private void loadProfile() {
        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);

        String firstName = prefs.getString("firstName", "Employee");
        String username = prefs.getString("username", "N/A");
        String role = prefs.getString("role", "EMPLOYEE");
        String email = prefs.getString("email", "Not available");

        tvFirstName.setText(firstName);
        tvUsername.setText(username);
        tvRole.setText(role);
        tvEmail.setText(email);
    }

    private void setupNav() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, EmployeeDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_customers) {
                startActivity(new Intent(this, CustomerListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_plans) {
                startActivity(new Intent(this, PlanListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                return true;
            }

            return false;
        });
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            SessionManager sessionManager = new SessionManager(EmployeeProfileActivity.this);
            sessionManager.clearToken();

            Intent intent = new Intent(EmployeeProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
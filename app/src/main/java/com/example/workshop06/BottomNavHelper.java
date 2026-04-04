package com.example.workshop06;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavHelper {

    public static void setup(AppCompatActivity activity, int selectedItemId) {
        BottomNavigationView bottomNavigation = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigation == null) return;

        if (selectedItemId != 0) {
            bottomNavigation.setSelectedItemId(selectedItemId);
        } else {
            bottomNavigation.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
                bottomNavigation.getMenu().getItem(i).setChecked(false);
            }
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == selectedItemId) return true;

            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(activity, EmployeeDashboardActivity.class);
            } else if (id == R.id.nav_customers) {
                intent = new Intent(activity, CustomerListActivity.class);
            } else if (id == R.id.nav_plans) {
                intent = new Intent(activity, PlanListActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(activity, EmployeeProfileActivity.class);
            }

            if (intent != null) {
                activity.startActivity(intent);
                activity.finish();
                return true;
            }

            return false;
        });
    }
}
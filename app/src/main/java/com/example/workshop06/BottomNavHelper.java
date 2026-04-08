package com.example.workshop06;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavHelper {

    public static void setup(AppCompatActivity activity, @IdRes int selectedItemId) {
        BottomNavigationView bottomNavigation = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigation == null) return;

        SharedPreferences prefs = activity.getSharedPreferences("teleconnect_prefs", Activity.MODE_PRIVATE);
        String role = prefs.getString("user_role", "");

        if ("Service Technician".equalsIgnoreCase(role)) {
            setupTechnician(activity, selectedItemId);
            return;
        }

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
                activity.overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    public static void setupTechnician(AppCompatActivity activity, @IdRes int selectedItemId) {
        BottomNavigationView bottomNavigation = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigation == null) return;

        Menu menu = bottomNavigation.getMenu();
        menu.clear();
        bottomNavigation.inflateMenu(R.menu.bottom_nav_technician);

        bottomNavigation.setSelectedItemId(selectedItemId);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == selectedItemId) return true;

            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(activity, EmployeeDashboardActivity.class);

//            } else if (id == R.id.nav_requests) {
//                intent = new Intent(activity, ServiceRequestListActivity.class);
            } else if (id == R.id.nav_maps) {
                intent = new Intent(activity, NavMapActivity.class); // temp reuse
            } else if (id == R.id.nav_customers) {
                intent = new Intent(activity, CustomerListActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(activity, EmployeeProfileActivity.class);
            }

            if (intent != null) {
                activity.startActivity(intent);
                activity.finish();
                activity.overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}
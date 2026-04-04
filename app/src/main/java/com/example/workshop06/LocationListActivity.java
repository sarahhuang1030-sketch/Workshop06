package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.LocationAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.LocationResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;
    private SearchView searchViewLocation;
    private MaterialAutoCompleteTextView spinnerLocationTypeFilter;

    private LocationAdapter adapter;
    private final List<LocationResponse> allLocations = new ArrayList<>();
    private boolean filtersReady = false;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadLocations();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        recyclerView = findViewById(R.id.recyclerLocations);
        progressBar = findViewById(R.id.progressBar);
        fabAdd = findViewById(R.id.fabAddLocation);
        searchViewLocation = findViewById(R.id.searchViewLocation);
        spinnerLocationTypeFilter = findViewById(R.id.spinnerLocationTypeFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LocationAdapter(new ArrayList<>(), new LocationAdapter.LocationActionListener() {
            @Override
            public void onEdit(LocationResponse item) {
                Intent intent = new Intent(LocationListActivity.this, LocationFormActivity.class);
                intent.putExtra("locationId", item.getLocationId());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(LocationResponse item) {
                deleteLocation(item.getLocationId());
            }
        });
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(LocationListActivity.this, LocationFormActivity.class);
            formLauncher.launch(intent);
        });

        setupSearch();
        setupTypeFilter();
        BottomNavHelper.setup(this, 0);

        loadLocations();
    }

    private void setupSearch() {
        if (searchViewLocation == null) return;

        searchViewLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });
    }

    private void setupTypeFilter() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All"}
        );
        spinnerLocationTypeFilter.setAdapter(typeAdapter);
        spinnerLocationTypeFilter.setText("All", false);

        spinnerLocationTypeFilter.setOnItemClickListener((parent, view, position, id) -> {
            if (filtersReady) applyFilters();
        });

        filtersReady = true;
    }

    private void loadLocations() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getLocations().enqueue(new Callback<List<LocationResponse>>() {
            @Override
            public void onResponse(Call<List<LocationResponse>> call, Response<List<LocationResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allLocations.clear();
                    allLocations.addAll(response.body());

                    updateTypeDropdown(allLocations);
                    applyFilters();
                } else {
                    Toast.makeText(LocationListActivity.this, "Failed to load locations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LocationResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LocationListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateTypeDropdown(List<LocationResponse> locations) {
        Set<String> types = new LinkedHashSet<>();
        types.add("All");

        for (LocationResponse item : locations) {
            if (item.getLocationType() != null && !item.getLocationType().trim().isEmpty()) {
                types.add(item.getLocationType().trim());
            }
        }

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(types)
        );
        spinnerLocationTypeFilter.setAdapter(typeAdapter);

        String current = spinnerLocationTypeFilter.getText() != null
                ? spinnerLocationTypeFilter.getText().toString().trim()
                : "";

        if (current.isEmpty()) {
            spinnerLocationTypeFilter.setText("All", false);
        }
    }

    private void applyFilters() {
        String query = "";
        if (searchViewLocation.getQuery() != null) {
            query = searchViewLocation.getQuery().toString().trim().toLowerCase();
        }

        String selectedType = spinnerLocationTypeFilter.getText() != null
                ? spinnerLocationTypeFilter.getText().toString().trim()
                : "All";

        List<LocationResponse> filtered = new ArrayList<>();

        for (LocationResponse item : allLocations) {
            String name = safe(item.getLocationName());
            String phone = safe(item.getPhone());
            String type = safe(item.getLocationType());

            boolean matchesSearch =
                    query.isEmpty()
                            || name.toLowerCase().contains(query)
                            || phone.toLowerCase().contains(query);

            boolean matchesType =
                    selectedType.isEmpty()
                            || selectedType.equalsIgnoreCase("All")
                            || type.equalsIgnoreCase(selectedType);

            if (matchesSearch && matchesType) {
                filtered.add(item);
            }
        }

        adapter.setData(filtered);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void deleteLocation(Integer locationId) {
        if (locationId == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteLocation(locationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LocationListActivity.this, "Location deleted", Toast.LENGTH_SHORT).show();
                    loadLocations();
                } else {
                    Toast.makeText(LocationListActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(LocationListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
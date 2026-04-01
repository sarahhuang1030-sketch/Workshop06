package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.LocationAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.LocationResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;

    private LocationAdapter adapter;
    private final List<LocationResponse> locations = new ArrayList<>();

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                loadLocations();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        recyclerView = findViewById(R.id.recyclerLocations);
        progressBar = findViewById(R.id.progressBar);
        fabAdd = findViewById(R.id.fabAddLocation);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationAdapter(locations, new LocationAdapter.LocationActionListener() {
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

        loadLocations();
    }

    private void loadLocations() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getLocations().enqueue(new Callback<List<LocationResponse>>() {
            @Override
            public void onResponse(Call<List<LocationResponse>> call, Response<List<LocationResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    locations.clear();
                    locations.addAll(response.body());
                    adapter.notifyDataSetChanged();
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

    private void deleteLocation(Integer locationId) {
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
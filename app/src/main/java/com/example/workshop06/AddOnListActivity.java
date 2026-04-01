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

import com.example.workshop06.adapter.AddOnAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.AddOnResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddOnListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddAddOn;

    private AddOnAdapter adapter;
    private final List<AddOnResponse> addOns = new ArrayList<>();

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                loadAddOns();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_list);

        recyclerView = findViewById(R.id.recyclerAddOns);
        progressBar = findViewById(R.id.progressBar);
        fabAddAddOn = findViewById(R.id.fabAddAddOn);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AddOnAdapter(addOns, new AddOnAdapter.AddOnActionListener() {
            @Override
            public void onEdit(AddOnResponse item) {
                Intent intent = new Intent(AddOnListActivity.this, AddOnFormActivity.class);
                intent.putExtra("addOnId", item.getAddOnId());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(AddOnResponse item) {
                deleteAddOn(item.getAddOnId());
            }
        });

        recyclerView.setAdapter(adapter);

        fabAddAddOn.setOnClickListener(v -> {
            Intent intent = new Intent(AddOnListActivity.this, AddOnFormActivity.class);
            formLauncher.launch(intent);
        });

        loadAddOns();
    }

    private void loadAddOns() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getAddOns().enqueue(new Callback<List<AddOnResponse>>() {
            @Override
            public void onResponse(Call<List<AddOnResponse>> call, Response<List<AddOnResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    addOns.clear();
                    addOns.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AddOnListActivity.this, "Failed to load add-ons", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AddOnResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddOnListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteAddOn(Integer addOnId) {
        if (addOnId == null) {
            Toast.makeText(this, "Invalid add-on id", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteAddOn(addOnId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddOnListActivity.this, "Add-on deleted", Toast.LENGTH_SHORT).show();
                    loadAddOns();
                } else {
                    Toast.makeText(AddOnListActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddOnListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
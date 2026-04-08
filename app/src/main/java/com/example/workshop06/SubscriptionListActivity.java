package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.SubscriptionAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.SubscriptionResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddSubscription;
    private SearchView searchViewSubscription;
    private MaterialAutoCompleteTextView spinnerStatusFilter;
    private BottomNavigationView bottomNavigation;
    private ImageButton btnBack;

    private SubscriptionAdapter adapter;
    private final List<SubscriptionResponse> subscriptions = new ArrayList<>();

    private String currentQuery = "";
    private String currentStatus = "All";

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> loadSubscriptions()
            );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_list);

        recyclerView = findViewById(R.id.recyclerSubscriptions);
        progressBar = findViewById(R.id.progressBar);
        fabAddSubscription = findViewById(R.id.fabAddSubscription);
        searchViewSubscription = findViewById(R.id.searchViewSubscription);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SubscriptionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        setupSearch();
        setupStatusFilter();

        BottomNavHelper.setup(this, 0);

        btnBack.setOnClickListener(v -> finish());

        bottomNavigation.post(() -> {
            bottomNavigation.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
                bottomNavigation.getMenu().getItem(i).setChecked(false);
            }
        });

        fabAddSubscription.setOnClickListener(v -> {
            Intent intent = new Intent(SubscriptionListActivity.this, SubscriptionFormActivity.class);
            formLauncher.launch(intent);
        });

        loadSubscriptions();
    }

    private void setupSearch() {
        searchViewSubscription.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query == null ? "" : query;
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText == null ? "" : newText;
                applyFilters();
                return true;
            }
        });
    }

    private void setupStatusFilter() {
        String[] statusOptions = {"All", "Active", "Inactive", "Suspended", "Cancelled", "Pending"};

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statusOptions
        );

        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setText("All", false);

        spinnerStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            currentStatus = parent.getItemAtPosition(position).toString();
            applyFilters();
        });
    }

    private void applyFilters() {
        adapter.filter(currentQuery, currentStatus);
    }

    private void loadSubscriptions() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getSubscriptions().enqueue(new Callback<List<SubscriptionResponse>>() {
            @Override
            public void onResponse(Call<List<SubscriptionResponse>> call, Response<List<SubscriptionResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<SubscriptionResponse> data = response.body();

                    subscriptions.clear();
                    subscriptions.addAll(data);

                    for (SubscriptionResponse s : data) {
                        Log.d("API_DEBUG",
                                "ID=" + s.getSubscriptionId()
                                        + " | customerId=" + s.getCustomerId()
                                        + " | customerName=" + s.getCustomerName()
                                        + " | planName=" + s.getPlanName()
                                        + " | status=" + s.getStatus());
                    }

                    adapter.updateData(data);
                    applyFilters();

                    Log.d("SubscriptionListActivity", "Loaded subscriptions: " + data.size());
                } else {
                    Toast.makeText(
                            SubscriptionListActivity.this,
                            "Failed to load subscriptions",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<SubscriptionResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(
                        SubscriptionListActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
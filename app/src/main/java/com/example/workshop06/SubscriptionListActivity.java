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

import com.example.workshop06.adapter.SubscriptionAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.SubscriptionResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddSubscription;

    private SubscriptionAdapter adapter;
    private final List<SubscriptionResponse> subscriptions = new ArrayList<>();

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadSubscriptions());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_list);

        recyclerView = findViewById(R.id.recyclerSubscriptions);
        progressBar = findViewById(R.id.progressBar);
        fabAddSubscription = findViewById(R.id.fabAddSubscription);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SubscriptionAdapter(subscriptions, new SubscriptionAdapter.SubscriptionActionListener() {
            @Override
            public void onEdit(SubscriptionResponse item) {
                Intent intent = new Intent(SubscriptionListActivity.this, SubscriptionFormActivity.class);
                intent.putExtra("subscriptionId", item.getSubscriptionId());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(SubscriptionResponse item) {
                deleteSubscription(item.getSubscriptionId());
            }
        });

        recyclerView.setAdapter(adapter);

        fabAddSubscription.setOnClickListener(v -> {
            Intent intent = new Intent(SubscriptionListActivity.this, SubscriptionFormActivity.class);
            formLauncher.launch(intent);
        });

        loadSubscriptions();
    }

    private void loadSubscriptions() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getSubscriptions().enqueue(new Callback<List<SubscriptionResponse>>() {
            @Override
            public void onResponse(Call<List<SubscriptionResponse>> call, Response<List<SubscriptionResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    subscriptions.clear();
                    subscriptions.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SubscriptionListActivity.this, "Failed to load subscriptions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SubscriptionResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SubscriptionListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteSubscription(Integer subscriptionId) {
        if (subscriptionId == null) {
            Toast.makeText(this, "Invalid subscription id", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteSubscription(subscriptionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SubscriptionListActivity.this, "Subscription deleted", Toast.LENGTH_SHORT).show();
                    loadSubscriptions();
                } else {
                    Toast.makeText(SubscriptionListActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SubscriptionListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
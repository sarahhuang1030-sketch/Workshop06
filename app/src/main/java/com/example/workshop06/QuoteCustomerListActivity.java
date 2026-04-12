package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.QuoteCustomerAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.QuoteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuoteCustomerListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private ImageButton btnBack;
    private QuoteCustomerAdapter adapter;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadQuotes();
            refreshHandler.postDelayed(this, 30000);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_customer_list);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerCustomers);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchViewCustomer);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuoteCustomerAdapter((customerId, customerName) -> {
            Intent intent = new Intent(QuoteCustomerListActivity.this, QuotesListActivity.class);
            intent.putExtra("customerId", customerId);
            intent.putExtra("customerName", customerName);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        setupSearch();
        loadQuotes();
        BottomNavHelper.setup(this, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHandler.postDelayed(refreshRunnable, 30000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    private void loadQuotes() {
        if (progressBar.getVisibility() != View.VISIBLE && adapter.getItemCount() == 0) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getQuotes().enqueue(new Callback<List<QuoteResponse>>() {
            @Override
            public void onResponse(Call<List<QuoteResponse>> call, Response<List<QuoteResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                } else {
                    Toast.makeText(QuoteCustomerListActivity.this, "Failed to load quote customers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<QuoteResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QuoteCustomerListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

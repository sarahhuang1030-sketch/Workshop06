package com.example.workshop06;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.QuoteAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.QuoteResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuotesListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private ImageButton btnBack;
    private QuoteAdapter adapter;
    private MaterialAutoCompleteTextView spinnerStatusFilter;
    private String currentQuery = "";
    private String currentStatus = "All";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerSubscriptions);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchViewSubscription);

        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new QuoteAdapter();
        recyclerView.setAdapter(adapter);

        setupSearch();
        setupStatusFilter();
        loadQuotes();
    }

    private void setupStatusFilter() {

        String[] statusOptions = {"All", "PENDING", "APPROVED", "CANCELLED"};
        MaterialAutoCompleteTextView dropdown =
                findViewById(R.id.spinnerStatusFilter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statusOptions
        );

        dropdown.setAdapter(statusAdapter);

        dropdown.setText("All", false);

        dropdown.setOnItemClickListener((parent, view, position, id) -> {

            currentStatus = statusOptions[position];

            adapter.filter(currentQuery, currentStatus);
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query == null ? "" : query;
                adapter.filter(currentQuery, currentStatus);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText == null ? "" : newText;
                adapter.filter(currentQuery, currentStatus);
                return true;
            }
        });
    }

    private void loadQuotes() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this)
                .create(ApiService.class);

        apiService.getQuotes().enqueue(new Callback<List<QuoteResponse>>() {
            @Override
            public void onResponse(Call<List<QuoteResponse>> call,
                                   Response<List<QuoteResponse>> response) {

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {

                    List<QuoteResponse> data = response.body();

                    adapter.updateData(data);

                    Log.d("Quotes", "Loaded: " + data.size());

                } else {
                    Toast.makeText(QuotesListActivity.this,
                            "Failed to load quotes",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<QuoteResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QuotesListActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
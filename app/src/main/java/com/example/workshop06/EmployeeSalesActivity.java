package com.example.workshop06;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.EmployeeSalesAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.EmployeeSalesResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeSalesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchViewEmployee;
    private ImageButton btnBack;

    private EmployeeSalesAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_employee_sales);
        // If you rename the XML file later, change this to:
        // setContentView(R.layout.activity_employee_sales);

        initViews();
        setupRecyclerView();
        setupSearch();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        loadEmployeeSales();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewSales);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewEmployee = findViewById(R.id.searchViewEmployee);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        adapter = new EmployeeSalesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchViewEmployee == null) return;

        searchViewEmployee.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                updateEmptyState();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                updateEmptyState();
                return true;
            }
        });
    }

    private void loadEmployeeSales() {
        showLoading(true);
        showEmpty(false);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        Call<List<EmployeeSalesResponse>> call = apiService.getEmployeeSales();

        call.enqueue(new Callback<List<EmployeeSalesResponse>>() {
            @Override
            public void onResponse(Call<List<EmployeeSalesResponse>> call,
                                   Response<List<EmployeeSalesResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    showError("Failed to load employee sales. Code: " + response.code());
                    return;
                }

                List<EmployeeSalesResponse> body = response.body();

                if (body != null && !body.isEmpty()) {
                    adapter.setData(body);
                    showEmpty(false);
                } else {
                    adapter.setData(null);
                    showEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<List<EmployeeSalesResponse>> call, Throwable t) {
                showLoading(false);
                adapter.setData(null);

                String message = "Unable to load employee sales";
                if (t != null && t.getMessage() != null && !t.getMessage().trim().isEmpty()) {
                    message += ": " + t.getMessage();
                }

                showError(message);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        if (recyclerView != null) {
            recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }

        if (tvEmpty != null && isLoading) {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmpty(boolean isEmpty) {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        if (recyclerView != null && progressBar != null && progressBar.getVisibility() != View.VISIBLE) {
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void updateEmptyState() {
        if (adapter == null) return;
        showEmpty(adapter.getItemCount() == 0);
    }

    private void showError(String message) {
        updateEmptyState();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
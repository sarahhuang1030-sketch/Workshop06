package com.example.workshop06;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerEmployeeSalesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchView;
    private ImageButton btnSortSales;
    private ImageButton btnSortCount;
    private ImageButton btnBack;

    private EmployeeSalesAdapter adapter;
    private final List<EmployeeSalesResponse> salesList = new ArrayList<>();

    private boolean sortSalesDescending = true;
    private boolean sortCountDescending = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_employee_sales);

        recyclerView = findViewById(R.id.recyclerViewSales);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchView = findViewById(R.id.searchViewEmployee);
        btnSortSales = findViewById(R.id.btnSortSales);
        btnSortCount = findViewById(R.id.btnSortCount);
        btnBack = findViewById(R.id.btnBack);

        adapter = new EmployeeSalesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnSortSales.setOnClickListener(v -> {
            sortSalesDescending = !sortSalesDescending;
            sortByTotalSales();
        });

        btnSortCount.setOnClickListener(v -> {
            sortCountDescending = !sortCountDescending;
            sortBySalesCount();
        });

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

        loadEmployeeSales();
    }

    private void loadEmployeeSales() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getEmployeeSales().enqueue(new Callback<List<EmployeeSalesResponse>>() {
            @Override
            public void onResponse(Call<List<EmployeeSalesResponse>> call, Response<List<EmployeeSalesResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    salesList.clear();
                    salesList.addAll(response.body());

                    if (salesList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        adapter.setData(new ArrayList<>());
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        sortByTotalSales();
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(ManagerEmployeeSalesActivity.this, "Failed to load employee sales", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EmployeeSalesResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(ManagerEmployeeSalesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sortByTotalSales() {
        salesList.sort((a, b) -> {
            double aValue = a.getTotalSales() != null ? a.getTotalSales() : 0.0;
            double bValue = b.getTotalSales() != null ? b.getTotalSales() : 0.0;
            return sortSalesDescending
                    ? Double.compare(bValue, aValue)
                    : Double.compare(aValue, bValue);
        });
        adapter.setData(new ArrayList<>(salesList));
        updateSortButtons();
    }

    private void sortBySalesCount() {
        salesList.sort((a, b) -> {
            int aValue = a.getSalesCount() != null ? a.getSalesCount() : 0;
            int bValue = b.getSalesCount() != null ? b.getSalesCount() : 0;
            return sortCountDescending
                    ? Integer.compare(bValue, aValue)
                    : Integer.compare(aValue, bValue);
        });
        adapter.setData(new ArrayList<>(salesList));
        updateSortButtons();
    }

    private void updateSortButtons() {
        btnSortSales.setContentDescription(
                sortSalesDescending ? "Sort sales descending" : "Sort sales ascending"
        );
        btnSortCount.setContentDescription(
                sortCountDescending ? "Sort count descending" : "Sort count ascending"
        );
    }
}
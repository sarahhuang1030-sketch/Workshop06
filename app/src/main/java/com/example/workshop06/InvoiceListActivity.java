package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.InvoiceAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.InvoiceResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddInvoice;

    private TextView tvEmpty;
    private SearchView searchViewInvoice;
    private MaterialAutoCompleteTextView spinnerStatusFilter;
    private MaterialAutoCompleteTextView spinnerAmountFilter;

    private InvoiceAdapter adapter;
    private final List<InvoiceResponse> allInvoices = new ArrayList<>();
    private boolean filtersReady = false;

    private ImageButton btnBack;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadInvoices();
                } else {
                    loadInvoices();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_list);

        recyclerView = findViewById(R.id.recyclerInvoices);
        progressBar = findViewById(R.id.progressBar);
        fabAddInvoice = findViewById(R.id.fabAddInvoice);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewInvoice = findViewById(R.id.searchViewInvoice);
        spinnerStatusFilter = findViewById(R.id.spinnerInvoiceStatusFilter);
        spinnerAmountFilter = findViewById(R.id.spinnerInvoiceAmountFilter);
        btnBack = findViewById(R.id.btnBack);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());
        adapter = new InvoiceAdapter(new ArrayList<>(), new InvoiceAdapter.InvoiceActionListener() {
            @Override
            public void onView(InvoiceResponse item) {
                Intent intent = new Intent(InvoiceListActivity.this, InvoiceDetailActivity.class);
                intent.putExtra("invoiceNumber", item.getInvoiceNumber());
                startActivity(intent);
            }

            @Override
            public void onEdit(InvoiceResponse item) {
                Intent intent = new Intent(InvoiceListActivity.this, InvoiceFormActivity.class);
                intent.putExtra("invoiceNumber", item.getInvoiceNumber());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(InvoiceResponse item) {
                deleteInvoice(item.getInvoiceNumber());
            }
        });
        recyclerView.setAdapter(adapter);

        fabAddInvoice.setOnClickListener(v -> {
            Intent intent = new Intent(InvoiceListActivity.this, InvoiceFormActivity.class);
            formLauncher.launch(intent);
        });

        setupSearch();
        setupStatusFilter();
        setupAmountFilter();

        // reuse the same bottom nav helper
        BottomNavHelper.setup(this, 0);

        loadInvoices();
    }

    private void setupSearch() {
        if (searchViewInvoice == null) return;

        searchViewInvoice.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    private void setupStatusFilter() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Active", "Inactive"}
        );
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setText("All", false);

        spinnerStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            if (filtersReady) applyFilters();
        });
    }

    private void setupAmountFilter() {
        ArrayAdapter<String> amountAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{
                        "All",
                        "Under $100",
                        "$100 - $499.99",
                        "$500 - $999.99",
                        "$1000 and above"
                }
        );
        spinnerAmountFilter.setAdapter(amountAdapter);
        spinnerAmountFilter.setText("All", false);

        spinnerAmountFilter.setOnItemClickListener((parent, view, position, id) -> {
            if (filtersReady) applyFilters();
        });

        filtersReady = true;
    }

    private void loadInvoices() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getAllInvoicesAdmin().enqueue(new Callback<List<InvoiceResponse>>() {
            @Override
            public void onResponse(Call<List<InvoiceResponse>> call, Response<List<InvoiceResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allInvoices.clear();
                    allInvoices.addAll(response.body());
                    applyFilters();
                } else {
                    Toast.makeText(InvoiceListActivity.this, "Failed to load invoices", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<InvoiceResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(InvoiceListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateStatusDropdown(List<InvoiceResponse> invoices) {
        Set<String> statuses = new LinkedHashSet<>();
        statuses.add("All");

        for (InvoiceResponse item : invoices) {
            if (item.getStatus() != null && !item.getStatus().trim().isEmpty()) {
                statuses.add(item.getStatus().trim());
            }
        }

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(statuses)
        );
        spinnerStatusFilter.setAdapter(statusAdapter);

        String current = spinnerStatusFilter.getText() != null
                ? spinnerStatusFilter.getText().toString().trim()
                : "";

        if (current.isEmpty()) {
            spinnerStatusFilter.setText("All", false);
        }
    }

    private void applyFilters() {
        String query = "";
        if (searchViewInvoice != null && searchViewInvoice.getQuery() != null) {
            query = searchViewInvoice.getQuery().toString().trim().toLowerCase();
        }

        String selectedStatus = spinnerStatusFilter.getText() != null
                ? spinnerStatusFilter.getText().toString().trim()
                : "All";

        String selectedAmount = spinnerAmountFilter.getText() != null
                ? spinnerAmountFilter.getText().toString().trim()
                : "All";

        List<InvoiceResponse> filtered = new ArrayList<>();

        for (InvoiceResponse item : allInvoices) {
            String invoiceNumber = safe(item.getInvoiceNumber());
            String customerName = safe(item.getCustomerName());
            String status = safe(item.getStatus());
            double total = item.getTotal() != null ? item.getTotal() : 0.0;

            boolean matchesSearch =
                    query.isEmpty()
                            || invoiceNumber.toLowerCase().contains(query)
                            || customerName.toLowerCase().contains(query);

            boolean matchesStatus =
                    selectedStatus.isEmpty()
                            || selectedStatus.equalsIgnoreCase("All")
                            || status.equalsIgnoreCase(selectedStatus);

            boolean matchesAmount = matchesAmountRange(total, selectedAmount);

            if (matchesSearch && matchesStatus && matchesAmount) {
                filtered.add(item);
            }
        }

        adapter.setData(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean matchesAmountRange(double total, String selectedAmount) {
        if (selectedAmount == null || selectedAmount.trim().isEmpty() || selectedAmount.equalsIgnoreCase("All")) {
            return true;
        }

        switch (selectedAmount) {
            case "Under $100":
                return total < 100.0;
            case "$100 - $499.99":
                return total >= 100.0 && total < 500.0;
            case "$500 - $999.99":
                return total >= 500.0 && total < 1000.0;
            case "$1000 and above":
                return total >= 1000.0;
            default:
                return true;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void deleteInvoice(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            Toast.makeText(this, "Invalid invoice number", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteInvoice(invoiceNumber).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InvoiceListActivity.this, "Invoice deleted", Toast.LENGTH_SHORT).show();
                    loadInvoices();
                } else {
                    Toast.makeText(InvoiceListActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(InvoiceListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
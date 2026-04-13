package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.InvoiceAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.InvoiceResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PastDueActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchView;
    private ImageButton btnBack;

    private InvoiceAdapter adapter;
    private final List<InvoiceResponse> allPastDue = new ArrayList<>();
    private String currentQuery = "";

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> loadInvoices());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_due);

        recyclerView = findViewById(R.id.recyclerInvoices);
        progressBar  = findViewById(R.id.progressBar);
        tvEmpty      = findViewById(R.id.tvEmpty);
        searchView   = findViewById(R.id.searchViewInvoice);
        btnBack      = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new InvoiceAdapter(new ArrayList<>(),
                new InvoiceAdapter.InvoiceActionListener() {
                    @Override
                    public void onView(InvoiceResponse item) {
                        Intent intent = new Intent(PastDueActivity.this,
                                InvoiceDetailActivity.class);
                        intent.putExtra("invoiceNumber", item.getInvoiceNumber());
                        startActivity(intent);
                    }
                });

        recyclerView.setAdapter(adapter);
        setupSearch();
        BottomNavHelper.setup(this, 0);
        loadInvoices();
    }

    @Override
    protected void onRefresh() {
        loadInvoices();
    }

    private void setupSearch() {
        if (searchView == null) return;
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                currentQuery = query == null ? "" : query;
                applyFilter();
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                currentQuery = newText == null ? "" : newText;
                applyFilter();
                return true;
            }
        });
    }

    private void loadInvoices() {
        if (progressBar.getVisibility() != View.VISIBLE && allPastDue.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiService api = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        api.getAllInvoicesAdmin().enqueue(new Callback<List<InvoiceResponse>>() {
            @Override
            public void onResponse(Call<List<InvoiceResponse>> call,
                                   Response<List<InvoiceResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allPastDue.clear();

                    // Current month boundaries
                    Calendar now = Calendar.getInstance();
                    int thisYear  = now.get(Calendar.YEAR);
                    int thisMonth = now.get(Calendar.MONTH); // 0-based

                    for (InvoiceResponse item : response.body()) {
                        // 1. Must be unpaid
                        String status = item.getStatus() != null
                                ? item.getStatus().trim() : "";
                        boolean isUnpaid = !status.equalsIgnoreCase("PAID")
                                && !status.equalsIgnoreCase("SUCCESS")
                                && !status.equalsIgnoreCase("APPROVED");
                        if (!isUnpaid) continue;

                        // 2. Due date must be before this month
                        boolean isPastDue = isDueBeforeThisMonth(
                                item.getDueDate(), thisYear, thisMonth);
                        if (!isPastDue) continue;

                        allPastDue.add(item);
                    }

                    applyFilter();
                } else {
                    Toast.makeText(PastDueActivity.this,
                            "Failed to load invoices", Toast.LENGTH_SHORT).show();
                    showEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<List<InvoiceResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PastDueActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                showEmpty(true);
            }
        });
    }

    /**
     * Returns true if dueDate (yyyy-MM-dd) is strictly before the 1st of this month.
     */
    private boolean isDueBeforeThisMonth(String dueDate, int thisYear, int thisMonth) {
        if (dueDate == null || dueDate.trim().isEmpty()) return false;
        try {
            // dueDate format: "yyyy-MM-dd"
            String[] parts = dueDate.split("-");
            if (parts.length < 2) return false;
            int dueYear  = Integer.parseInt(parts[0]);
            int dueMonth = Integer.parseInt(parts[1]) - 1; // convert to 0-based

            if (dueYear < thisYear) return true;
            if (dueYear == thisYear && dueMonth < thisMonth) return true;
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void applyFilter() {
        if (currentQuery.isEmpty()) {
            adapter.setData(new ArrayList<>(allPastDue));
        } else {
            String q = currentQuery.toLowerCase(Locale.US);
            List<InvoiceResponse> filtered = new ArrayList<>();
            for (InvoiceResponse item : allPastDue) {
                String num  = item.getInvoiceNumber() != null
                        ? item.getInvoiceNumber().toLowerCase(Locale.US) : "";
                String name = item.getCustomerName() != null
                        ? item.getCustomerName().toLowerCase(Locale.US) : "";
                if (num.contains(q) || name.contains(q)) filtered.add(item);
            }
            adapter.setData(filtered);
        }
        showEmpty(adapter.getItemCount() == 0);
    }

    private void showEmpty(boolean isEmpty) {
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        tvEmpty.setText(isEmpty && allPastDue.isEmpty()
                ? "No past due invoices 🎉"
                : "No results found");
    }

    private void deleteInvoice(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            Toast.makeText(this, "Invalid invoice number", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService api = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        api.deleteInvoice(invoiceNumber).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PastDueActivity.this,
                            "Invoice deleted", Toast.LENGTH_SHORT).show();
                    loadInvoices();
                } else {
                    Toast.makeText(PastDueActivity.this,
                            "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PastDueActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
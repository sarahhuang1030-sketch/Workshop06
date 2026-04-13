package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
//import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.InvoiceAdapter;
import com.example.workshop06.adapter.InvoiceCustomerAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.InvoiceResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceListActivity extends BaseActivity {

    // ── Views ──────────────────────────────────────────────────────────────
    private TextView tvHeaderTitle, tvHeaderSubtitle, tvEmpty;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddInvoice;

//    private SearchView searchViewInvoice;
    private android.widget.EditText searchViewInvoice;

    private MaterialAutoCompleteTextView spinnerStatusFilter, spinnerAmountFilter;
    private ImageButton btnBack;

    // Filter row wrapper — hide/show this to control both dropdowns at once
    private View layoutFilterRow;

    // ── State ──────────────────────────────────────────────────────────────
    private final List<InvoiceResponse> allInvoices = new ArrayList<>();
    private boolean filtersReady = false;

    // "customers" = customer list view, "invoices" = filtered invoice view
    private String mode = "customers";
    private String selectedCustomerName = null;

    // ── Adapters ───────────────────────────────────────────────────────────
    private InvoiceCustomerAdapter customerAdapter;
    private InvoiceAdapter invoiceAdapter;

    // ── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    protected void onRefresh() {
        loadInvoices();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_list);

        // Bind views
        tvHeaderTitle       = findViewById(R.id.tvHeaderTitle);
        tvHeaderSubtitle    = findViewById(R.id.tvHeaderSubtitle);
        recyclerView        = findViewById(R.id.recyclerInvoices);
        progressBar         = findViewById(R.id.progressBar);
        fabAddInvoice       = findViewById(R.id.fabAddInvoice);
        tvEmpty             = findViewById(R.id.tvEmpty);
        searchViewInvoice   = findViewById(R.id.searchViewInvoice);
        spinnerStatusFilter = findViewById(R.id.spinnerInvoiceStatusFilter);
        spinnerAmountFilter = findViewById(R.id.spinnerInvoiceAmountFilter);
        btnBack             = findViewById(R.id.btnBack);
        layoutFilterRow     = findViewById(R.id.layoutFilterRow); // filter row wrapper

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Back: go to customer list if in invoice mode, otherwise exit activity
        btnBack.setOnClickListener(v -> {
            if (mode.equals("invoices")) {
                showCustomerMode();
            } else {
                finish();
            }
        });

        // Customer list adapter — clicking a row enters invoice mode for that customer
        customerAdapter = new InvoiceCustomerAdapter((customerId, customerName) -> {
            selectedCustomerName = customerName;
            showInvoiceMode(customerName);
        });

        // Invoice list adapter
        invoiceAdapter = new InvoiceAdapter(new ArrayList<>(), item -> {
            Intent intent = new Intent(InvoiceListActivity.this, InvoiceDetailActivity.class);
            intent.putExtra("invoiceNumber", item.getInvoiceNumber());
            startActivity(intent);
        });

        setupSearch();
        setupStatusFilter();
        setupAmountFilter();

        BottomNavHelper.setup(this, 0);

        loadInvoices();
    }

    // ── Mode switching ─────────────────────────────────────────────────────

    /** Show the customer list — hide filters, reset search */
    private void showCustomerMode() {
        mode = "customers";
        selectedCustomerName = null;

        tvHeaderTitle.setText("Invoices");
        tvHeaderSubtitle.setText("Select a customer");

        // Hide the entire filter row (TextInputLayout borders disappear with it)
        layoutFilterRow.setVisibility(View.GONE);

        searchViewInvoice.setText("");

        recyclerView.setAdapter(customerAdapter);
        customerAdapter.filter("");
        tvEmpty.setVisibility(customerAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    /** Show invoices filtered by the selected customer — show filters */
    private void showInvoiceMode(String customerName) {
        mode = "invoices";

        tvHeaderTitle.setText(customerName);
        tvHeaderSubtitle.setText("Invoices");

        // Show the filter row with proper outlined borders
        layoutFilterRow.setVisibility(View.VISIBLE);

        recyclerView.setAdapter(invoiceAdapter);
        applyFilters();
    }

    // ── Search ─────────────────────────────────────────────────────────────

    private void setupSearch() {
        if (searchViewInvoice == null) return;

        searchViewInvoice.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleSearch(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    /** Route search input to the correct adapter based on current mode */
    private void handleSearch(String query) {
        if (mode.equals("customers")) {
            customerAdapter.filter(query);
            tvEmpty.setVisibility(
                    customerAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        } else {
            applyFilters();
        }
    }

    // ── Filters ────────────────────────────────────────────────────────────

    private void setupStatusFilter() {
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Paid", "Unpaid"});
        spinnerStatusFilter.setAdapter(a);
        spinnerStatusFilter.setText("All", false);
        spinnerStatusFilter.setOnItemClickListener((p, v, pos, id) -> {
            if (filtersReady) applyFilters();
        });
    }

    private void setupAmountFilter() {
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Under $100", "$100 - $499.99",
                        "$500 - $999.99", "$1000 and above"});
        spinnerAmountFilter.setAdapter(a);
        spinnerAmountFilter.setText("All", false);
        spinnerAmountFilter.setOnItemClickListener((p, v, pos, id) -> {
            if (filtersReady) applyFilters();
        });
        filtersReady = true;
    }

    // ── Data loading ───────────────────────────────────────────────────────

    private void loadInvoices() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService api = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        api.getAllInvoicesAdmin().enqueue(new Callback<List<InvoiceResponse>>() {
            @Override
            public void onResponse(Call<List<InvoiceResponse>> call,
                                   Response<List<InvoiceResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allInvoices.clear();
                    allInvoices.addAll(response.body());
                    customerAdapter.updateData(allInvoices);

                    if (mode.equals("invoices")) {
                        applyFilters();
                    } else {
                        showCustomerMode();
                    }
                } else {
                    Toast.makeText(InvoiceListActivity.this,
                            "Failed to load invoices", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<InvoiceResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(InvoiceListActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Filter logic ───────────────────────────────────────────────────────

    /** Apply search query + status + amount filters to the invoice list */
    private void applyFilters() {
        String query = searchViewInvoice.getText() != null
                ? searchViewInvoice.getText().toString().trim().toLowerCase() : "";

        String selectedStatus = spinnerStatusFilter.getText() != null
                ? spinnerStatusFilter.getText().toString().trim() : "All";

        String selectedAmount = spinnerAmountFilter.getText() != null
                ? spinnerAmountFilter.getText().toString().trim() : "All";

        List<InvoiceResponse> filtered = new ArrayList<>();

        for (InvoiceResponse item : allInvoices) {

            // Only show invoices belonging to the selected customer
            if (selectedCustomerName != null
                    && !selectedCustomerName.equals(item.getCustomerName())) continue;

            String invoiceNum = safe(item.getInvoiceNumber());
            String status     = safe(item.getStatus());
            double total      = item.getTotal() != null ? item.getTotal() : 0.0;


            // Search matches invoice number or customer name
            boolean matchSearch = query.isEmpty()
                    || invoiceNum.toLowerCase().contains(query)
                    || safe(item.getCustomerName()).toLowerCase().contains(query)
                    || status.toLowerCase().contains(query);

            // Status filter
            boolean matchStatus;
            if (selectedStatus.equalsIgnoreCase("All") || selectedStatus.isEmpty()) {
                matchStatus = true;
            } else if (selectedStatus.equalsIgnoreCase("Unpaid")) {
                matchStatus = !status.equalsIgnoreCase("Paid")
                        && !status.equalsIgnoreCase("Success");
            } else {
                matchStatus = status.equalsIgnoreCase(selectedStatus);
            }

            boolean matchAmount = matchesAmountRange(total, selectedAmount);

            if (matchSearch && matchStatus && matchAmount) {
                filtered.add(item);
            }
        }

        invoiceAdapter.setData(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean matchesAmountRange(double total, String range) {
        if (range == null || range.equalsIgnoreCase("All")) return true;
        switch (range) {
            case "Under $100":      return total < 100;
            case "$100 - $499.99":  return total >= 100 && total < 500;
            case "$500 - $999.99":  return total >= 500 && total < 1000;
            case "$1000 and above": return total >= 1000;
            default:                return true;
        }
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

}
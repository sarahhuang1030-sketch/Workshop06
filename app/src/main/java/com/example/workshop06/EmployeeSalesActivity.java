package com.example.workshop06;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.InvoiceResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeSalesActivity extends BaseActivity {

    // ── Views ──────────────────────────────────────────────────────────────
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvMonthlyTotal;
    private TextView tvMonthLabel;
    private ImageButton btnBack;
    private SearchView searchView;

    // ── Data ───────────────────────────────────────────────────────────────
    private InvoiceSalesAdapter adapter;
    private final List<InvoiceResponse> allInvoices = new ArrayList<>();

    // ── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_employee_sales);

        // Bind views
        recyclerView    = findViewById(R.id.recyclerViewSales);
        progressBar     = findViewById(R.id.progressBar);
        tvEmpty         = findViewById(R.id.tvEmpty);
        tvMonthlyTotal  = findViewById(R.id.tvMonthlyTotal);
        tvMonthLabel    = findViewById(R.id.tvMonthLabel);
        btnBack         = findViewById(R.id.btnBack);
        searchView      = findViewById(R.id.searchViewEmployee);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Show current month label e.g. "April 2026"
        String monthLabel = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
        if (tvMonthLabel != null) tvMonthLabel.setText(monthLabel);

        // Set up RecyclerView
        adapter = new InvoiceSalesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up search
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterInvoices(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterInvoices(newText);
                    return true;
                }
            });
        }

        BottomNavHelper.setup(this, 0);
        loadInvoices();
    }

    @Override
    protected void onRefresh() {
        loadInvoices();
    }

    // ── Data Loading ───────────────────────────────────────────────────────

    private void loadInvoices() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        api.getMySalesInvoices().enqueue(new Callback<List<InvoiceResponse>>() {
            @Override
            public void onResponse(Call<List<InvoiceResponse>> call,
                                   Response<List<InvoiceResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allInvoices.clear();
                    allInvoices.addAll(response.body());
                    filterInvoices(""); // show all by default
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(EmployeeSalesActivity.this,
                            "Failed to load invoices", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<InvoiceResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(EmployeeSalesActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Filter + Total ─────────────────────────────────────────────────────

    /** Filter invoices by search query and update the monthly total */
    private void filterInvoices(String query) {
        List<InvoiceResponse> filtered = new ArrayList<>();
        String q = query == null ? "" : query.trim().toLowerCase();

        for (InvoiceResponse inv : allInvoices) {
            boolean matchSearch = q.isEmpty()
                    || (inv.getInvoiceNumber() != null
                    && inv.getInvoiceNumber().toLowerCase().contains(q))
                    || (inv.getCustomerName() != null
                    && inv.getCustomerName().toLowerCase().contains(q))
                    || (inv.getStatus() != null
                    && inv.getStatus().toLowerCase().contains(q));

            if (matchSearch) filtered.add(inv);
        }

        adapter.setData(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

        // Calculate and display total of filtered invoices
        double total = 0;
        for (InvoiceResponse inv : filtered) {
            total += inv.getTotal() != null ? inv.getTotal() : 0.0;
        }
        if (tvMonthlyTotal != null) {
            tvMonthlyTotal.setText(String.format(Locale.US, "$%.2f", total));
        }
    }

    // ── Inline Adapter ─────────────────────────────────────────────────────

    static class InvoiceSalesAdapter
            extends RecyclerView.Adapter<InvoiceSalesAdapter.VH> {

        private final List<InvoiceResponse> items = new ArrayList<>();

        void setData(List<InvoiceResponse> data) {
            items.clear();
            if (data != null) items.addAll(data);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_employee_sale_invoice, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            InvoiceResponse inv = items.get(position);

            h.tvInvoiceNumber.setText(
                    inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "—");

            h.tvCustomerName.setText(
                    inv.getCustomerName() != null
                            ? "Customer: " + inv.getCustomerName() : "—");

            String status = inv.getStatus() != null ? inv.getStatus() : "—";
            h.tvStatus.setText(status);

            // Apply status badge color
            if (inv.getStatus() != null) {
                switch (inv.getStatus().toUpperCase()) {
                    case "PAID":
                        h.tvStatus.setBackgroundResource(R.drawable.bg_badge_approved);
                        break;
                    case "PENDING":
                    case "UNPAID":
                        h.tvStatus.setBackgroundResource(R.drawable.bg_badge_pending);
                        break;
                    default:
                        h.tvStatus.setBackgroundResource(R.drawable.bg_badge_cancelled);
                }
            }

            h.tvSubtotal.setText(String.format(Locale.US, "$%.2f",
                    inv.getSubtotal() != null ? inv.getSubtotal() : 0.0));

            h.tvTax.setText(String.format(Locale.US, "$%.2f",
                    inv.getTaxTotal() != null ? inv.getTaxTotal() : 0.0));

            h.tvTotal.setText(String.format(Locale.US, "$%.2f",
                    inv.getTotal() != null ? inv.getTotal() : 0.0));

            h.tvIssueDate.setText(
                    inv.getIssueDate() != null ? "Issued: " + inv.getIssueDate() : "");

            h.tvDueDate.setText(
                    inv.getDueDate() != null ? "Due: " + inv.getDueDate() : "");
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvInvoiceNumber, tvCustomerName, tvStatus;
            TextView tvSubtotal, tvTax, tvTotal;
            TextView tvIssueDate, tvDueDate;

            VH(View v) {
                super(v);
                tvInvoiceNumber = v.findViewById(R.id.tvInvoiceNumber);
                tvCustomerName  = v.findViewById(R.id.tvCustomerName);
                tvStatus        = v.findViewById(R.id.tvStatus);
                tvSubtotal      = v.findViewById(R.id.tvSubtotal);
                tvTax           = v.findViewById(R.id.tvTax);
                tvTotal         = v.findViewById(R.id.tvTotal);
                tvIssueDate     = v.findViewById(R.id.tvIssueDate);
                tvDueDate       = v.findViewById(R.id.tvDueDate);
            }
        }
    }
}
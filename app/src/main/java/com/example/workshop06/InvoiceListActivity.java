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

import com.example.workshop06.adapter.InvoiceAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.InvoiceResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddInvoice;
    private InvoiceAdapter adapter;
    private final List<InvoiceResponse> invoices = new ArrayList<>();

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadInvoices());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_list);

        recyclerView = findViewById(R.id.recyclerInvoices);
        progressBar = findViewById(R.id.progressBar);
        fabAddInvoice = findViewById(R.id.fabAddInvoice);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvoiceAdapter(invoices, new InvoiceAdapter.InvoiceActionListener() {
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

        loadInvoices();
    }

    private void loadInvoices() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getAllInvoicesAdmin().enqueue(new Callback<List<InvoiceResponse>>() {
            @Override
            public void onResponse(Call<List<InvoiceResponse>> call, Response<List<InvoiceResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    invoices.clear();
                    invoices.addAll(response.body());
                    adapter.notifyDataSetChanged();
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

    private void deleteInvoice(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isEmpty()) {
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
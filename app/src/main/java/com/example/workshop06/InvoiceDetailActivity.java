package com.example.workshop06;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.InvoiceResponse;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceDetailActivity extends AppCompatActivity {

    private TextView tvInvoiceNumber, tvCustomerName, tvStatus, tvIssueDate, tvDueDate, tvTotal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        tvInvoiceNumber = findViewById(R.id.tvInvoiceNumber);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvStatus = findViewById(R.id.tvStatus);
        tvIssueDate = findViewById(R.id.tvIssueDate);
        tvDueDate = findViewById(R.id.tvDueDate);
        tvTotal = findViewById(R.id.tvTotal);

        String invoiceNumber = getIntent().getStringExtra("invoiceNumber");
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            Toast.makeText(this, "Invoice number not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadInvoice(invoiceNumber);
    }

    private void loadInvoice(String invoiceNumber) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getInvoiceByNumber(invoiceNumber).enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InvoiceResponse item = response.body();

                    tvInvoiceNumber.setText("Invoice Number: " + safe(item.getInvoiceNumber()));
                    tvCustomerName.setText("Customer Name: " + safe(item.getCustomerName()));
                    tvStatus.setText("Status: " + safe(item.getStatus()));
                    tvIssueDate.setText("Issue Date: " + safe(item.getIssueDate()));
                    tvDueDate.setText("Due Date: " + safe(item.getDueDate()));

                    double total = item.getTotal() != null ? item.getTotal() : 0.0;
                    tvTotal.setText(String.format(Locale.US, "Total: $%.2f", total));
                } else {
                    Toast.makeText(InvoiceDetailActivity.this, "Failed to load invoice details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                Toast.makeText(InvoiceDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String safe(String value) {
        return value == null ? "—" : value.trim();
    }
}
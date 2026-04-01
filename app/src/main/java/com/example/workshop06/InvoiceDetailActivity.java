package com.example.workshop06;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.InvoiceResponse;

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
        if (invoiceNumber != null) {
            loadInvoice(invoiceNumber);
        }
    }

    private void loadInvoice(String invoiceNumber) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getInvoiceByNumber(invoiceNumber).enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InvoiceResponse item = response.body();

                    tvInvoiceNumber.setText(item.getInvoiceNumber());
                    tvCustomerName.setText(item.getCustomerName() != null ? item.getCustomerName() : "—");
                    tvStatus.setText(item.getStatus() != null ? item.getStatus() : "—");
                    tvIssueDate.setText(item.getIssueDate() != null ? item.getIssueDate() : "—");
                    tvDueDate.setText(item.getDueDate() != null ? item.getDueDate() : "—");
                    tvTotal.setText(item.getTotal() != null ? "$" + String.format("%.2f", item.getTotal()) : "$0.00");
                } else {
                    Toast.makeText(InvoiceDetailActivity.this, "Failed to load invoice", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                Toast.makeText(InvoiceDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
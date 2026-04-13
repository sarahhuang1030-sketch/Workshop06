package com.example.workshop06;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.InvoiceResponse;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceDetailActivity extends BaseActivity {

    private TextView tvInvoiceNumber, tvCustomerName, tvStatus,
            tvIssueDate, tvDueDate, tvTotal, tvHeaderInvoiceNumber;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_detail);

        tvInvoiceNumber       = findViewById(R.id.tvInvoiceNumber);
        tvCustomerName        = findViewById(R.id.tvCustomerName);
        tvStatus              = findViewById(R.id.tvStatus);
        tvIssueDate           = findViewById(R.id.tvIssueDate);
        tvDueDate             = findViewById(R.id.tvDueDate);
        tvTotal               = findViewById(R.id.tvTotal);
        tvHeaderInvoiceNumber = findViewById(R.id.tvHeaderInvoiceNumber);
        progressBar           = findViewById(R.id.progressBar);
        btnBack               = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        String invoiceNumber = getIntent().getStringExtra("invoiceNumber");
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            Toast.makeText(this, "Invoice number not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadInvoice(invoiceNumber);
    }

    @Override
    protected void onRefresh() {}

    private void loadInvoice(String invoiceNumber) {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getInvoiceByNumber(invoiceNumber).enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call,
                                   Response<InvoiceResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    InvoiceResponse item = response.body();
                    bindData(item);
                } else {
                    Toast.makeText(InvoiceDetailActivity.this,
                            "Failed to load invoice details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(InvoiceDetailActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindData(InvoiceResponse item) {
        String num    = safe(item.getInvoiceNumber());
        String status = safe(item.getStatus());

        tvHeaderInvoiceNumber.setText(num.isEmpty() ? "Invoice Details" : num);
        tvInvoiceNumber.setText(num.isEmpty() ? "—" : num);
        tvCustomerName.setText(safe(item.getCustomerName()));
        tvIssueDate.setText(safe(item.getIssueDate()));
        tvDueDate.setText(safe(item.getDueDate()));

        double total = item.getTotal() != null ? item.getTotal() : 0.0;
        tvTotal.setText(String.format(Locale.US, "$%.2f", total));

        // Status text
        tvStatus.setText(status.isEmpty() ? "—" : status);

        // Status badge color
        int bgRes;
        switch (status.toLowerCase(Locale.US)) {
            case "paid":
            case "success":
                bgRes = R.drawable.bg_badge_approved; break;
            case "unpaid":
            case "pending":
                bgRes = R.drawable.bg_badge_pending;  break;
            case "cancelled":
                bgRes = R.drawable.bg_badge_cancelled; break;
            default:
                bgRes = R.drawable.bg_badge_pending;  break;
        }
        tvStatus.setBackgroundResource(bgRes);
    }

    private String safe(String value) {
        return value == null ? "—" : value.trim();
    }
}
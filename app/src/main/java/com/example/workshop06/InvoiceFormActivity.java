package com.example.workshop06;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.InvoiceRequest;
import com.example.workshop06.model.InvoiceResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceFormActivity extends AppCompatActivity {

    private EditText etCustomerId, etInvoiceNumber, etStatus, etIssueDate, etDueDate,
            etSubtotal, etTaxTotal, etTotal ;
    private Button btnSaveInvoice;

    private String originalInvoiceNumber = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_form);

        etCustomerId = findViewById(R.id.etCustomerId);
        etInvoiceNumber = findViewById(R.id.etInvoiceNumber);
        etStatus = findViewById(R.id.etStatus);
        etIssueDate = findViewById(R.id.etIssueDate);
        etDueDate = findViewById(R.id.etDueDate);
        etSubtotal = findViewById(R.id.etSubtotal);
        etTaxTotal = findViewById(R.id.etTaxTotal);
        etTotal = findViewById(R.id.etTotal);
        btnSaveInvoice = findViewById(R.id.btnSaveInvoice);

        if (getIntent() != null && getIntent().hasExtra("invoiceNumber")) {
            originalInvoiceNumber = getIntent().getStringExtra("invoiceNumber");
            if (originalInvoiceNumber != null && !originalInvoiceNumber.isEmpty()) {
                loadInvoice(originalInvoiceNumber);
            }
        }

        btnSaveInvoice.setOnClickListener(v -> saveInvoice());
    }

    private void loadInvoice(String invoiceNumber) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getInvoiceByNumber(invoiceNumber).enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InvoiceResponse item = response.body();

                    etInvoiceNumber.setText(item.getInvoiceNumber() != null ? item.getInvoiceNumber() : "");
                    etStatus.setText(item.getStatus() != null ? item.getStatus() : "");
                    etIssueDate.setText(item.getIssueDate() != null ? item.getIssueDate() : "");
                    etDueDate.setText(item.getDueDate() != null ? item.getDueDate() : "");
                    etSubtotal.setText(item.getSubtotal() != null ? String.valueOf(item.getSubtotal()) : "");
                    etTaxTotal.setText(item.getTaxTotal() != null ? String.valueOf(item.getTaxTotal()) : "");
                    etTotal.setText(item.getTotal() != null ? String.valueOf(item.getTotal()) : "");
                } else {
                    Toast.makeText(InvoiceFormActivity.this, "Failed to load invoice", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                Toast.makeText(InvoiceFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveInvoice() {
        String customerIdText = etCustomerId.getText().toString().trim();
        String invoiceNumber = etInvoiceNumber.getText().toString().trim();
        String status = etStatus.getText().toString().trim();
        String issueDate = etIssueDate.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();
        String subtotalText = etSubtotal.getText().toString().trim();
        String taxTotalText = etTaxTotal.getText().toString().trim();
        String totalText = etTotal.getText().toString().trim();

        if (customerIdText.isEmpty() || invoiceNumber.isEmpty()) {
            Toast.makeText(this, "Customer Id and Invoice Number are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer customerId;
        Double subtotal;
        Double taxTotal;
        Double total;

        try {
            customerId = Integer.parseInt(customerIdText);
            subtotal = subtotalText.isEmpty() ? 0.0 : Double.parseDouble(subtotalText);
            taxTotal = taxTotalText.isEmpty() ? 0.0 : Double.parseDouble(taxTotalText);
            total = totalText.isEmpty() ? 0.0 : Double.parseDouble(totalText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Numeric fields are invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        InvoiceRequest request = new InvoiceRequest(
                customerId,
                invoiceNumber,
                status.isEmpty() ? null : status,
                issueDate.isEmpty() ? null : issueDate,
                dueDate.isEmpty() ? null : dueDate,
                subtotal,
                taxTotal,
                total
        );

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if (originalInvoiceNumber == null) {
            apiService.createInvoice(request).enqueue(new Callback<InvoiceResponse>() {
                @Override
                public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(InvoiceFormActivity.this, "Invoice created", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(InvoiceFormActivity.this, "Create failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                    Toast.makeText(InvoiceFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            apiService.updateInvoice(originalInvoiceNumber, request).enqueue(new Callback<InvoiceResponse>() {
                @Override
                public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(InvoiceFormActivity.this, "Invoice updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(InvoiceFormActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                    Toast.makeText(InvoiceFormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
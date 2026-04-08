package com.example.workshop06;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.InvoiceRequest;
import com.example.workshop06.model.InvoiceResponse;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceFormActivity extends AppCompatActivity {

    private EditText etCustomerName, etInvoiceNumber, etIssueDate, etDueDate,
            etSubtotal, etTaxTotal, etTotal;
    private MaterialAutoCompleteTextView spinnerInvoiceStatus;
    private TextInputLayout tilIssueDate, tilDueDate;
    private Button btnSaveInvoice;

    private String originalInvoiceNumber = null;
    private Integer customerId = null;

    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_form);

        etCustomerName = findViewById(R.id.etCustomerName);
        etInvoiceNumber = findViewById(R.id.etInvoiceNumber);
        spinnerInvoiceStatus = findViewById(R.id.spinnerInvoiceStatus);
        etIssueDate = findViewById(R.id.etIssueDate);
        etDueDate = findViewById(R.id.etDueDate);
        etSubtotal = findViewById(R.id.etSubtotal);
        etTaxTotal = findViewById(R.id.etTaxTotal);
        etTotal = findViewById(R.id.etTotal);
        tilIssueDate = findViewById(R.id.tilIssueDate);
        tilDueDate = findViewById(R.id.tilDueDate);
        btnSaveInvoice = findViewById(R.id.btnSaveInvoice);
        btnBack = findViewById(R.id.btnBack);
        setupStatusDropdown();
        setupDatePickers();

        if (getIntent() != null && getIntent().hasExtra("invoiceNumber")) {
            originalInvoiceNumber = getIntent().getStringExtra("invoiceNumber");
            if (originalInvoiceNumber != null && !originalInvoiceNumber.isEmpty()) {
                loadInvoice(originalInvoiceNumber);
            }
        }
        btnBack.setOnClickListener(v -> finish());
        btnSaveInvoice.setOnClickListener(v -> saveInvoice());
    }

    private void setupStatusDropdown() {
        String[] statuses = {"Active", "Inactive"};

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statuses
        );

        spinnerInvoiceStatus.setAdapter(statusAdapter);
        spinnerInvoiceStatus.setText("Active", false);

        spinnerInvoiceStatus.setOnClickListener(v -> spinnerInvoiceStatus.showDropDown());
        spinnerInvoiceStatus.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                spinnerInvoiceStatus.showDropDown();
            }
        });
    }

    private void setupDatePickers() {
        etIssueDate.setOnClickListener(v -> showDatePicker(etIssueDate));
        etDueDate.setOnClickListener(v -> showDatePicker(etDueDate));

        tilIssueDate.setEndIconOnClickListener(v -> showDatePicker(etIssueDate));
        tilDueDate.setEndIconOnClickListener(v -> showDatePicker(etDueDate));
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formatted = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );
                    target.setText(formatted);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void loadInvoice(String invoiceNumber) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getInvoiceByNumber(invoiceNumber).enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InvoiceResponse item = response.body();

                    customerId = item.getCustomerId();

                    etCustomerName.setText(item.getCustomerName() != null ? item.getCustomerName() : "");
                    etInvoiceNumber.setText(item.getInvoiceNumber() != null ? item.getInvoiceNumber() : "");
                    spinnerInvoiceStatus.setText(
                            item.getStatus() != null && !item.getStatus().trim().isEmpty()
                                    ? item.getStatus()
                                    : "Active",
                            false
                    );
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
        String invoiceNumber = etInvoiceNumber.getText() != null
                ? etInvoiceNumber.getText().toString().trim()
                : "";
        String status = spinnerInvoiceStatus.getText() != null
                ? spinnerInvoiceStatus.getText().toString().trim()
                : "";
        String issueDate = etIssueDate.getText() != null
                ? etIssueDate.getText().toString().trim()
                : "";
        String dueDate = etDueDate.getText() != null
                ? etDueDate.getText().toString().trim()
                : "";
        String subtotalText = etSubtotal.getText() != null
                ? etSubtotal.getText().toString().trim()
                : "";
        String taxTotalText = etTaxTotal.getText() != null
                ? etTaxTotal.getText().toString().trim()
                : "";
        String totalText = etTotal.getText() != null
                ? etTotal.getText().toString().trim()
                : "";

        if (customerId == null) {
            Toast.makeText(this, "Customer information is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (invoiceNumber.isEmpty()) {
            Toast.makeText(this, "Invoice Number is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Double subtotal;
        Double taxTotal;
        Double total;

        try {
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
                status.isEmpty() ? "Active" : status,
                issueDate.isEmpty() ? null : issueDate,
                dueDate.isEmpty() ? null : dueDate,
                subtotal,
                taxTotal,
                total
        );

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.updateInvoice(invoiceNumber, request).enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InvoiceFormActivity.this, "Invoice updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
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
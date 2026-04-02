package com.example.workshop06;

import android.app.AlertDialog;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.ServiceRequestAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.ServiceRequestResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceRequestListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchViewServiceRequest;
    private ImageButton btnBack;
    private FloatingActionButton fabAdd;

    private ServiceRequestAdapter adapter;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                loadServiceRequests();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_request_list);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupButtons();
        loadServiceRequests();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewServiceRequests);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewServiceRequest = findViewById(R.id.searchViewServiceRequest);
        btnBack = findViewById(R.id.btnBack);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        adapter = new ServiceRequestAdapter(new ServiceRequestAdapter.OnRequestActionListener() {
            @Override
            public void onEdit(ServiceRequestResponse item) {
                Intent intent = new Intent(ServiceRequestListActivity.this, ServiceRequestFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("requestId", item.getRequestId());
                intent.putExtra("customerId", item.getCustomerId());
                intent.putExtra("createdByUserId", item.getCreatedByUserId());
                intent.putExtra("assignedTechnicianUserId", item.getAssignedTechnicianUserId());
                intent.putExtra("requestType", item.getRequestType());
                intent.putExtra("priority", item.getPriority());
                intent.putExtra("status", item.getStatus());
                intent.putExtra("description", item.getDescription());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(ServiceRequestResponse item) {
                if (item.getRequestId() == null) return;

                new AlertDialog.Builder(ServiceRequestListActivity.this)
                        .setTitle("Delete Service Request")
                        .setMessage("Are you sure you want to delete this service request?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteServiceRequest(item.getRequestId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onAppointments(ServiceRequestResponse item) {
                if (item.getRequestId() == null) return;

                Intent intent = new Intent(ServiceRequestListActivity.this, ServiceAppointmentListActivity.class);
                intent.putExtra("requestId", item.getRequestId());
                intent.putExtra("customerName", item.getCustomerName());
                intent.putExtra("requestType", item.getRequestType());
                intent.putExtra("status", item.getStatus());
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchViewServiceRequest == null) return;

        searchViewServiceRequest.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                updateEmptyState();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                updateEmptyState();
                return true;
            }
        });
    }

    private void setupButtons() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(ServiceRequestListActivity.this, ServiceRequestFormActivity.class);
                intent.putExtra("mode", "add");
                formLauncher.launch(intent);
            });
        }
    }

    private void loadServiceRequests() {
        showLoading(true);
        showEmpty(false);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getServiceRequests().enqueue(new Callback<List<ServiceRequestResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceRequestResponse>> call, Response<List<ServiceRequestResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    showError("Failed to load service requests. Code: " + response.code());
                    return;
                }

                List<ServiceRequestResponse> data = response.body();
                adapter.setData(data);

                if (data == null || data.isEmpty()) {
                    showEmpty(true);
                } else {
                    showEmpty(false);
                }
            }

            @Override
            public void onFailure(Call<List<ServiceRequestResponse>> call, Throwable t) {
                showLoading(false);
                adapter.setData(null);
                showError("Unable to load service requests");
            }
        });
    }

    private void deleteServiceRequest(int requestId) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteServiceRequest(requestId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(ServiceRequestListActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadServiceRequests();
                } else {
                    showError("Delete failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Unable to delete service request");
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        if (recyclerView != null) {
            recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }

        if (tvEmpty != null && isLoading) {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmpty(boolean isEmpty) {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        if (recyclerView != null && progressBar != null && progressBar.getVisibility() != View.VISIBLE) {
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void updateEmptyState() {
        showEmpty(adapter == null || adapter.getItemCount() == 0);
    }

    private void showError(String message) {
        updateEmptyState();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
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

import com.example.workshop06.adapter.ServiceAppointmentAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.ServiceAppointmentResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceAppointmentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvSubtitle;
    private SearchView searchView;

    private FloatingActionButton fabAdd;

    private ServiceAppointmentAdapter adapter;
    private int requestId = -1;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadAppointments());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_appointment_list);

        requestId = getIntent().getIntExtra("requestId", -1);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupButtons();

        String customerName = getIntent().getStringExtra("customerName");
        String requestType = getIntent().getStringExtra("requestType");
        if (tvSubtitle != null) {
            tvSubtitle.setText((customerName != null ? customerName : "Service Request")
                    + (requestType != null ? " • " + requestType : ""));
        }

        if (requestId <= 0) {
            Toast.makeText(this, "Invalid request ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadAppointments();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewAppointments);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        searchView = findViewById(R.id.searchViewAppointment);

        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        adapter = new ServiceAppointmentAdapter(new ServiceAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onEdit(ServiceAppointmentResponse item) {
                Intent intent = new Intent(ServiceAppointmentListActivity.this, ServiceAppointmentFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("requestId", requestId);
                intent.putExtra("appointmentId", item.getAppointmentId());
                intent.putExtra("technicianUserId", item.getTechnicianUserId());
                intent.putExtra("addressId", item.getAddressId());
                intent.putExtra("locationId", item.getLocationId());
                intent.putExtra("locationType", item.getLocationType());
                intent.putExtra("scheduledStart", item.getScheduledStart());
                intent.putExtra("scheduledEnd", item.getScheduledEnd());
                intent.putExtra("status", item.getStatus());
                intent.putExtra("notes", item.getNotes());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(ServiceAppointmentResponse item) {
                if (item.getAppointmentId() == null) return;

                new AlertDialog.Builder(ServiceAppointmentListActivity.this)
                        .setTitle("Delete Appointment")
                        .setMessage("Are you sure you want to delete this appointment?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteAppointment(item.getAppointmentId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                updateEmptyState();
                return true;
            }

            @Override public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                updateEmptyState();
                return true;
            }
        });
    }

    private void setupButtons() {


        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ServiceAppointmentListActivity.this, ServiceAppointmentFormActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("requestId", requestId);
            formLauncher.launch(intent);
        });
    }

    private void loadAppointments() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getServiceAppointments(requestId).enqueue(new Callback<List<ServiceAppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<ServiceAppointmentResponse>> call, Response<List<ServiceAppointmentResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    showError("Failed to load appointments. Code: " + response.code());
                    return;
                }

                List<ServiceAppointmentResponse> data = response.body();
                adapter.setData(data);
                showEmpty(data == null || data.isEmpty());
            }

            @Override
            public void onFailure(Call<List<ServiceAppointmentResponse>> call, Throwable t) {
                showLoading(false);
                adapter.setData(null);
                showError("Unable to load appointments");
            }
        });
    }

    private void deleteAppointment(int appointmentId) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteServiceAppointment(requestId, appointmentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(ServiceAppointmentListActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadAppointments();
                } else {
                    showError("Delete failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Unable to delete appointment");
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (isLoading) tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(boolean isEmpty) {
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (progressBar.getVisibility() != View.VISIBLE) {
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
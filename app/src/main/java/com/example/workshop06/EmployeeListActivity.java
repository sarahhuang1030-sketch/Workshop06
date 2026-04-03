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

import com.example.workshop06.adapter.EmployeeAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.EmployeeResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchViewEmployee;

    private FloatingActionButton fabAdd;

    private EmployeeAdapter adapter;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                loadEmployees();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupButtons();
        loadEmployees();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewEmployees);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchViewEmployee = findViewById(R.id.searchViewEmployee);

        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        adapter = new EmployeeAdapter(new EmployeeAdapter.OnEmployeeActionListener() {
            @Override
            public void onEdit(EmployeeResponse item) {
                Intent intent = new Intent(EmployeeListActivity.this, EmployeeFormActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("employeeId", item.getEmployeeId());
                intent.putExtra("primaryLocationId", item.getPrimaryLocationId());
                intent.putExtra("firstName", item.getFirstName());
                intent.putExtra("lastName", item.getLastName());
                intent.putExtra("email", item.getEmail());
                intent.putExtra("phone", item.getPhone());
                intent.putExtra("role", item.getRole());
                intent.putExtra("salary", item.getSalary() != null ? item.getSalary() : Double.NaN);
                intent.putExtra("hireDate", item.getHireDate());
                intent.putExtra("status", item.getStatus());
                intent.putExtra("active", item.getActive() != null ? item.getActive() : Integer.MIN_VALUE);
                intent.putExtra("managerId", item.getManagerId() != null ? item.getManagerId() : Integer.MIN_VALUE);
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(EmployeeResponse item) {
                if (item.getEmployeeId() == null) return;

                new AlertDialog.Builder(EmployeeListActivity.this)
                        .setTitle("Delete Employee")
                        .setMessage("Are you sure you want to delete this employee?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteEmployee(item.getEmployeeId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchViewEmployee == null) return;

        searchViewEmployee.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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


        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(EmployeeListActivity.this, EmployeeFormActivity.class);
                intent.putExtra("mode", "add");
                formLauncher.launch(intent);
            });
        }
    }

    private void loadEmployees() {
        showLoading(true);
        showEmpty(false);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getEmployees().enqueue(new Callback<List<EmployeeResponse>>() {
            @Override
            public void onResponse(Call<List<EmployeeResponse>> call, Response<List<EmployeeResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    showError("Failed to load employees. Code: " + response.code());
                    return;
                }

                List<EmployeeResponse> data = response.body();
                adapter.setData(data);
                showEmpty(data == null || data.isEmpty());
            }

            @Override
            public void onFailure(Call<List<EmployeeResponse>> call, Throwable t) {
                showLoading(false);
                adapter.setData(null);
                showError("Unable to load employees");
            }
        });
    }

    private void deleteEmployee(int employeeId) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteEmployee(employeeId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(EmployeeListActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    loadEmployees();
                } else {
                    showError("Delete failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showError("Unable to delete employee");
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
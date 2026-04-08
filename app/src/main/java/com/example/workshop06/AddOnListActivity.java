package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
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

import com.example.workshop06.adapter.AddOnAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.AddOnResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddOnListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAddAddOn;
    private SearchView searchViewAddOns;
    private TextInputEditText etFilterPrice;
    private MaterialAutoCompleteTextView spinnerStatusFilter;

    private AddOnAdapter adapter;

    private final List<AddOnResponse> allAddOns = new ArrayList<>();
    private final List<AddOnResponse> filteredAddOns = new ArrayList<>();

    private String searchText = "";
    private String selectedStatus = "All";
    private Double maxPriceFilter = null;

    private ImageButton btnBack;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadAddOns());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_list);

        recyclerView = findViewById(R.id.recyclerAddOns);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAddAddOn = findViewById(R.id.fabAddAddOn);
        searchViewAddOns = findViewById(R.id.searchViewAddOns);
        etFilterPrice = findViewById(R.id.etFilterPrice);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AddOnAdapter(filteredAddOns, new AddOnAdapter.AddOnActionListener() {
            @Override
            public void onEdit(AddOnResponse item) {
                Intent intent = new Intent(AddOnListActivity.this, AddOnFormActivity.class);
                intent.putExtra("addOnId", item.getAddOnId());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(AddOnResponse item) {
                deleteAddOn(item.getAddOnId());
            }
        });

        recyclerView.setAdapter(adapter);

        fabAddAddOn.setOnClickListener(v -> {
            Intent intent = new Intent(AddOnListActivity.this, AddOnFormActivity.class);
            formLauncher.launch(intent);
        });

        setupFilters();
        BottomNavHelper.setup(this, 0);
        loadAddOns();
    }

    private void setupFilters() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Active", "Inactive"}
        );
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setText("All", false);

        spinnerStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatus = parent.getItemAtPosition(position).toString();
            applyFilters();
        });

        searchViewAddOns.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query != null ? query.trim() : "";
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText != null ? newText.trim() : "";
                applyFilters();
                return true;
            }
        });

        etFilterPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s != null ? s.toString().trim() : "";
                if (value.isEmpty()) {
                    maxPriceFilter = null;
                } else {
                    try {
                        maxPriceFilter = Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        maxPriceFilter = null;
                    }
                }
                applyFilters();
            }
        });
    }

    private void loadAddOns() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getAddOns().enqueue(new Callback<List<AddOnResponse>>() {
            @Override
            public void onResponse(Call<List<AddOnResponse>> call, Response<List<AddOnResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allAddOns.clear();
                    allAddOns.addAll(response.body());
                    applyFilters();
                } else {
                    Toast.makeText(AddOnListActivity.this, "Failed to load add-ons", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<AddOnResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddOnListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }

    private void applyFilters() {
        filteredAddOns.clear();

        String q = searchText == null ? "" : searchText.toLowerCase(Locale.US);

        for (AddOnResponse item : allAddOns) {
            String name = item.getAddOnName() != null ? item.getAddOnName().toLowerCase(Locale.US) : "";
            String description = item.getDescription() != null ? item.getDescription().toLowerCase(Locale.US) : "";
            boolean isActive = Boolean.TRUE.equals(item.getIsActive());

            boolean matchesSearch = q.isEmpty()
                    || name.contains(q)
                    || description.contains(q);

            boolean matchesStatus = "All".equals(selectedStatus)
                    || ("Active".equals(selectedStatus) && isActive)
                    || ("Inactive".equals(selectedStatus) && !isActive);

            boolean matchesPrice = true;
            if (maxPriceFilter != null) {
                double price = item.getMonthlyPrice() != null ? item.getMonthlyPrice() : 0.0;
                matchesPrice = price <= maxPriceFilter;
            }

            if (matchesSearch && matchesStatus && matchesPrice) {
                filteredAddOns.add(item);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredAddOns.isEmpty()) {
            showEmptyState();
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void deleteAddOn(Integer addOnId) {
        if (addOnId == null) {
            Toast.makeText(this, "Invalid add-on id", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteAddOn(addOnId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddOnListActivity.this, "Add-on deleted", Toast.LENGTH_SHORT).show();
                    loadAddOns();
                } else {
                    Toast.makeText(AddOnListActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddOnListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
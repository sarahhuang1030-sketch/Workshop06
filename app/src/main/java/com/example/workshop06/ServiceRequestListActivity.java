package com.example.workshop06;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.adapter.ServiceRequestAdapter;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.EmployeeResponse;
import com.example.workshop06.model.ServiceRequestResponse;
import com.example.workshop06.model.ServiceTicketDTO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceRequestListActivity extends BaseActivity {

    @Override
    protected void onRefresh() { loadServiceRequests(); }

    // True when the logged-in user is a SERVICE_TECHNICIAN.
    // Drives all role-based UI differences in this screen.
    private boolean isTechnician = false;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView txtServiceRequestTitle;
    private SearchView searchViewServiceRequest;
    private FloatingActionButton fabAdd;
    private MaterialAutoCompleteTextView spinnerTechnicianFilter;
    private MaterialAutoCompleteTextView spinnerPriorityFilter;
    private ImageButton btnBack;

    private ServiceRequestAdapter adapter;

    private String currentSearch      = "";
    private String selectedTechnician = "All";
    private String selectedPriority   = "All";

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> loadServiceRequests()
            );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_request_list);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Determine role from SharedPreferences (stored at login time).
        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String savedRole = prefs.getString("user_role", "");
        isTechnician = isTechnicianRoleString(savedRole);

        initViews();
        setupRecyclerView();  // adapter is created here
        setupSearch();
        setupButtons();       // FAB hidden for technicians
        setupStaticPriorityFilter();
        setupTechnicianFilter(); // spinner hidden for technicians
        loadServiceRequests();

        BottomNavHelper.setup(this, 0);
    }

    // -------------------------------------------------------------------------
    // Role detection helpers
    // -------------------------------------------------------------------------

    /**
     * Returns true when the role string from SharedPreferences belongs to a technician.
     * Uses contains() so it works regardless of exact casing or prefix differences.
     */
    private boolean isTechnicianRoleString(String role) {
        if (role == null) return false;
        String lower = role.trim().toLowerCase(Locale.US);
        return lower.contains("technician") || lower.equals("tech");
    }

    /**
     * Returns true when an EmployeeResponse from the API belongs to a technician.
     * Checks roleId == 3 first (most reliable), then falls back to string matching
     * across role / roleName / positionTitle fields — mirrors ServiceRequestFormActivity.
     */
    private boolean isTechnicianEmployee(EmployeeResponse emp) {
        if (emp == null) return false;

        Integer roleId = emp.getRoleId();
        if (roleId != null && roleId == 3) return true;

        String role          = safeLower(emp.getRole());
        String roleName      = safeLower(emp.getRoleName());
        String positionTitle = safeLower(emp.getPositionTitle());

        return role.contains("technician")         || role.equals("tech")
                || roleName.contains("technician") || roleName.equals("tech")
                || positionTitle.contains("technician") || positionTitle.equals("tech");
    }

    // -------------------------------------------------------------------------
    // View initialisation
    // -------------------------------------------------------------------------

    private void initViews() {
        recyclerView             = findViewById(R.id.recyclerViewServiceRequests);
        progressBar              = findViewById(R.id.progressBar);
        tvEmpty                  = findViewById(R.id.tvEmpty);
        searchViewServiceRequest = findViewById(R.id.searchViewServiceRequest);
        fabAdd                   = findViewById(R.id.fabAdd);
        spinnerTechnicianFilter  = findViewById(R.id.spinnerTechnicianFilter);
        spinnerPriorityFilter    = findViewById(R.id.spinnerPriorityFilter);
        txtServiceRequestTitle   = findViewById(R.id.txtServiceRequestTitle);

        String viewMode = getIntent().getStringExtra("ViewMode");
        if (viewMode != null && !viewMode.equals("Empty")) {
            txtServiceRequestTitle.setText(viewMode + " Requests");
        }
    }

    private void setupRecyclerView() {
        adapter = new ServiceRequestAdapter(new ServiceRequestAdapter.OnRequestActionListener() {

            @Override
            public void onEdit(ServiceRequestResponse item) {
                Intent intent = new Intent(ServiceRequestListActivity.this,
                        ServiceRequestFormActivity.class);
                intent.putExtra("mode",                     "edit");
                intent.putExtra("requestId",                item.getRequestId());
                intent.putExtra("customerId",               item.getCustomerId());
                intent.putExtra("createdByUserId",          item.getCreatedByUserId());
                intent.putExtra("assignedTechnicianUserId", item.getAssignedTechnicianUserId());
                intent.putExtra("requestType",              item.getRequestType());
                intent.putExtra("priority",                 item.getPriority());
                intent.putExtra("status",                   item.getStatus());
                intent.putExtra("description",              item.getDescription());
                intent.putExtra("customerName",             item.getCustomerName());
                intent.putExtra("createdByName",            item.getCreatedByName());
                intent.putExtra("technicianName",           item.getTechnicianName());
                formLauncher.launch(intent);
            }

            @Override
            public void onDelete(ServiceRequestResponse item) {
                if (item.getRequestId() == null) return;
                new AlertDialog.Builder(ServiceRequestListActivity.this)
                        .setTitle("Delete Service Request")
                        .setMessage("Are you sure you want to delete this service request?")
                        .setPositiveButton("Delete",
                                (dialog, which) -> deleteServiceRequest(item.getRequestId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onAppointments(ServiceRequestResponse item) {
                if (item.getRequestId() == null) return;
                Intent intent = new Intent(ServiceRequestListActivity.this,
                        ServiceAppointmentListActivity.class);
                intent.putExtra("customerId",               item.getCustomerId());
                intent.putExtra("requestId",                item.getRequestId());
                intent.putExtra("createdByUserId",          item.getCreatedByUserId());
                intent.putExtra("createdByName",            item.getCreatedByName());
                intent.putExtra("customerName",             item.getCustomerName());
                intent.putExtra("requestType",              item.getRequestType());
                intent.putExtra("description",              item.getDescription());
                intent.putExtra("status",                   item.getStatus());
                intent.putExtra("assignedTechnicianUserId", item.getAssignedTechnicianUserId());
                intent.putExtra("technicianName",           item.getTechnicianName());
                startActivity(intent);
            }
        });

        // FIX: put the adapter in read-only mode for technicians so that
        //      the Edit and Delete buttons are hidden in every card row.
        //      The Appointments (calendar) button remains visible for all roles.
        adapter.setReadOnlyMode(isTechnician);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchViewServiceRequest == null) return;
        searchViewServiceRequest.clearFocus();
        searchViewServiceRequest.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearch = query == null ? "" : query;
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearch = newText == null ? "" : newText;
                applyFilters();
                return true;
            }
        });
    }

    /**
     * Configure the FAB (Add button).
     * FIX: Technicians cannot create service requests, so the FAB is hidden for them.
     *      Only managers / employees see and can use it.
     */
    private void setupButtons() {
        if (fabAdd == null) return;

        if (isTechnician) {
            // Technicians have no permission to create service requests
            fabAdd.setVisibility(View.GONE);
        } else {
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(ServiceRequestListActivity.this,
                        ServiceRequestFormActivity.class);
                intent.putExtra("mode", "add");
                formLauncher.launch(intent);
            });
        }
    }

    private void setupStaticPriorityFilter() {
        if (spinnerPriorityFilter == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Low", "Medium", "High"}
        );
        spinnerPriorityFilter.setAdapter(adapter);
        spinnerPriorityFilter.setText("All", false);
        spinnerPriorityFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedPriority = parent.getItemAtPosition(position).toString();
            applyFilters();
        });
    }

    /**
     * Populate the Technician filter dropdown from the employee API.
     *
     * FIX 1 (role-based): Technicians get a 403 on /api/manager/employees and only
     *   ever see their own tickets, so the filter is irrelevant for them.
     *   The spinner and its parent TextInputLayout are hidden entirely.
     *
     * FIX 2 (matching logic): replaced the old hard-coded 3-string equalsIgnoreCase
     *   check with isTechnicianEmployee(), which also checks roleId == 3 and uses
     *   contains() across role / roleName / positionTitle.  The old check missed
     *   any role string that didn't exactly match one of the three literals.
     */
    private void setupTechnicianFilter() {
        if (spinnerTechnicianFilter == null) return;

        if (isTechnician) {
            // Hide the spinner's grandparent TextInputLayout so the empty space
            // is also removed from the layout, not just made invisible.
            View textInputLayout = (View) spinnerTechnicianFilter.getParent().getParent();
            textInputLayout.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.getEmployees().enqueue(new Callback<List<EmployeeResponse>>() {

            @Override
            public void onResponse(Call<List<EmployeeResponse>> call,
                                   Response<List<EmployeeResponse>> response) {

                List<String> names = new ArrayList<>();
                names.add("All"); // always first

                if (response.isSuccessful() && response.body() != null) {
                    for (EmployeeResponse emp : response.body()) {
                        // FIX: use unified helper instead of the old 3-string check
                        if (!isTechnicianEmployee(emp)) continue;

                        String first = emp.getFirstName() != null
                                ? emp.getFirstName().trim() : "";
                        String last  = emp.getLastName()  != null
                                ? emp.getLastName().trim()  : "";
                        String full  = (first + " " + last).trim();

                        if (!full.isEmpty() && !names.contains(full)) {
                            names.add(full);
                        }
                    }
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                        ServiceRequestListActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        names
                );
                spinnerTechnicianFilter.setAdapter(spinnerAdapter);
                spinnerTechnicianFilter.setText(
                        selectedTechnician == null ? "All" : selectedTechnician, false);
                spinnerTechnicianFilter.setOnItemClickListener((parent, view, position, id) -> {
                    selectedTechnician = parent.getItemAtPosition(position).toString();
                    applyFilters();
                });
            }

            @Override
            public void onFailure(Call<List<EmployeeResponse>> call, Throwable t) {
                // Network failure: keep the spinner usable with just "All"
                ArrayAdapter<String> fallback = new ArrayAdapter<>(
                        ServiceRequestListActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        new String[]{"All"}
                );
                spinnerTechnicianFilter.setAdapter(fallback);
                spinnerTechnicianFilter.setText("All", false);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Data loading
    // -------------------------------------------------------------------------

    private void loadServiceRequests() {
        showLoading(true);
        showEmpty(false);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        String viewMode = getIntent().getStringExtra("ViewMode");

        if (isTechnician) {
            // Technician path: /api/service/tickets — only returns tickets
            // assigned to the authenticated technician
            apiService.getMyTickets().enqueue(new Callback<List<ServiceTicketDTO>>() {

                @Override
                public void onResponse(Call<List<ServiceTicketDTO>> call,
                                       Response<List<ServiceTicketDTO>> response) {
                    showLoading(false);

                    if (!response.isSuccessful() || response.body() == null) {
                        adapter.setData(null);
                        showError("Failed to load tickets. Code: " + response.code());
                        return;
                    }

                    // Convert ServiceTicketDTO → ServiceRequestResponse for the shared adapter
                    List<ServiceRequestResponse> converted = new ArrayList<>();
                    for (ServiceTicketDTO t : response.body()) {
                        ServiceRequestResponse r = new ServiceRequestResponse();
                        r.setRequestId(t.getRequestId());
                        r.setCustomerId(t.getCustomerId());
                        r.setCustomerName(t.getCustomerName());
                        r.setRequestType(t.getRequestType());
                        r.setPriority(t.getPriority());
                        r.setStatus(t.getStatus());
                        r.setDescription(t.getDescription());
                        r.setAssignedTechnicianUserId(t.getTechnicianUserId());
                        // FIX: these two fields were previously not copied,
                        //      causing tvTechnician and tvAddress to always show "—"
                        r.setTechnicianName(t.getTechnicianName());
                        r.setAddressText(t.getAddressText());
                        converted.add(r);
                    }

                    // Keep only "Completed" rows when opened in Completed mode
                    if ("Completed".equals(viewMode)) {
                        converted.removeIf(sr ->
                                !sr.getStatus().equalsIgnoreCase("Completed"));
                    }

                    adapter.setData(converted);
                    applyFilters();
                }

                @Override
                public void onFailure(Call<List<ServiceTicketDTO>> call, Throwable t) {
                    showLoading(false);
                    adapter.setData(null);
                    showError("Unable to load tickets");
                }
            });

        } else {
            // Manager / employee path: /api/manager/service-requests
            apiService.getServiceRequests().enqueue(new Callback<List<ServiceRequestResponse>>() {

                @Override
                public void onResponse(Call<List<ServiceRequestResponse>> call,
                                       Response<List<ServiceRequestResponse>> response) {
                    showLoading(false);

                    if (!response.isSuccessful()) {
                        adapter.setData(null);
                        showError("Failed to load service requests. Code: " + response.code());
                        return;
                    }

                    List<ServiceRequestResponse> data = response.body();
                    if (data == null) data = new ArrayList<>();

                    // Keep only "Completed" rows when opened in Completed mode
                    if ("Completed".equals(viewMode)) {
                        data.removeIf(sr ->
                                !sr.getStatus().equalsIgnoreCase("Completed"));
                    }

                    adapter.setData(data);
                    applyFilters();
                }

                @Override
                public void onFailure(Call<List<ServiceRequestResponse>> call, Throwable t) {
                    showLoading(false);
                    adapter.setData(null);
                    showError("Unable to load service requests");
                }
            });
        }
    }

    private void applyFilters() {
        if (adapter == null) return;
        adapter.applyFilters(currentSearch, selectedTechnician, selectedPriority);
        updateEmptyState();
    }

    private void deleteServiceRequest(int requestId) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.deleteServiceRequest(requestId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ServiceRequestListActivity.this,
                            "Deleted successfully", Toast.LENGTH_SHORT).show();
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

    // -------------------------------------------------------------------------
    // UI state helpers
    // -------------------------------------------------------------------------

    private void showLoading(boolean isLoading) {
        if (progressBar != null)
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (recyclerView != null)
            recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (tvEmpty != null && isLoading)
            tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(boolean isEmpty) {
        if (tvEmpty != null)
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (recyclerView != null && progressBar != null
                && progressBar.getVisibility() != View.VISIBLE)
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        showEmpty(adapter == null || adapter.getItemCount() == 0);
    }

    private void showError(String message) {
        updateEmptyState();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /** Null-safe lowercase trim helper. */
    private String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }
}
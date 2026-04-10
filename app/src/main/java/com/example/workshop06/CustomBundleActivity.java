package com.example.workshop06;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.*;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomBundleActivity extends AppCompatActivity {

    // =========================
    // UI
    // =========================
    private Spinner spinnerCustomer;
    private RadioGroup rgServiceType;
    private LinearLayout planContainer, addonContainer;
    private TextView tvTotal, tvSummary;
    private Button btnSubmit;

    // =========================
    // DATA
    // =========================
    private List<CustomerResponse> customers = new ArrayList<>();
    private List<PlanResponse> plans = new ArrayList<>();
    private List<AddOnResponse> addons = new ArrayList<>();
    private Integer selectedPlanId;
    private List<Integer> selectedAddonIds = new ArrayList<>();
    private double total = 0;

    private Integer selectedCustomerId;
    private String selectedServiceType = "Internet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_bundle);

        initViews();
        loadData();
    }

    // =========================
    // INIT UI
    // =========================
    private void initViews() {

        spinnerCustomer = findViewById(R.id.spinnerCustomer);
        rgServiceType = findViewById(R.id.rgServiceType);

        planContainer = findViewById(R.id.planContainer);
        addonContainer = findViewById(R.id.addonContainer);

        tvTotal = findViewById(R.id.tvTotal);
        tvSummary = findViewById(R.id.tvSummary);

        btnSubmit = findViewById(R.id.btnSubmit);

        // service type
        rgServiceType.setOnCheckedChangeListener((group, checkedId) -> {
            selectedServiceType = checkedId == R.id.rbMobile ? "Mobile" : "Internet";
            selectedPlanId = null;
            selectedAddonIds.clear();
            total = 0;
            loadData();
        });

        btnSubmit.setOnClickListener(v -> createQuote());
    }

    // =========================
    // LOAD ALL DATA
    // =========================
    private void loadData() {

        ApiService api = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        api.getCustomers().enqueue(new Callback<List<CustomerResponse>>() {
            @Override
            public void onResponse(Call<List<CustomerResponse>> call, Response<List<CustomerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    customers = response.body();
                    setupCustomerSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<CustomerResponse>> call, Throwable t) {}
        });

        api.getPlans().enqueue(new Callback<List<PlanResponse>>() {
            @Override
            public void onResponse(Call<List<PlanResponse>> call, Response<List<PlanResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    plans = filterPlans(response.body());
                    renderPlans();
                }
            }

            @Override
            public void onFailure(Call<List<PlanResponse>> call, Throwable t) {}
        });

        api.getAddOns().enqueue(new Callback<List<AddOnResponse>>() {
            @Override
            public void onResponse(Call<List<AddOnResponse>> call, Response<List<AddOnResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    addons = filterAddons(response.body());
                    renderAddons();
                }
            }

            @Override
            public void onFailure(Call<List<AddOnResponse>> call, Throwable t) {}
        });
    }

    // =========================
    // CUSTOMER SPINNER
    // =========================
    private void setupCustomerSpinner() {

        List<String> names = new ArrayList<>();

        for (CustomerResponse c : customers) {
            names.add(c.getFirstName() + " " + c.getLastName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                names
        );

        spinnerCustomer.setAdapter(adapter);

        spinnerCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCustomerId = customers.get(position).getCustomerId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // =========================
    // FILTER
    // =========================
//    private List<PlanResponse> filterPlans(List<PlanResponse> list) {
//        List<PlanResponse> res = new ArrayList<>();
//        for (PlanResponse p : list) {
//            if (selectedServiceType.equalsIgnoreCase(p.getServiceType())) {
//                res.add(p);
//            }
//        }
//        return res;
//    }
    private List<PlanResponse> filterPlans(List<PlanResponse> list) {
        return list;
    }

//    private List<AddOnResponse> filterAddons(List<AddOnResponse> list) {
//        List<AddOnResponse> res = new ArrayList<>();
//        for (AddOnResponse a : list) {
//            if (selectedServiceType.equalsIgnoreCase(a.getServiceTypeName())) {
//                res.add(a);
//            }
//        }
//        return res;
//    }
    private List<AddOnResponse> filterAddons(List<AddOnResponse> list) {
        return list;
    }

    // =========================
    // RENDER PLANS
    // =========================
    private void renderPlans() {
        planContainer.removeAllViews();

        for (PlanResponse p : plans) {

            CheckBox cb = new CheckBox(this);
            cb.setText(p.getPlanName() + " - $" + p.getMonthlyPrice());

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked) {
                    selectedPlanId = p.getPlanId();
                } else {
                    selectedPlanId = null;
                }

                recalc();
            });

            planContainer.addView(cb);
        }
    }

    // =========================
    // RENDER ADDONS
    // =========================
    private void renderAddons() {

        addonContainer.removeAllViews();

        for (AddOnResponse a : addons) {

            CheckBox cb = new CheckBox(this);
            cb.setText(a.getAddOnName() + " - $" + a.getMonthlyPrice());

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked) {
                    selectedAddonIds.add(a.getAddOnId());
                } else {
                    selectedAddonIds.remove(Integer.valueOf(a.getAddOnId()));
                }

                recalc();
            });

            addonContainer.addView(cb);
        }
    }

    // =========================
    // REMOVE ITEM
    // =========================
//    private void removeItem(Item item) {
//        selectedItems.removeIf(x ->
//                x.id.equals(item.id) && x.type.equals(item.type)
//        );
//    }

    // =========================
    // RECALC TOTAL
    // =========================
    private void recalc() {

        total = 0;

        for (PlanResponse p : plans) {
            if (selectedPlanId != null && p.getPlanId().equals(selectedPlanId)) {
                total += p.getMonthlyPrice();
            }
        }

        for (AddOnResponse a : addons) {
            if (selectedAddonIds.contains(a.getAddOnId())) {
                total += a.getMonthlyPrice();
            }
        }

        tvTotal.setText("Total: $" + total);
    }

    // =========================
    // CREATE QUOTE (IMPORTANT)
    // =========================
    private void createQuote() {

        if (selectedCustomerId == null) {
            Toast.makeText(this, "Select customer", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        QuoteRequest request = new QuoteRequest(
                selectedCustomerId,
                selectedPlanId,
                selectedAddonIds,
                total,
                "PENDING"
        );

        api.createQuote(request).enqueue(new Callback<QuoteResponse>() {
            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(CustomBundleActivity.this,
                            "Quote created",
                            Toast.LENGTH_SHORT).show();

                    finish();
                } else {
                    Toast.makeText(CustomBundleActivity.this,
                            "Failed",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QuoteResponse> call, Throwable t) {
                Toast.makeText(CustomBundleActivity.this,
                        t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // =========================
    // SIMPLE CALLBACK
    // =========================
//    static class SimpleCallback<T> implements Callback<List<T>> {
//
//        interface Handler<T> {
//            void handle(List<T> data);
//        }
//
//        private final Handler<T> handler;
//
//        SimpleCallback(Handler<T> handler) {
//            this.handler = handler;
//        }
//
//        @Override
//        public void onResponse(Call<List<T>> call, Response<List<T>> response) {
//            if (response.isSuccessful() && response.body() != null) {
//                handler.handle(response.body());
//            }
//        }
//
//        @Override
//        public void onFailure(Call<List<T>> call, Throwable t) {}
//    }

}
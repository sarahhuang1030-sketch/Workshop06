package com.example.workshop06;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.CustomerAddressResponse;
import com.example.workshop06.model.SaveCustomerAddressRequest;
import com.example.workshop06.util.FormFormatUtils;
import com.example.workshop06.util.ValidationUtils;
import android.content.SharedPreferences;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerAddressFormActivity extends AppCompatActivity {

    private static final String TAG = "CustomerAddressForm";

    private EditText etStreet1, etStreet2, etCity, etProvince, etPostalCode, etCountry;
    private EditText etServiceStreet1, etServiceStreet2, etServiceCity, etServiceProvince, etServicePostalCode, etServiceCountry;
    private ImageButton btnBack;
    private Button btnSave;
    private ProgressBar progressBar;
    private CheckBox cbSameAsBilling;
    private LinearLayout layoutServiceAddressSection;

    private int customerId = -1;
    private boolean readOnly = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_address_form);

        initViews();

        customerId = getIntent().getIntExtra("customerId", -1);
        readOnly = getIntent().getBooleanExtra("readOnly", false);


        setupFormatting();
        setupSameAsBillingToggle();

        if (customerId <= 0) {
            Toast.makeText(this, "Invalid customer id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (readOnly) {
            makeReadOnly();
        } else {
            setupButtons();
        }

        loadAddresses();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        etStreet1 = findViewById(R.id.etStreet1);
        etStreet2 = findViewById(R.id.etStreet2);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etPostalCode = findViewById(R.id.etPostalCode);
        etCountry = findViewById(R.id.etCountry);

        etServiceStreet1 = findViewById(R.id.etServiceStreet1);
        etServiceStreet2 = findViewById(R.id.etServiceStreet2);
        etServiceCity = findViewById(R.id.etServiceCity);
        etServiceProvince = findViewById(R.id.etServiceProvince);
        etServicePostalCode = findViewById(R.id.etServicePostalCode);
        etServiceCountry = findViewById(R.id.etServiceCountry);
        btnBack = findViewById(R.id.btnBack);
        cbSameAsBilling = findViewById(R.id.cbSameAsBilling);
        layoutServiceAddressSection = findViewById(R.id.layoutServiceAddressSection);

        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        btnBack.setOnClickListener(v -> finish());
    }



    private void setupFormatting() {
        FormFormatUtils.attachCanadianPostalCodeFormatter(etPostalCode);
        FormFormatUtils.attachCanadianPostalCodeFormatter(etServicePostalCode);
    }

    private void setupSameAsBillingToggle() {
        cbSameAsBilling.setChecked(true);
        layoutServiceAddressSection.setVisibility(View.GONE);

        cbSameAsBilling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutServiceAddressSection.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> saveAddresses());
    }

    private void loadAddresses() {
        showLoading(true);

        // ✅ GET ROLE FROM SHARED PREFERENCES
        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "");
        boolean isTechnician = "Service Technician".equalsIgnoreCase(role);

        // ✅ API SERVICE
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        // ✅ CALL CORRECT ENDPOINT BASED ON ROLE
        Call<List<CustomerAddressResponse>> call = isTechnician
                ? apiService.getCustomerAddressesForTechnician(customerId)
                : apiService.getCustomerAddresses(customerId);

        call.enqueue(new Callback<List<CustomerAddressResponse>>() {
            @Override
            public void onResponse(Call<List<CustomerAddressResponse>> call, Response<List<CustomerAddressResponse>> response) {
                showLoading(false);

                if (!response.isSuccessful()) {
                    Toast.makeText(CustomerAddressFormActivity.this,
                            "Failed to load address. Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                List<CustomerAddressResponse> list = response.body();
                if (list == null || list.isEmpty()) return;

                // ✅ fill billing + service
                CustomerAddressResponse billing = null;
                CustomerAddressResponse service = null;

                for (CustomerAddressResponse a : list) {
                    if ("Billing".equalsIgnoreCase(a.getAddressType())) {
                        billing = a;
                    } else if ("Service".equalsIgnoreCase(a.getAddressType())) {
                        service = a;
                    }
                }

                if (billing != null) {
                    etStreet1.setText(billing.getStreet1());
                    etStreet2.setText(billing.getStreet2());
                    etCity.setText(billing.getCity());
                    etProvince.setText(billing.getProvince());
                    etPostalCode.setText(billing.getPostalCode());
                    etCountry.setText(billing.getCountry());
                }

                if (service != null) {
                    cbSameAsBilling.setChecked(false);
                    layoutServiceAddressSection.setVisibility(View.VISIBLE);

                    etServiceStreet1.setText(service.getStreet1());
                    etServiceStreet2.setText(service.getStreet2());
                    etServiceCity.setText(service.getCity());
                    etServiceProvince.setText(service.getProvince());
                    etServicePostalCode.setText(service.getPostalCode());
                    etServiceCountry.setText(service.getCountry());
                } else {
                    cbSameAsBilling.setChecked(true);
                    layoutServiceAddressSection.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<CustomerAddressResponse>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CustomerAddressFormActivity.this,
                        "Unable to load address: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateBillingAddress() {
        if (!ValidationUtils.required(etStreet1, "Street 1 is required")) return false;
        if (!ValidationUtils.required(etCity, "City is required")) return false;
        if (!ValidationUtils.required(etProvince, "Province is required")) return false;
        if (!ValidationUtils.canadianPostalCode(etPostalCode)) return false;
        if (!ValidationUtils.required(etCountry, "Country is required")) return false;
        return true;
    }

    private boolean validateServiceAddress() {
        if (!ValidationUtils.required(etServiceStreet1, "Service street 1 is required")) return false;
        if (!ValidationUtils.required(etServiceCity, "Service city is required")) return false;
        if (!ValidationUtils.required(etServiceProvince, "Service province is required")) return false;
        if (!ValidationUtils.canadianPostalCode(etServicePostalCode)) return false;
        if (!ValidationUtils.required(etServiceCountry, "Service country is required")) return false;
        return true;
    }

    private void saveAddresses() {
        if (readOnly) {
            Toast.makeText(this, "Technicians can only view addresses", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateBillingAddress()) return;

        boolean sameAsBilling = cbSameAsBilling.isChecked();
        if (!sameAsBilling && !validateServiceAddress()) return;

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        SaveCustomerAddressRequest billingRequest = new SaveCustomerAddressRequest(
                "Billing",
                getTextValue(etStreet1),
                getTextValue(etStreet2),
                getTextValue(etCity),
                getTextValue(etProvince),
                getTextValue(etPostalCode).toUpperCase(),
                getTextValue(etCountry)
        );

        apiService.saveCustomerAddress(customerId, billingRequest).enqueue(new Callback<CustomerAddressResponse>() {
            @Override
            public void onResponse(Call<CustomerAddressResponse> call, Response<CustomerAddressResponse> response) {
                if (!response.isSuccessful()) {
                    showLoading(false);
                    Toast.makeText(CustomerAddressFormActivity.this,
                            "Billing address save failed. Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (sameAsBilling) {
                    deleteServiceIfNeeded(apiService);
                    return;
                }

                SaveCustomerAddressRequest serviceRequest = new SaveCustomerAddressRequest(
                        "Service",
                        getTextValue(etServiceStreet1),
                        getTextValue(etServiceStreet2),
                        getTextValue(etServiceCity),
                        getTextValue(etServiceProvince),
                        getTextValue(etServicePostalCode).toUpperCase(),
                        getTextValue(etServiceCountry)
                );

                apiService.saveCustomerAddress(customerId, serviceRequest).enqueue(new Callback<CustomerAddressResponse>() {
                    @Override
                    public void onResponse(Call<CustomerAddressResponse> call, Response<CustomerAddressResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful()) {
                            Toast.makeText(CustomerAddressFormActivity.this,
                                    "Billing and service addresses saved",
                                    Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(CustomerAddressFormActivity.this,
                                    "Service address save failed. Code: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CustomerAddressResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(CustomerAddressFormActivity.this,
                                "Unable to save service address",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<CustomerAddressResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CustomerAddressFormActivity.this,
                        "Unable to save billing address",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteServiceIfNeeded(ApiService apiService) {
        Call<Void> deleteCall = apiService.deleteCustomerAddressByType(customerId, "Service");
        deleteCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                Toast.makeText(CustomerAddressFormActivity.this,
                        "Address saved",
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CustomerAddressFormActivity.this,
                        "Address saved",
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private String getTextValue(EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void makeReadOnly() {
        etStreet1.setEnabled(false);
        etStreet2.setEnabled(false);
        etCity.setEnabled(false);
        etProvince.setEnabled(false);
        etPostalCode.setEnabled(false);
        etCountry.setEnabled(false);

        etServiceStreet1.setEnabled(false);
        etServiceStreet2.setEnabled(false);
        etServiceCity.setEnabled(false);
        etServiceProvince.setEnabled(false);
        etServicePostalCode.setEnabled(false);
        etServiceCountry.setEnabled(false);

        cbSameAsBilling.setEnabled(false);

        if (btnSave != null) {
            btnSave.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (btnSave != null) {
            btnSave.setEnabled(!isLoading && !readOnly);
        }

        if (btnBack != null) {
            btnBack.setEnabled(!isLoading);
        }
    }
}
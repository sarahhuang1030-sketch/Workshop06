package com.example.workshop06;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.MeResponse;
import com.example.workshop06.model.UpdateProfileRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends BaseActivity {

    private ImageView btnBack;
    private EditText etFirstName, etLastName, etEmail, etPhone;
    private Button btnSave;
    private SessionManager sessionManager;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnBack     = findViewById(R.id.btnBack);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName  = findViewById(R.id.etLastName);
        etEmail     = findViewById(R.id.etEmail);
        etPhone     = findViewById(R.id.etPhone);
        btnSave     = findViewById(R.id.btnSave);

        sessionManager = new SessionManager(this);
        apiService     = RetrofitClient.getApiService();
        token          = sessionManager.getToken();

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    // Form screen — no periodic refresh needed
    @Override
    protected void onRefresh() {}

    private void loadProfile() {
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        apiService.getMe("Bearer " + token).enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MeResponse me = response.body();
                    if (me.getFirstName() != null)      etFirstName.setText(me.getFirstName());
                    if (me.getLastName() != null)       etLastName.setText(me.getLastName());
                    if (me.getEmail() != null)          etEmail.setText(me.getEmail());
                    if (me.getResolvedPhone() != null)  etPhone.setText(me.getResolvedPhone());
                } else {
                    Toast.makeText(EditProfileActivity.this,
                            "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName  = etLastName.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("First name is required"); etFirstName.requestFocus(); return;
        }
        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Last name is required"); etLastName.requestFocus(); return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required"); etEmail.requestFocus(); return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email"); etEmail.requestFocus(); return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(firstName, lastName, email, phone);
        apiService.updateProfile("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this,
                            "Profile updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this,
                            "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
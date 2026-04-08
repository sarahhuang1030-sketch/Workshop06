package com.example.workshop06;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.LoginRequest;
import com.example.workshop06.model.LoginResponse;
import com.example.workshop06.util.ValidationUtils;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private EditText etPassword, etUsername;
    private Button btnLogin;
    private TextView tvGoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> doLogin());

        tvGoRegister.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Going to Register Page", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!ValidationUtils.username(etUsername)) return;
        if (!ValidationUtils.required(etPassword, "Password is required")) return;

        Retrofit retrofit = RetrofitClient.getRetrofitInstance(this);
        ApiService apiService = retrofit.create(ApiService.class);

        LoginRequest request = new LoginRequest(username, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    SessionManager sessionManager = new SessionManager(LoginActivity.this);
                    sessionManager.saveToken(loginResponse.getToken());

                    SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("jwt_token", loginResponse.getToken())
                            .putString("user_role", loginResponse.getRole())
                            .putString("first_name", loginResponse.getFirstName())
                            .putString("last_name", loginResponse.getLastName())
                            .putString("username", loginResponse.getUsername())
                            .apply();

                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    Intent intent;
                    if (Boolean.TRUE.equals(loginResponse.getMustChangePassword())) {
                        intent = new Intent(LoginActivity.this, ChangePasswordFirstLoginActivity.class);
                    } else if (loginResponse.getEmployeeId() != null) {
                        intent = new Intent(LoginActivity.this, EmployeeDashboardActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    }

                    startActivity(intent);
                    finish();

                } else if (response.code() == 403) {
                    String message = "Your profile is inactive now, so you can't access your dashboard.";

                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            JSONObject jsonObject = new JSONObject(errorJson);
                            if (jsonObject.has("message")) {
                                message = jsonObject.getString("message");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Access Denied")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show();

                } else {
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
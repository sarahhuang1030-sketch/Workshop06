package com.example.workshop06;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private Spinner spinnerAccountType;

    private EditText etFirstName, etLastName, etPhone, etEmail, etUsername;
    private EditText etPassword, etConfirmPassword;
    private EditText etStreet1, etStreet2, etCity, etProvince, etPostalCode, etCountry;

    private ImageButton btnTogglePassword, btnToggleConfirmPassword;
    private Button btnCreateAccount;
    private TextView tvSignIn;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupAccountTypeSpinner();
        setupPasswordToggle();
        setupConfirmPasswordToggle();
        setupButtons();
    }

    private void initViews() {
        spinnerAccountType = findViewById(R.id.spinnerAccountType);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        etStreet1 = findViewById(R.id.etStreet1);
        etStreet2 = findViewById(R.id.etStreet2);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etPostalCode = findViewById(R.id.etPostalCode);
        etCountry = findViewById(R.id.etCountry);

        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvSignIn = findViewById(R.id.tvSignIn);
    }

    private void setupAccountTypeSpinner() {
        String[] accountTypes = {"Individual", "Business", "Family"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                accountTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccountType.setAdapter(adapter);
    }

    private void setupPasswordToggle() {
        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;

            if (isPasswordVisible) {
                etPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                );
            } else {
                etPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                );
            }

            etPassword.setSelection(etPassword.getText().length());
        });
    }

    private void setupConfirmPasswordToggle() {
        btnToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;

            if (isConfirmPasswordVisible) {
                etConfirmPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                );
            } else {
                etConfirmPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                );
            }

            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });
    }

    private void setupButtons() {
        btnCreateAccount.setOnClickListener(v -> registerUser());

        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String accountType = spinnerAccountType.getSelectedItem().toString().trim();

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        String street1 = etStreet1.getText().toString().trim();
        String street2 = etStreet2.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String province = etProvince.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();
        String country = etCountry.getText().toString().trim();

        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (street1.isEmpty()) {
            etStreet1.setError("Street 1 is required");
            etStreet1.requestFocus();
            return;
        }

        if (city.isEmpty()) {
            etCity.setError("City is required");
            etCity.requestFocus();
            return;
        }

        if (province.isEmpty()) {
            etProvince.setError("Province is required");
            etProvince.requestFocus();
            return;
        }

        if (postalCode.isEmpty()) {
            etPostalCode.setError("Postal code is required");
            etPostalCode.requestFocus();
            return;
        }

        if (country.isEmpty()) {
            etCountry.setError("Country is required");
            etCountry.requestFocus();
            return;
        }

        Toast.makeText(
                this,
                "Account created for " + firstName + " (" + accountType + ")",
                Toast.LENGTH_LONG
        ).show();

        // Later you can save to database / send to API here

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
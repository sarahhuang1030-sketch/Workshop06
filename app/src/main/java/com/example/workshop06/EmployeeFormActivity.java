package com.example.workshop06;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.CreateEmployeeResponse;
import com.example.workshop06.model.EmployeeResponse;
import com.example.workshop06.model.LocationResponse;
import com.example.workshop06.model.SaveEmployeeRequest;
import com.example.workshop06.util.FormFormatUtils;
import com.example.workshop06.util.ValidationUtils;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeFormActivity extends AppCompatActivity {

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etSalary;
    private EditText etHireDate;

    private MaterialAutoCompleteTextView spinnerRole;
    private MaterialAutoCompleteTextView spinnerStatus;
    private MaterialAutoCompleteTextView spinnerManager;
    private MaterialAutoCompleteTextView spinnerLocation;

    private ImageView imgAvatarPreview;
    private TextView tvAvatarStatus;
    private Button btnTakePhoto;
    private Button btnChoosePhoto;

    private Button btnSave;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    private String mode = "add";
    private int employeeId = -1;

    private Uri selectedAvatarUri;
    private Uri cameraImageUri;

    private int selectedLocationId = -1;
    private final List<LocationResponse> locationList = new ArrayList<>();

    private final List<EmployeeResponse> managerList = new ArrayList<>();
    private int selectedManagerId = -1;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedAvatarUri = uri;
                    showSelectedAvatar(uri);
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCameraCapture();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    selectedAvatarUri = cameraImageUri;
                    showSelectedAvatar(cameraImageUri);
                } else {
                    Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_form);

        btnBack = findViewById(R.id.btnBack);

        initViews();
        setupDropdowns();
        setupFormatters();
        setupDatePicker();
        loadIntentData();
        loadLocations();
        loadManagers();
        setupButtons();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etSalary = findViewById(R.id.etSalary);
        etHireDate = findViewById(R.id.etHireDate);

        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerManager = findViewById(R.id.spinnerManager);
        spinnerLocation = findViewById(R.id.spinnerLocation);

        imgAvatarPreview = findViewById(R.id.imgAvatarPreview);
        tvAvatarStatus = findViewById(R.id.tvAvatarStatus);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);

        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupButtons() {
        btnTakePhoto.setOnClickListener(v -> openCamera());
        btnChoosePhoto.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveEmployee());
    }

    private void showSelectedAvatar(Uri uri) {
        Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
                .into(imgAvatarPreview);

        tvAvatarStatus.setText("Photo selected");
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCameraCapture();
        } else {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void launchCameraCapture() {
        try {
            File imageDir = new File(getCacheDir(), "images");
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }

            File imageFile = File.createTempFile("employee_avatar_", ".jpg", imageDir);

            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    imageFile
            );

            takePictureLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDropdowns() {
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"MANAGER", "SALES AGENT", "SERVICE TECHNICIAN"}
        );
        spinnerRole.setAdapter(roleAdapter);
        spinnerRole.setOnClickListener(v -> spinnerRole.showDropDown());
        spinnerRole.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) spinnerRole.showDropDown();
        });

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Active", "Inactive", "On Leave", "Suspended"}
        );
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setOnClickListener(v -> spinnerStatus.showDropDown());
        spinnerStatus.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) spinnerStatus.showDropDown();
        });

        ArrayAdapter<String> managerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        spinnerManager.setAdapter(managerAdapter);
        spinnerManager.setOnClickListener(v -> spinnerManager.showDropDown());
        spinnerManager.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) spinnerManager.showDropDown();
        });

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        spinnerLocation.setAdapter(locationAdapter);
        spinnerLocation.setOnClickListener(v -> spinnerLocation.showDropDown());
        spinnerLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) spinnerLocation.showDropDown();
        });
    }

    private void setupFormatters() {
        FormFormatUtils.attachCanadianPhoneFormatter(etPhone);
    }

    private void setupDatePicker() {
        View.OnClickListener listener = v -> showDatePicker();
        etHireDate.setOnClickListener(listener);

        TextInputLayout tilHireDate = findParentTextInputLayout(etHireDate);
        if (tilHireDate != null) {
            tilHireDate.setEndIconOnClickListener(v -> showDatePicker());
        }
    }

    private TextInputLayout findParentTextInputLayout(View view) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent instanceof TextInputLayout) {
                return (TextInputLayout) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    if (year != currentYear) {
                        Toast.makeText(this,
                                "Hire date must be within current year",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String formatted = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );

                    etHireDate.setText(formatted);
                    handleHireDateStatus(formatted);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void loadIntentData() {
        if (getIntent() == null) return;

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "add";

        if ("edit".equalsIgnoreCase(mode)) {
            employeeId = getIntent().getIntExtra("employeeId", -1);

            int primaryLocationId = getIntent().getIntExtra("primaryLocationId", Integer.MIN_VALUE);
            double salary = getIntent().getDoubleExtra("salary", Double.NaN);
            int managerId = getIntent().getIntExtra("managerId", Integer.MIN_VALUE);

            String firstName = getIntent().getStringExtra("firstName");
            String lastName = getIntent().getStringExtra("lastName");
            String email = getIntent().getStringExtra("email");
            String phone = getIntent().getStringExtra("phone");
            String role = getIntent().getStringExtra("role");
            String hireDate = getIntent().getStringExtra("hireDate");
            String status = getIntent().getStringExtra("status");

            if (primaryLocationId != Integer.MIN_VALUE) {
                selectedLocationId = primaryLocationId;
            }

            if (!Double.isNaN(salary)) {
                etSalary.setText(String.format(Locale.US, "%.2f", salary));
            }

            if (managerId != Integer.MIN_VALUE) {
                selectedManagerId = managerId;
            }

            if (firstName != null) etFirstName.setText(firstName);
            if (lastName != null) etLastName.setText(lastName);
            if (email != null) etEmail.setText(email);
            if (phone != null) etPhone.setText(phone);
            if (hireDate != null) etHireDate.setText(hireDate);

            setDropdownValue(spinnerRole, role);
            setDropdownValue(spinnerStatus, status);
        }
    }

    private void loadLocations() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getLocations().enqueue(new Callback<List<LocationResponse>>() {
            @Override
            public void onResponse(Call<List<LocationResponse>> call, Response<List<LocationResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(EmployeeFormActivity.this, "Failed to load locations", Toast.LENGTH_SHORT).show();
                    return;
                }

                locationList.clear();
                locationList.addAll(response.body());

                List<String> names = new ArrayList<>();
                for (LocationResponse loc : locationList) {
                    names.add(loc.getLocationName() != null ? loc.getLocationName() : "Location");
                }

                ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                        EmployeeFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        names
                );
                spinnerLocation.setAdapter(locationAdapter);

                spinnerLocation.setOnItemClickListener((parent, view, position, id) -> {
                    if (position >= 0 && position < locationList.size()) {
                        Integer idValue = locationList.get(position).getLocationId();
                        selectedLocationId = idValue != null ? idValue : -1;
                    }
                });

                setSelectedLocation();
            }

            @Override
            public void onFailure(Call<List<LocationResponse>> call, Throwable t) {
                Toast.makeText(EmployeeFormActivity.this, "Failed to load locations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSelectedLocation() {
        if (selectedLocationId <= 0) return;

        for (LocationResponse loc : locationList) {
            if (loc.getLocationId() != null && loc.getLocationId() == selectedLocationId) {
                spinnerLocation.setText(
                        loc.getLocationName() != null ? loc.getLocationName() : "",
                        false
                );
                break;
            }
        }
    }

    private void loadManagers() {
        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.getEmployees().enqueue(new Callback<List<EmployeeResponse>>() {
            @Override
            public void onResponse(Call<List<EmployeeResponse>> call, Response<List<EmployeeResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(EmployeeFormActivity.this, "Unable to load managers", Toast.LENGTH_SHORT).show();
                    return;
                }

                managerList.clear();
                List<String> labels = new ArrayList<>();
                labels.add("No Manager");

                for (EmployeeResponse employee : response.body()) {
                    if (employee != null
                            && employee.getRole() != null
                            && "MANAGER".equalsIgnoreCase(employee.getRole())) {

                        managerList.add(employee);

                        String fullName = (safe(employee.getFirstName()) + " " + safe(employee.getLastName())).trim();
                        if (fullName.isEmpty()) {
                            fullName = "Manager #" + employee.getEmployeeId();
                        }
                        labels.add(fullName);
                    }
                }

                ArrayAdapter<String> managerAdapter = new ArrayAdapter<>(
                        EmployeeFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        labels
                );
                spinnerManager.setAdapter(managerAdapter);

                spinnerManager.setOnItemClickListener((parent, view, position, id) -> {
                    if (position == 0) {
                        selectedManagerId = -1;
                    } else {
                        int index = position - 1;
                        if (index >= 0 && index < managerList.size()) {
                            Integer idValue = managerList.get(index).getEmployeeId();
                            selectedManagerId = idValue != null ? idValue : -1;
                        }
                    }
                });

                if (selectedManagerId > 0) {
                    setManagerSelection(selectedManagerId);
                } else {
                    spinnerManager.setText("No Manager", false);
                }
            }

            @Override
            public void onFailure(Call<List<EmployeeResponse>> call, Throwable t) {
                Toast.makeText(EmployeeFormActivity.this, "Unable to load managers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setManagerSelection(int managerId) {
        for (EmployeeResponse manager : managerList) {
            if (manager.getEmployeeId() != null && manager.getEmployeeId() == managerId) {
                String fullName = (safe(manager.getFirstName()) + " " + safe(manager.getLastName())).trim();
                if (fullName.isEmpty()) {
                    fullName = "Manager #" + managerId;
                }
                spinnerManager.setText(fullName, false);
                break;
            }
        }
    }

    private void setDropdownValue(MaterialAutoCompleteTextView dropdown, String value) {
        if (value == null || value.trim().isEmpty()) return;
        dropdown.setText(value, false);
    }

    private void saveEmployee() {
        if (!ValidationUtils.required(etFirstName, "First name is required")) return;
        if (!ValidationUtils.required(etLastName, "Last name is required")) return;
        if (!ValidationUtils.email(etEmail)) return;
        if (!ValidationUtils.phone(etPhone)) return;
        if (!ValidationUtils.positiveNumber(etSalary, "Enter a valid salary")) return;

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String role = spinnerRole.getText() != null ? spinnerRole.getText().toString().trim() : "";
        String salaryText = etSalary.getText().toString().trim();
        String hireDate = etHireDate.getText().toString().trim();
        String status = spinnerStatus.getText() != null ? spinnerStatus.getText().toString().trim() : "";

        if (selectedAvatarUri == null) {
            Toast.makeText(this, "Please take or choose a profile photo first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(role)) {
            spinnerRole.setError("Role is required");
            spinnerRole.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(status)) {
            spinnerStatus.setError("Status is required");
            spinnerStatus.requestFocus();
            return;
        }

        if (!TextUtils.isEmpty(hireDate)) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
                sdf.setLenient(false);

                java.util.Date selectedDate = sdf.parse(hireDate);
                if (selectedDate == null) {
                    Toast.makeText(this, "Invalid hire date", Toast.LENGTH_SHORT).show();
                    return;
                }

                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(selectedDate);

                Calendar todayCal = Calendar.getInstance();
                int currentYear = todayCal.get(Calendar.YEAR);

                if (selectedCal.get(Calendar.YEAR) != currentYear) {
                    Toast.makeText(this,
                            "Hire date must be within current year",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isFutureDate(hireDate)) {
                    status = "Inactive";
                }

            } catch (Exception e) {
                Toast.makeText(this, "Invalid hire date", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Integer primaryLocationId = selectedLocationId > 0 ? selectedLocationId : null;
        Double salary = TextUtils.isEmpty(salaryText) ? null : Double.parseDouble(salaryText);
        Integer managerId = selectedManagerId > 0 ? selectedManagerId : null;

        SaveEmployeeRequest request = new SaveEmployeeRequest(
                primaryLocationId,
                firstName,
                lastName,
                email,
                phone,
                role,
                salary,
                TextUtils.isEmpty(hireDate) ? null : hireDate,
                status,
                managerId
        );

        showLoading(true);

        ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        if ("edit".equalsIgnoreCase(mode) && employeeId > 0) {
            apiService.updateEmployee(employeeId, request).enqueue(new Callback<EmployeeResponse>() {
                @Override
                public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(EmployeeFormActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(EmployeeFormActivity.this,
                                "Update failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(EmployeeFormActivity.this,
                            "Unable to update employee",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            apiService.createEmployee(request).enqueue(new Callback<CreateEmployeeResponse>() {
                @Override
                public void onResponse(Call<CreateEmployeeResponse> call, Response<CreateEmployeeResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        CreateEmployeeResponse created = response.body();

                        int newEmployeeId = created.getEmployeeId();

                        uploadAvatar(newEmployeeId, () -> {
                            new AlertDialog.Builder(EmployeeFormActivity.this)
                                    .setTitle("Employee Created")
                                    .setMessage("Username: " + created.getUsername()
                                            + "\nTemporary Password: " + created.getTempPassword()
                                            + "\n\nTap COPY to save credentials.")
                                    .setCancelable(false)
                                    .setPositiveButton("Copy", (dialog, which) -> {
                                        String textToCopy = "Username: " + created.getUsername()
                                                + "\nTemporary Password: " + created.getTempPassword();

                                        android.content.ClipboardManager clipboard =
                                                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                                        android.content.ClipData clip =
                                                android.content.ClipData.newPlainText("Employee Credentials", textToCopy);

                                        clipboard.setPrimaryClip(clip);

                                        Toast.makeText(EmployeeFormActivity.this,
                                                "Copied to clipboard",
                                                Toast.LENGTH_SHORT).show();

                                        setResult(RESULT_OK);
                                        finish();
                                    })
                                    .show();
                        });

                    } else {
                        showLoading(false);
                        Toast.makeText(EmployeeFormActivity.this,
                                "Create failed. Code: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<CreateEmployeeResponse> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(EmployeeFormActivity.this,
                            "Unable to create employee",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void uploadAvatar(int employeeId, Runnable onSuccess) {
        if (selectedAvatarUri == null) {
            onSuccess.run();
            return;
        }

        try {
            ApiService apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

            InputStream inputStream = getContentResolver().openInputStream(selectedAvatarUri);
            if (inputStream == null) {
                onSuccess.run();
                return;
            }

            byte[] bytes = readBytes(inputStream);

            RequestBody requestFile = RequestBody.create(bytes, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestFile);

            apiService.uploadEmployeeAvatar(employeeId, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    onSuccess.run();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(EmployeeFormActivity.this,
                            "Avatar upload failed",
                            Toast.LENGTH_SHORT).show();
                    onSuccess.run();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            onSuccess.run();
        }
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
        btnTakePhoto.setEnabled(!isLoading);
        btnChoosePhoto.setEnabled(!isLoading);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void handleHireDateStatus(String hireDateStr) {
        if (isFutureDate(hireDateStr)) {
            spinnerStatus.setText("Inactive", false);
            spinnerStatus.setEnabled(false);

            Toast.makeText(this,
                    "Future hire date: employee stays inactive until that date",
                    Toast.LENGTH_SHORT).show();
        } else {
            spinnerStatus.setEnabled(true);

            String currentStatus = spinnerStatus.getText() != null
                    ? spinnerStatus.getText().toString().trim()
                    : "";

            if (currentStatus.isEmpty() || "Inactive".equalsIgnoreCase(currentStatus)) {
                spinnerStatus.setText("Active", false);
            }
        }
    }

    private boolean isFutureDate(String dateStr) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);

            java.util.Date selectedDate = sdf.parse(dateStr);
            if (selectedDate == null) return false;

            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);
            selectedCal.set(Calendar.HOUR_OF_DAY, 0);
            selectedCal.set(Calendar.MINUTE, 0);
            selectedCal.set(Calendar.SECOND, 0);
            selectedCal.set(Calendar.MILLISECOND, 0);

            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);

            return selectedCal.after(todayCal);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
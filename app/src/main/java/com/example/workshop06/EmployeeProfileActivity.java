package com.example.workshop06;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.workshop06.api.ApiService;
import com.example.workshop06.api.RetrofitClient;
import com.example.workshop06.model.MeResponse;
import com.example.workshop06.model.ProfileImageUploadResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.view.View;

public class EmployeeProfileActivity extends BaseActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080";
    private static final String DEFAULT_AVATAR_PATH = "/uploads/avatars/default.jpg";

    private BottomNavigationView bottomNavigation;
    private TextView tvFirstName;
    private TextView tvUsername;
    private TextView tvRole;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvEmployeeId;
    private TextView tvLocation;
    private ImageView imgAvatar;
    private Button btnLogout;
    private Button btnEditProfile;

    private Uri cameraImageUri;
    private View layoutPersonalInfo;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadAvatar(uri);
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
                    uploadAvatar(cameraImageUri);
                } else {
                    Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            fetchProfileFromBackend();
                        }
                    }
            );


    @Override
    protected void onRefresh() {
        fetchProfileFromBackend();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_profile);

        BottomNavHelper.setup(this, R.id.nav_profile);
        layoutPersonalInfo = findViewById(R.id.layoutPersonalInfo);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvUsername = findViewById(R.id.tvUsername);
        tvRole = findViewById(R.id.tvRole);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmployeeId = findViewById(R.id.tvEmployeeId);
        tvLocation = findViewById(R.id.tvLocation);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        layoutPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeProfileActivity.this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });
        loadProfile();
        fetchProfileFromBackend();
        setupAvatarActions();
        setupNav();
        setupLogout();
    }

    private void setupAvatarActions() {
        imgAvatar.setOnClickListener(v -> showAvatarOptions());
        btnEditProfile.setOnClickListener(v -> showAvatarOptions());
    }

    private void showAvatarOptions() {
        String[] options = {"Take photo", "Upload photo", "Remove photo", "Cancel"};

        new AlertDialog.Builder(this)
                .setTitle("Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openImagePicker();
                    } else if (which == 2) {
                        deleteAvatar();
                    } else {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }

    private void loadProfile() {
        SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);

        String firstName = prefs.getString("firstName", "Employee");
        String username = prefs.getString("username", "N/A");
        String role = prefs.getString("role", "EMPLOYEE");
        String email = prefs.getString("email", "Not available");
        String avatarUrl = prefs.getString("avatarUrl", DEFAULT_AVATAR_PATH);

        tvFirstName.setText(firstName);
        tvUsername.setText(username);
        tvRole.setText(role);
        tvEmail.setText(email);

        loadAvatar(avatarUrl);
    }

    private void fetchProfileFromBackend() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "No token found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient
                .getRetrofitInstance(this)
                .create(ApiService.class);

        apiService.getMe("Bearer " + token).enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MeResponse me = response.body();

                    String fullName = me.getDisplayName();
                    tvFirstName.setText(fullName != null && !fullName.trim().isEmpty() ? fullName : "Employee");
                    tvUsername.setText(me.getUsername() != null ? me.getUsername() : "N/A");
                    tvRole.setText(me.getRole() != null ? me.getRole() : "EMPLOYEE");
                    tvEmail.setText(me.getEmail() != null ? me.getEmail() : "Not available");
                    tvPhone.setText(me.getResolvedPhone() != null ? me.getResolvedPhone() : "Not available");
                    tvEmployeeId.setText(me.getEmployeeId() != null ? String.valueOf(me.getEmployeeId()) : "Not available");
                    tvLocation.setText(me.getLocationName() != null ? me.getLocationName() : "Not available");

                    String avatarUrl = (me.getAvatarUrl() != null && !me.getAvatarUrl().trim().isEmpty())
                            ? me.getAvatarUrl()
                            : DEFAULT_AVATAR_PATH;

                    loadAvatar(avatarUrl);

                    SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("firstName",
                                    fullName != null && !fullName.trim().isEmpty() ? fullName : "Employee")
                            .putString("username", me.getUsername() != null ? me.getUsername() : "N/A")
                            .putString("role", me.getRole() != null ? me.getRole() : "EMPLOYEE")
                            .putString("email", me.getEmail() != null ? me.getEmail() : "Not available")
                            .putString("avatarUrl", avatarUrl)
                            .apply();
                } else {
                    Toast.makeText(EmployeeProfileActivity.this,
                            "Failed to load profile",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                Toast.makeText(EmployeeProfileActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAvatar(String avatarUrl) {

        String BASE_URL = "http://10.0.2.2:8080";
        String DEFAULT_AVATAR = "/uploads/avatars/default.jpg";

        String finalUrl;

        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            finalUrl = BASE_URL + DEFAULT_AVATAR;
        } else if (avatarUrl.startsWith("http")) {   // ✅ THIS FIX
            finalUrl = avatarUrl;
        } else {
            finalUrl = BASE_URL + avatarUrl;
        }

        Glide.with(this)
                .load(finalUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
                .into(imgAvatar);
    }

    private void uploadAvatar(Uri uri) {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "No token found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(this, "Unable to read image", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] bytes = readBytes(inputStream);

            RequestBody requestFile = RequestBody.create(bytes, MediaType.parse("image/*"));
            MultipartBody.Part avatarPart = MultipartBody.Part.createFormData(
                    "avatar",
                    "avatar.jpg",
                    requestFile
            );

            ApiService apiService = RetrofitClient
                    .getRetrofitInstance(this)
                    .create(ApiService.class);

            apiService.uploadAvatar("Bearer " + token, avatarPart)
                    .enqueue(new Callback<ProfileImageUploadResponse>() {
                        @Override
                        public void onResponse(Call<ProfileImageUploadResponse> call,
                                               Response<ProfileImageUploadResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String avatarUrl = response.body().getAvatarUrl();
                                if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                                    avatarUrl = DEFAULT_AVATAR_PATH;
                                }

                                loadAvatar(avatarUrl);

                                SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
                                prefs.edit().putString("avatarUrl", avatarUrl).apply();

                                Toast.makeText(EmployeeProfileActivity.this,
                                        "Profile photo updated",
                                        Toast.LENGTH_SHORT).show();

                                fetchProfileFromBackend();
                            } else {
                                Toast.makeText(EmployeeProfileActivity.this,
                                        "Upload failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ProfileImageUploadResponse> call, Throwable t) {
                            Toast.makeText(EmployeeProfileActivity.this,
                                    "Upload error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(this, "Unable to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAvatar() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "No token found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient
                .getRetrofitInstance(this)
                .create(ApiService.class);

        apiService.deleteAvatar("Bearer " + token).enqueue(new Callback<Map<String, Boolean>>() {
            @Override
            public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> response) {
                if (response.isSuccessful()) {
                    loadAvatar(DEFAULT_AVATAR_PATH);

                    SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
                    prefs.edit().putString("avatarUrl", DEFAULT_AVATAR_PATH).apply();

                    Toast.makeText(EmployeeProfileActivity.this,
                            "Profile photo removed",
                            Toast.LENGTH_SHORT).show();

                    fetchProfileFromBackend();
                } else {
                    Toast.makeText(EmployeeProfileActivity.this,
                            "Failed to delete photo",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                Toast.makeText(EmployeeProfileActivity.this,
                        "Delete error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
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

    private void setupNav() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, EmployeeDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_customers) {
                startActivity(new Intent(this, CustomerListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            if (id == R.id.nav_plans) {
                startActivity(new Intent(this, PlanListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return id == R.id.nav_profile;
        });
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("teleconnect_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            SessionManager sessionManager = new SessionManager(EmployeeProfileActivity.this);
            sessionManager.clearToken();

            Intent intent = new Intent(EmployeeProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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

            File imageFile = File.createTempFile("avatar_capture_", ".jpg", imageDir);

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

}
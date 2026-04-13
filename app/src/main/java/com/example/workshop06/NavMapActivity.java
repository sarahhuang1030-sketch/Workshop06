package com.example.workshop06;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NavMapActivity extends BaseActivity implements OnMapReadyCallback {

    @Override
    protected void onRefresh() {}

    private static final LatLng DEFAULT_LOCATION = new LatLng(51.0447, -114.0719); // Calgary

    private View infoOverlay;
    private FloatingActionButton fabInfo;
    private FloatingActionButton fabMyLocation;
    private MaterialButton btnCloseOverlay;
    private BottomNavigationView bottomNavigation;
    private ProgressBar progressBar;

    private GoogleMap googleMap;

    // You can pass these from another screen using Intent extras
    // Example:
    // intent.putExtra("address_line", "123 Main St SW, Calgary, AB");
    // intent.putExtra("job_title", "Service Appointment #7");
    private String addressLine;
    private String markerTitle;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableMyLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_map);

        infoOverlay = findViewById(R.id.infoOverlay);
        fabInfo = findViewById(R.id.fabInfo);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        progressBar = findViewById(R.id.progressBar);
//        btnCloseOverlay = findViewById(R.id.btnCloseOverlay);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        addressLine = getIntent().getStringExtra("address_line");
        markerTitle = getIntent().getStringExtra("job_title");
        if (markerTitle == null || markerTitle.trim().isEmpty()) {
            markerTitle = "Job Location";
        }

        setupOverlay();
        setupBottomNavigation();
        setupActions();
        setupMap();
    }

    private void setupMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            showLoading(true);
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map failed to load", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        showLoading(false);

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        enableMyLocation();

        if (addressLine != null && !addressLine.trim().isEmpty()) {
            geocodeAndPinAddress(addressLine, markerTitle);
        } else {
            // fallback when no address was passed in
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f));
            Toast.makeText(this, "No appointment address found", Toast.LENGTH_SHORT).show();
        }

        googleMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return false;
        });
    }

    private void geocodeAndPinAddress(String fullAddress, String title) {
        if (googleMap == null) return;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> results = geocoder.getFromLocationName(fullAddress, 1);

            if (results != null && !results.isEmpty()) {
                Address result = results.get(0);
                LatLng latLng = new LatLng(result.getLatitude(), result.getLongitude());

                googleMap.clear();

                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .snippet(fullAddress)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f));
                Toast.makeText(this, "Could not find this address on map", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f));
            Toast.makeText(this, "Failed to load address on map", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void enableMyLocation() {
        if (googleMap == null) return;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void setupOverlay() {
        if (infoOverlay != null) {
            infoOverlay.setVisibility(View.GONE);
            infoOverlay.setOnClickListener(v -> infoOverlay.setVisibility(View.GONE));
        }

        if (fabInfo != null) {
            fabInfo.setOnClickListener(v -> {
                if (infoOverlay != null) {
                    infoOverlay.setVisibility(View.VISIBLE);
                }
            });
        }

        if (btnCloseOverlay != null) {
            btnCloseOverlay.setOnClickListener(v -> {
                if (infoOverlay != null) {
                    infoOverlay.setVisibility(View.GONE);
                }
            });
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;

        bottomNavigation.setSelectedItemId(R.id.nav_maps);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ServiceAppointmentListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_maps) {
                return true;
            } else if (id == R.id.nav_customers) {
                startActivity(new Intent(this, CustomerListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, EmployeeProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void setupActions() {
        if (fabMyLocation != null) {
            fabMyLocation.setOnClickListener(v -> {
                if (googleMap == null) return;

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            });
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
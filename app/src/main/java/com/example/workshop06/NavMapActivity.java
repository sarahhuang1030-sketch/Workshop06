package com.example.workshop06;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NavMapActivity extends BaseActivity implements OnMapReadyCallback {

    @Override
    protected void onRefresh() {}

    private static final LatLng DEFAULT_LOCATION = new LatLng(51.0447, -114.0719);
    private static final String ROUTES_API_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";

    private View infoOverlay;
    private FloatingActionButton fabInfo;
    private MaterialButton btnCloseOverlay;
    private BottomNavigationView bottomNavigation;
    private ProgressBar progressBar;

    private TextView tvOverlayAppointmentTitle;
    private TextView tvOverlayTechnician;
    private TextView tvOverlayLocationType;
    private TextView tvOverlayAddress;
    private TextView tvOverlayScheduledStart;
    private TextView tvOverlayScheduledEnd;
    private TextView tvOverlayStatus;
    private TextView tvOverlayRequestType;
    private TextView tvOverlayRequestDescription;
    private TextView tvOverlayNotes;
    private TextView tvOverlayCustomerName;
    private ImageButton btnEditOverlayAppointment;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private Marker destinationMarker;
    private Marker carMarker;
    private Polyline routePolyline;

    private LatLng currentLatLng;
    private LatLng destinationLatLng;

    private String customerName;
    private String addressLine;
    private String markerTitle;

    private int appointmentId = -1;
    private int requestId = -1;
    private int technicianUserId = Integer.MIN_VALUE;
    private int addressId = Integer.MIN_VALUE;
    private int locationId = Integer.MIN_VALUE;

    private String technicianName;
    private String locationType;
    private String scheduledStart;
    private String scheduledEnd;
    private String appointmentStatus;
    private String appointmentNotes;
    private String requestType;
    private String requestDescription;

    private boolean technicianLimitedEdit = false;
    private boolean firstCameraFitDone = false;

    private boolean routeRequestInFlight = false;
    private LatLng lastRouteOrigin;
    private LatLng lastRouteDestination;

    private final OkHttpClient httpClient = new OkHttpClient();

    private final ActivityResultLauncher<Intent> editAppointmentLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setResult(RESULT_OK);
                    finish();
                }
            });

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableMyLocation();
                    fetchLastKnownLocation();
                    startLiveLocationUpdates();
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
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        tvOverlayAppointmentTitle = findViewById(R.id.tvOverlayAppointmentTitle);
        tvOverlayTechnician = findViewById(R.id.tvOverlayTechnician);
        tvOverlayLocationType = findViewById(R.id.tvOverlayLocationType);
        tvOverlayAddress = findViewById(R.id.tvOverlayAddress);
        tvOverlayScheduledStart = findViewById(R.id.tvOverlayScheduledStart);
        tvOverlayScheduledEnd = findViewById(R.id.tvOverlayScheduledEnd);
        tvOverlayStatus = findViewById(R.id.tvOverlayStatus);
        tvOverlayRequestType = findViewById(R.id.tvOverlayRequestType);
        tvOverlayRequestDescription = findViewById(R.id.tvOverlayRequestDescription);
        tvOverlayNotes = findViewById(R.id.tvOverlayNotes);
        tvOverlayCustomerName = findViewById(R.id.tvOverlayCustomerName);
        btnEditOverlayAppointment = findViewById(R.id.btnEditOverlayAppointment);
        btnCloseOverlay = findViewById(R.id.btnCloseOverlay);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        readIntentData();
        bindOverlayData();

        buildLocationRequest();
        buildLocationCallback();

        setupOverlay();
        setupBottomNavigation();
        setupMap();
    }

    private void readIntentData() {
        Intent intent = getIntent();

        customerName = intent.getStringExtra("customerName");
        requestType = intent.getStringExtra("requestType");
        requestDescription = intent.getStringExtra("requestDescription");
        addressLine = intent.getStringExtra("address_line");
        markerTitle = intent.getStringExtra("job_title");

        if (markerTitle == null || markerTitle.trim().isEmpty()) {
            markerTitle = "Job Location";
        }

        appointmentId = intent.getIntExtra("appointmentId", -1);
        requestId = intent.getIntExtra("requestId", -1);
        technicianUserId = intent.getIntExtra("technicianUserId", Integer.MIN_VALUE);
        addressId = intent.getIntExtra("addressId", Integer.MIN_VALUE);
        locationId = intent.getIntExtra("locationId", Integer.MIN_VALUE);

        technicianName = intent.getStringExtra("technicianName");
        locationType = intent.getStringExtra("locationType");
        scheduledStart = intent.getStringExtra("scheduledStart");
        scheduledEnd = intent.getStringExtra("scheduledEnd");
        appointmentStatus = intent.getStringExtra("status");
        appointmentNotes = intent.getStringExtra("notes");

        technicianLimitedEdit = intent.getBooleanExtra("technicianLimitedEdit", false);
    }

    private void bindOverlayData() {
        if (tvOverlayAppointmentTitle != null) {
            tvOverlayAppointmentTitle.setText(
                    appointmentId > 0 ? "Appointment #" + appointmentId : "Appointment"
            );
        }

        if (tvOverlayTechnician != null) tvOverlayTechnician.setText(valueOrDash(technicianName));
        if (tvOverlayLocationType != null) tvOverlayLocationType.setText(valueOrDash(locationType));
        if (tvOverlayAddress != null) tvOverlayAddress.setText(valueOrDash(addressLine));
        if (tvOverlayScheduledStart != null) tvOverlayScheduledStart.setText(valueOrDash(scheduledStart));
        if (tvOverlayScheduledEnd != null) tvOverlayScheduledEnd.setText(valueOrDash(scheduledEnd));
        if (tvOverlayStatus != null) tvOverlayStatus.setText(valueOrDash(appointmentStatus));
        if (tvOverlayRequestType != null) tvOverlayRequestType.setText(valueOrDash(requestType));
        if (tvOverlayRequestDescription != null) tvOverlayRequestDescription.setText(valueOrDash(requestDescription));
        if (tvOverlayNotes != null) tvOverlayNotes.setText(valueOrDash(appointmentNotes));
        if (tvOverlayCustomerName != null) tvOverlayCustomerName.setText(valueOrDash(customerName));
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
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

        if (addressLine != null && !addressLine.trim().isEmpty()) {
            geocodeAndPinAddress(addressLine, markerTitle);
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f));
            Toast.makeText(this, "No appointment address found", Toast.LENGTH_SHORT).show();
        }

        enableMyLocation();
        fetchLastKnownLocation();
        startLiveLocationUpdates();
    }

    private void geocodeAndPinAddress(String fullAddress, String title) {
        if (googleMap == null || fullAddress == null || fullAddress.trim().isEmpty()) {
            return;
        }

        showLoading(true);

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(NavMapActivity.this, Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(fullAddress, 1);

                runOnUiThread(() -> {
                    showLoading(false);

                    if (results != null && !results.isEmpty()) {
                        Address result = results.get(0);
                        LatLng latLng = new LatLng(result.getLatitude(), result.getLongitude());
                        showDestinationOnMap(latLng, title, fullAddress);
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f));
                        Toast.makeText(NavMapActivity.this,
                                "Could not find this address on map",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f));
                    Toast.makeText(NavMapActivity.this,
                            "Could not find this address on map",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showDestinationOnMap(LatLng latLng, String title, String fullAddress) {
        destinationLatLng = latLng;

        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        destinationMarker = googleMap.addMarker(new MarkerOptions()
                .position(destinationLatLng)
                .title(title)
                .snippet(fullAddress)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

        fitMapToBothLocations();
        maybeDrawRoute();
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1500)
                .build();
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) return;

                currentLatLng = new LatLng(
                        locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude()
                );

                updateCarMarker(currentLatLng);

                if (destinationLatLng == null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                } else if (!firstCameraFitDone) {
                    fitMapToBothLocations();
                }

                maybeDrawRoute();
            }
        };
    }

    private void fitMapToBothLocations() {
        if (googleMap == null) return;

        if (currentLatLng != null && destinationLatLng != null) {
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(currentLatLng)
                    .include(destinationLatLng)
                    .build();

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 180));
            firstCameraFitDone = true;
            return;
        }

        if (!firstCameraFitDone && destinationLatLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15f));
            firstCameraFitDone = true;
        }
    }

    private void updateCarMarker(LatLng position) {
        if (googleMap == null) return;

        if (carMarker == null) {
            carMarker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car_marker)));
        } else {
            carMarker.setPosition(position);
        }
    }

    private void maybeDrawRoute() {
        if (googleMap == null || currentLatLng == null || destinationLatLng == null) {
            return;
        }

        if (routeRequestInFlight) {
            return;
        }

        if (lastRouteOrigin != null && lastRouteDestination != null) {
            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    lastRouteOrigin.latitude, lastRouteOrigin.longitude,
                    currentLatLng.latitude, currentLatLng.longitude,
                    results
            );

            if (results[0] < 25f &&
                    Math.abs(lastRouteDestination.latitude - destinationLatLng.latitude) < 0.00001 &&
                    Math.abs(lastRouteDestination.longitude - destinationLatLng.longitude) < 0.00001) {
                return;
            }
        }

        lastRouteOrigin = currentLatLng;
        lastRouteDestination = destinationLatLng;
        requestRoute(currentLatLng, destinationLatLng);
    }

    private void requestRoute(LatLng origin, LatLng destination) {
        routeRequestInFlight = true;

        try {
            JSONObject payload = new JSONObject();

            JSONObject originObj = new JSONObject();
            JSONObject originLocation = new JSONObject();
            JSONObject originLatLng = new JSONObject();
            originLatLng.put("latitude", origin.latitude);
            originLatLng.put("longitude", origin.longitude);
            originLocation.put("latLng", originLatLng);
            originObj.put("location", originLocation);

            JSONObject destinationObj = new JSONObject();
            JSONObject destinationLocation = new JSONObject();
            JSONObject destinationLatLngObj = new JSONObject();
            destinationLatLngObj.put("latitude", destination.latitude);
            destinationLatLngObj.put("longitude", destination.longitude);
            destinationLocation.put("latLng", destinationLatLngObj);
            destinationObj.put("location", destinationLocation);

            payload.put("origin", originObj);
            payload.put("destination", destinationObj);
            payload.put("travelMode", "DRIVE");
            payload.put("routingPreference", "TRAFFIC_AWARE");
            payload.put("computeAlternativeRoutes", false);
            payload.put("languageCode", "en-US");
            payload.put("units", "METRIC");

            RequestBody body = RequestBody.create(
                    payload.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(ROUTES_API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Goog-Api-Key", "AIzaSyD5Tuu3y5QdtwRY02WzIjngDi1ZpnUQc_8")
                    .addHeader("X-Goog-FieldMask", "routes.polyline.encodedPolyline")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    routeRequestInFlight = false;
                    runOnUiThread(() ->
                            Toast.makeText(NavMapActivity.this, "Failed to load route", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    routeRequestInFlight = false;

                    if (!response.isSuccessful() || response.body() == null) {
                        runOnUiThread(() ->
                                Toast.makeText(NavMapActivity.this, "Route request failed", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    try {
                        String json = response.body().string();
                        JSONObject root = new JSONObject(json);
                        JSONArray routes = root.optJSONArray("routes");

                        if (routes == null || routes.length() == 0) {
                            runOnUiThread(() ->
                                    Toast.makeText(NavMapActivity.this, "No route found", Toast.LENGTH_SHORT).show()
                            );
                            return;
                        }

                        JSONObject firstRoute = routes.getJSONObject(0);
                        JSONObject polyline = firstRoute.getJSONObject("polyline");
                        String encoded = polyline.getString("encodedPolyline");

                        List<LatLng> points = decodePolyline(encoded);

                        runOnUiThread(() -> drawRoute(points));

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(NavMapActivity.this, "Failed to parse route", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            routeRequestInFlight = false;
            Toast.makeText(this, "Unable to build route request", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawRoute(List<LatLng> points) {
        if (googleMap == null || points == null || points.isEmpty()) return;

        if (routePolyline != null) {
            routePolyline.remove();
        }

        routePolyline = googleMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(12f)
                .geodesic(true)
                .jointType(JointType.ROUND));

        fitMapToBothLocations();
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lat += dlat;

            shift = 0;
            result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }

        return poly;
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

    private void fetchLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) return;

            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            updateCarMarker(currentLatLng);
            fitMapToBothLocations();
            maybeDrawRoute();
        });
    }

    private void startLiveLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private void stopLiveLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLiveLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleMap != null) {
            startLiveLocationUpdates();
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

        View overlayCardAnchor = findViewById(R.id.tvOverlayAppointmentTitle);
        if (overlayCardAnchor != null && overlayCardAnchor.getParent() != null) {
            View parent = (View) ((View) overlayCardAnchor.getParent()).getParent();
            if (parent != null) {
                parent.setOnClickListener(v -> {
                    // keep overlay open when tapping inside
                });
            }
        }

        if (btnEditOverlayAppointment != null) {
            btnEditOverlayAppointment.setOnClickListener(v -> openEditAppointment());
        }
    }

    private void openEditAppointment() {
        if (appointmentId <= 0 || requestId <= 0) {
            Toast.makeText(this, "Appointment information is incomplete", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ServiceAppointmentFormActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("requestId", requestId);
        intent.putExtra("appointmentId", appointmentId);
        intent.putExtra("technicianUserId", technicianUserId);
        intent.putExtra("addressId", addressId);
        intent.putExtra("locationId", locationId);
        intent.putExtra("locationType", locationType);
        intent.putExtra("scheduledStart", scheduledStart);
        intent.putExtra("scheduledEnd", scheduledEnd);
        intent.putExtra("status", appointmentStatus);
        intent.putExtra("notes", appointmentNotes);
        intent.putExtra("technicianName", technicianName);
        intent.putExtra("addressText", addressLine);
        intent.putExtra("technicianLimitedEdit", technicianLimitedEdit);

        editAppointmentLauncher.launch(intent);
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;

        bottomNavigation.setSelectedItemId(R.id.nav_maps);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, EmployeeDashboardActivity.class));
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

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
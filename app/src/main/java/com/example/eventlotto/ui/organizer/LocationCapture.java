package com.example.eventlotto.ui.organizer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.GeoPoint;

public class LocationCapture {
    private static final String TAG = "LocationCapture";
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public interface LocationCallback {
        void onLocationReceived(GeoPoint location);
        void onLocationFailed(String error);
    }

    public LocationCapture(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Check if location permission is granted
     * CRITICAL: This must check Android's actual permission status
     */
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get the current location
     * This will only work if permission has been granted
     */
    public void getCurrentLocation(LocationCallback callback) {
        // Check permission first
        if (!hasLocationPermission()) {
            callback.onLocationFailed("Location permission not granted");
            return;
        }

        try {
            // Try to get last known location first (faster)
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Use last known location
                            GeoPoint geoPoint = new GeoPoint(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                            callback.onLocationReceived(geoPoint);
                        } else {
                            // Last location is null, request current location
                            requestCurrentLocation(callback);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get last location", e);
                        // Try requesting current location
                        requestCurrentLocation(callback);
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception getting location", e);
            callback.onLocationFailed("Security exception: " + e.getMessage());
        }
    }

    /**
     * Request current location update
     */
    private void requestCurrentLocation(LocationCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationFailed("Location permission not granted");
            return;
        }

        try {
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    10000) // 10 seconds
                    .setMinUpdateIntervalMillis(5000) // 5 seconds
                    .setMaxUpdates(1) // Only need one update
                    .build();

            com.google.android.gms.location.LocationCallback locationCallback =
                    new com.google.android.gms.location.LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null) {
                                callback.onLocationFailed("Location result is null");
                                return;
                            }

                            Location location = locationResult.getLastLocation();
                            if (location != null) {
                                GeoPoint geoPoint = new GeoPoint(
                                        location.getLatitude(),
                                        location.getLongitude()
                                );
                                callback.onLocationReceived(geoPoint);
                            } else {
                                callback.onLocationFailed("Could not get current location");
                            }

                            // Remove updates after receiving location
                            fusedLocationClient.removeLocationUpdates(this);
                        }
                    };

            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );

            // Timeout after 15 seconds
            new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                fusedLocationClient.removeLocationUpdates(locationCallback);
                callback.onLocationFailed("Location request timed out");
            }, 15000);

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception requesting location", e);
            callback.onLocationFailed("Security exception: " + e.getMessage());
        }
    }
}
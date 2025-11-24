package com.example.eventlotto.ui.organizer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.firestore.GeoPoint;

/**
 * Helper class for capturing user location when they join event waitlists
 */
public class LocationCapture {

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
     */
    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get current location and return as GeoPoint
     */
    @SuppressWarnings("MissingPermission")
    public void getCurrentLocation(LocationCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationFailed("Location permission not granted");
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return this;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                callback.onLocationReceived(geoPoint);
            } else {
                // Try last known location as fallback
                getLastKnownLocation(callback);
            }
        }).addOnFailureListener(e -> {
            callback.onLocationFailed("Failed to get location: " + e.getMessage());
        });
    }

    /**
     * Fallback method to get last known location
     */
    @SuppressWarnings("MissingPermission")
    private void getLastKnownLocation(LocationCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationFailed("Location permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        callback.onLocationReceived(geoPoint);
                    } else {
                        callback.onLocationFailed("Unable to determine location");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onLocationFailed("Failed to get last location: " + e.getMessage());
                });
    }
}
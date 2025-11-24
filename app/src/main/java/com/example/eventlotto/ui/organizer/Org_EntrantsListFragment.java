package com.example.eventlotto.ui.organizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Org_EntrantsListFragment extends DialogFragment implements OnMapReadyCallback {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_TITLE = "eventTitle";

    private String eventId;
    private String eventTitle;

    // UI Components
    private RecyclerView entrantsRecycler;
    private FrameLayout mapContainer;
    private MapView mapView;
    private GoogleMap googleMap;
    private Button listTabButton;
    private Button mapTabButton;
    private TextView entrantsTitleView;

    // Data
    private EntrantsAdapter adapter;
    private List<DocumentSnapshot> entrantsList = new ArrayList<>();
    private ActivityResultLauncher<String> createFileLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // View state
    private boolean isListView = true;

    public static Org_EntrantsListFragment newInstance(String eventId, String eventTitle) {
        Org_EntrantsListFragment fragment = new Org_EntrantsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_TITLE, eventTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup location permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted && googleMap != null) {
                        enableMyLocation();
                    } else {
                        Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrants_list, container, false);

        // Get arguments
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventTitle = getArguments().getString(ARG_EVENT_TITLE, "Event");
        }

        // Initialize views
        entrantsRecycler = view.findViewById(R.id.entrantsRecycler);
        mapContainer = view.findViewById(R.id.mapContainer);
        mapView = view.findViewById(R.id.mapView);
        listTabButton = view.findViewById(R.id.listTabButton);
        mapTabButton = view.findViewById(R.id.mapTabButton);
        entrantsTitleView = view.findViewById(R.id.entrantsTitle);

        // Set event title
        if (entrantsTitleView != null) {
            entrantsTitleView.setText(eventTitle);
        }

        // Setup RecyclerView
        entrantsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EntrantsAdapter(entrantsList);
        entrantsRecycler.setAdapter(adapter);

        // Initialize MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Load entrants
        if (eventId != null) {
            loadEntrants(eventId);
        }

        // Setup tab buttons
        listTabButton.setOnClickListener(v -> switchToListView());
        mapTabButton.setOnClickListener(v -> switchToMapView());

        // Return button
        View returnButton = view.findViewById(R.id.returnButton);
        returnButton.setOnClickListener(v -> dismiss());

        // Cancel button
        Button cancelButton = view.findViewById(R.id.cancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dismiss());
        }

        // CSV Export setup
        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/csv"),
                uri -> {
                    if (uri != null) exportCsvToUri(uri);
                }
        );

        view.findViewById(R.id.exportCsvButton).setOnClickListener(v -> {
            String filename = eventTitle.replaceAll("[^a-zA-Z0-9]", "_") + "_entrants.csv";
            createFileLauncher.launch(filename);
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Basic map settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        // Check and request location permission
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }

        // Load markers when map is ready
        if (!entrantsList.isEmpty()) {
            loadEntrantLocationsOnMap();
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    /**
     * Switch to List View
     */
    private void switchToListView() {
        isListView = true;
        entrantsRecycler.setVisibility(View.VISIBLE);
        mapContainer.setVisibility(View.GONE);

        // Update button styles
        listTabButton.setBackgroundTintList(
                getResources().getColorStateList(android.R.color.holo_blue_light, null));
        listTabButton.setTextColor(getResources().getColor(android.R.color.white, null));

        mapTabButton.setBackgroundTintList(
                getResources().getColorStateList(android.R.color.darker_gray, null));
        mapTabButton.setTextColor(getResources().getColor(android.R.color.black, null));
    }

    /**
     * Switch to Map View
     */
    private void switchToMapView() {
        isListView = false;
        entrantsRecycler.setVisibility(View.GONE);
        mapContainer.setVisibility(View.VISIBLE);

        // Update button styles
        mapTabButton.setBackgroundTintList(
                getResources().getColorStateList(android.R.color.holo_blue_light, null));
        mapTabButton.setTextColor(getResources().getColor(android.R.color.white, null));

        listTabButton.setBackgroundTintList(
                getResources().getColorStateList(android.R.color.darker_gray, null));
        listTabButton.setTextColor(getResources().getColor(android.R.color.black, null));

        // Load map data if not already loaded
        if (googleMap != null && !entrantsList.isEmpty()) {
            loadEntrantLocationsOnMap();
        }
    }

    /**
     * Load entrant locations and display them on the map
     */
    private void loadEntrantLocationsOnMap() {
        if (googleMap == null) return;

        googleMap.clear(); // Clear existing markers

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();

        // Fetch all user documents to get their locations
        for (DocumentSnapshot entrant : entrantsList) {
            String userId = entrant.getId();
            userTasks.add(db.collection("users").document(userId).get());
        }

        Tasks.whenAllSuccess(userTasks).addOnSuccessListener(results -> {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            int markersAdded = 0;

            for (int i = 0; i < results.size(); i++) {
                DocumentSnapshot userDoc = (DocumentSnapshot) results.get(i);
                DocumentSnapshot entrantDoc = entrantsList.get(i);

                // Get location from entrant's status document
                GeoPoint location = entrantDoc.getGeoPoint("joinLocation");

                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    String name = userDoc.getString("fullName");
                    String status = entrantDoc.getString("status");

                    // Determine marker color based on status
                    float markerColor = getMarkerColorForStatus(status);

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .title(name != null ? name : "Unknown")
                            .snippet("Status: " + (status != null ? status : "N/A"))
                            .icon(BitmapDescriptorFactory.defaultMarker(markerColor));

                    googleMap.addMarker(markerOptions);
                    boundsBuilder.include(latLng);
                    markersAdded++;
                }
            }

            // Zoom to show all markers
            if (markersAdded > 0) {
                try {
                    LatLngBounds bounds = boundsBuilder.build();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                } catch (IllegalStateException e) {
                    // If bounds are invalid, just center on first marker
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getContext(), "No entrants with location data", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load locations: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Get marker color based on entrant status
     */
    private float getMarkerColorForStatus(String status) {
        if (status == null) return BitmapDescriptorFactory.HUE_AZURE;

        switch (status.toLowerCase()) {
            case "waiting":
                return BitmapDescriptorFactory.HUE_CYAN;
            case "selected":
                return BitmapDescriptorFactory.HUE_GREEN;
            case "accepted":
                return BitmapDescriptorFactory.HUE_GREEN;
            case "not_chosen":
                return BitmapDescriptorFactory.HUE_RED;
            case "cancelled":
                return BitmapDescriptorFactory.HUE_ORANGE;
            default:
                return BitmapDescriptorFactory.HUE_AZURE;
        }
    }

    /**
     * Export entrants data to CSV
     */
    private void exportCsvToUri(Uri uri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .document(eventId)
                .collection("status")
                .get()
                .addOnSuccessListener(entrantsSnap -> {

                    if (entrantsSnap.isEmpty()) {
                        Toast.makeText(getContext(), "No entrants found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    List<DocumentSnapshot> entrantsDocs = entrantsSnap.getDocuments();

                    for (DocumentSnapshot entrant : entrantsDocs) {
                        String userId = entrant.getId();
                        Task<DocumentSnapshot> userTask =
                                db.collection("users").document(userId).get();
                        userTasks.add(userTask);
                    }

                    Tasks.whenAllSuccess(userTasks)
                            .addOnSuccessListener(results -> {

                                StringBuilder csv = new StringBuilder();
                                csv.append("Full Name,Email,Phone,Status,Latitude,Longitude\n");

                                for (int i = 0; i < entrantsDocs.size(); i++) {
                                    DocumentSnapshot entrant = entrantsDocs.get(i);
                                    DocumentSnapshot user = (DocumentSnapshot) results.get(i);

                                    String name = user.getString("fullName");
                                    String email = user.getString("email");
                                    String phone = user.getString("phone");
                                    String status = entrant.getString("status");

                                    GeoPoint location = entrant.getGeoPoint("joinLocation");
                                    String lat = location != null ? String.valueOf(location.getLatitude()) : "";
                                    String lng = location != null ? String.valueOf(location.getLongitude()) : "";

                                    csv.append(safe(name)).append(",");
                                    csv.append(safe(email)).append(",");
                                    csv.append(safe(phone)).append(",");
                                    csv.append(safe(status)).append(",");
                                    csv.append(lat).append(",");
                                    csv.append(lng).append("\n");
                                }

                                writeCsv(uri, csv.toString());
                            });
                });
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private void writeCsv(Uri uri, String csvContent) {
        try (OutputStream out = requireContext()
                .getContentResolver()
                .openOutputStream(uri)) {

            out.write(csvContent.getBytes());
            out.flush();

            Toast.makeText(getContext(), "CSV exported successfully!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to export CSV", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Load entrants from Firestore
     */
    private void loadEntrants(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId)
                .collection("status")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    entrantsList.clear();
                    entrantsList.addAll(querySnapshot.getDocuments());
                    adapter.notifyDataSetChanged();

                    // If map is ready and visible, update markers
                    if (googleMap != null && !isListView) {
                        loadEntrantLocationsOnMap();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load entrants: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}
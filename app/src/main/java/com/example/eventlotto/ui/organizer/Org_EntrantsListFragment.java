package com.example.eventlotto.ui.organizer;

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
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Org_EntrantsListFragment extends DialogFragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_TITLE = "eventTitle";

    private String eventId;
    private String eventTitle;

    public static Org_EntrantsListFragment newInstance(String eventId, String eventTitle) {
        Org_EntrantsListFragment fragment = new Org_EntrantsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_TITLE, eventTitle);
        fragment.setArguments(args);
        return fragment;
    }

    // UI Components
    private RecyclerView entrantsRecycler;
    private FrameLayout mapContainer;
    private Button listTabButton;
    private Button mapTabButton;
    private TextView entrantsTitleView;

    // Data
    private EntrantsAdapter adapter;
    private List<DocumentSnapshot> entrantsList = new ArrayList<>();
    private ActivityResultLauncher<String> createFileLauncher;

    // View state
    private boolean isListView = true;

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

        // Cancel button (alternative to return)
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
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
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

        // TODO: Initialize map view here if needed
        Toast.makeText(getContext(), "Map view - implement Google Maps integration", Toast.LENGTH_SHORT).show();
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

                    // For each entrant, fetch matching user
                    for (DocumentSnapshot entrant : entrantsDocs) {
                        String userId = entrant.getId();
                        Task<DocumentSnapshot> userTask =
                                db.collection("users").document(userId).get();
                        userTasks.add(userTask);
                    }

                    // When ALL user tasks complete:
                    Tasks.whenAllSuccess(userTasks)
                            .addOnSuccessListener(results -> {

                                StringBuilder csv = new StringBuilder();
                                csv.append("Full Name,Email,Phone,Status\n");

                                for (int i = 0; i < entrantsDocs.size(); i++) {
                                    DocumentSnapshot entrant = entrantsDocs.get(i);
                                    DocumentSnapshot user = (DocumentSnapshot) results.get(i);

                                    String name = user.getString("fullName");
                                    String email = user.getString("email");
                                    String phone = user.getString("phone");
                                    String status = entrant.getString("status");

                                    csv.append(safe(name)).append(",");
                                    csv.append(safe(email)).append(",");
                                    csv.append(safe(phone)).append(",");
                                    csv.append(safe(status)).append("\n");
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
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
package com.example.eventlotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;

public class AdmEventDetailsFragment extends DialogFragment {

    private String eventId;
    private FirestoreService firestoreService;

    private ImageView eventImage;
    private TextView eventTitle, eventDescription, signupDates, eventDates;
    private Button deleteButton;

    public static AdmEventDetailsFragment newInstance(String eventId) {
        AdmEventDetailsFragment fragment = new AdmEventDetailsFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_details_admin, container, false);

        eventImage = view.findViewById(R.id.eventImage);
        eventTitle = view.findViewById(R.id.eventTitle);
        eventDescription = view.findViewById(R.id.eventDescription);
        signupDates = view.findViewById(R.id.signupDates);
        eventDates = view.findViewById(R.id.eventDates);
        deleteButton = view.findViewById(R.id.btnDeleteEvent);

        eventId = getArguments() != null ? getArguments().getString("eventId") : null;
        firestoreService = new FirestoreService();

        if (eventId != null) {
            fetchEventData(eventId);
        }

        // Set click listener with confirmation dialog
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        return view;
    }

    /**
     * Shows a confirmation popup before deleting the event.
     */
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setCancelable(true)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) // Cancel on left
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (eventId != null) deleteEvent(eventId); // Yes on right
                })
                .show();
    }

    private void fetchEventData(String eventId) {
        firestoreService.events()
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        populateEvent(doc);
                    } else {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void populateEvent(DocumentSnapshot doc) {

        eventTitle.setText(doc.getString("eventTitle"));
        eventDescription.setText(doc.getString("description"));

        Timestamp regOpen = doc.getTimestamp("registrationOpensAt");
        Timestamp regClose = doc.getTimestamp("registrationClosesAt");
        signupDates.setText("Sign-up: " + formatTimestampRange(regOpen, regClose));

        Timestamp eventStart = doc.getTimestamp("eventStartAt");
        Timestamp eventEnd = doc.getTimestamp("eventEndAt");
        eventDates.setText("Event: " + formatTimestampRange(eventStart, eventEnd));

        TextView locationText = getView().findViewById(R.id.location);
        if (locationText != null) {
            com.google.firebase.firestore.GeoPoint location = doc.getGeoPoint("location");
            locationText.setText(location != null
                    ? "Location: " + location.getLatitude() + ", " + location.getLongitude()
                    : "Location: N/A");
        }

        String imageUrl = doc.getString("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void deleteEvent(String eventId) {
        firestoreService.events()
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                    dismiss(); // close fragment after deletion
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private String formatTimestampRange(Timestamp start, Timestamp end) {
        if (start == null || end == null) return "N/A";
        DateFormat df = DateFormat.getDateInstance();
        return df.format(start.toDate()) + " - " + df.format(end.toDate());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}

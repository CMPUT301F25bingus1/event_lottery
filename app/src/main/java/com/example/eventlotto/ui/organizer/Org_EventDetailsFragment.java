package com.example.eventlotto.ui.organizer;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.DateFormat;

public class Org_EventDetailsFragment extends DialogFragment {

    private String eventId;
    private FirestoreService firestoreService;

    private ImageView eventImage;
    private TextView statusText;
    private EditText lotteryNumberInput;
    private TextView eventTitle, eventDescription, signupDates, eventDates, waitlistCount;
    private Button cancelButton, lotteryButton;
    private TextView capacityText, selectedCountText, acceptedCountText;
    private EditText eventUrlField;
    private Button updateUrlButton;

    private LinearLayout acceptDeclineLayout;
    private Button acceptButton, declineButton;

    public static Org_EventDetailsFragment newInstance(String eventId) {
        Org_EventDetailsFragment fragment = new Org_EventDetailsFragment();
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

        View view = inflater.inflate(R.layout.fragment_event_details_org, container, false);

        eventId = getArguments() != null ? getArguments().getString("eventId") : null;
        firestoreService = new FirestoreService();

        // Bind views
        eventImage = view.findViewById(R.id.eventImage);
        eventTitle = view.findViewById(R.id.eventTitle);
        eventDescription = view.findViewById(R.id.eventDescription);
        signupDates = view.findViewById(R.id.signupDates);
        eventDates = view.findViewById(R.id.eventDates);
        waitlistCount = view.findViewById(R.id.waitlistCount);
        cancelButton = view.findViewById(R.id.cancelButton);
        lotteryButton = view.findViewById(R.id.lotteryButton);
        cancelButton.setOnClickListener(v -> dismiss());
        statusText = view.findViewById(R.id.statusText);
        capacityText = view.findViewById(R.id.capacityText);
        selectedCountText = view.findViewById(R.id.selectedCountText);
        acceptedCountText = view.findViewById(R.id.acceptedCountText);
        eventUrlField = view.findViewById(R.id.input_event_url_details);
        updateUrlButton = view.findViewById(R.id.btn_update_event_url);

        // Entrants buttons
        Button viewEntrantsButton = view.findViewById(R.id.viewEntrantsButton);
        viewEntrantsButton.setOnClickListener(v -> {
            if (eventId != null) {
                String title = eventTitle.getText().toString();
                Org_EntrantsListFragment fragment = Org_EntrantsListFragment.newInstance(eventId, title);
                fragment.show(getParentFragmentManager(), "entrantsList");
            }
        });

        Button notifyEntrantsButton = view.findViewById(R.id.notifyEntrantsButton);
        notifyEntrantsButton.setOnClickListener(v -> {
            Org_NotifyEntrantsDialog dialog = Org_NotifyEntrantsDialog.newInstance(eventId);
            dialog.show(getParentFragmentManager(), "notify_dialog");
        });

        if (updateUrlButton != null) {
            updateUrlButton.setOnClickListener(v -> updateEventUrl());
        }

        if (eventId != null) {
            fetchEventData(eventId);
            loadWaitingCount(eventId);
        }
        updateCapacityStatus();

        lotteryButton.setOnClickListener(v -> doLottery());

        return view;
    }

    private void doLottery() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnSuccessListener(eventSnap -> {
            if (!eventSnap.exists()) return;

            Long capacity = eventSnap.getLong("capacity");
            if (capacity == null) capacity = 0L;
            Long finalCapacity = capacity;

            // Count selected + accepted
            eventRef.collection("status")
                    .whereIn("status", java.util.Arrays.asList("selected", "accepted"))
                    .get()
                    .addOnSuccessListener(selectedSnapshot -> {
                        int alreadyChosen = (selectedSnapshot != null) ? selectedSnapshot.size() : 0;

                        if (alreadyChosen >= finalCapacity) {
                            Toast.makeText(getContext(), "Event is already at capacity.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int remainingCapacity = (int) (finalCapacity - alreadyChosen);

                        // Load waiting users
                        eventRef.collection("status")
                                .whereEqualTo("status", "waiting")
                                .get()
                                .addOnSuccessListener(waitingSnapshot -> {
                                    if (waitingSnapshot == null || waitingSnapshot.isEmpty()) {
                                        Toast.makeText(getContext(), "No users on the waiting list.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    java.util.List<DocumentSnapshot> waitingUsers = waitingSnapshot.getDocuments();
                                    java.util.Collections.shuffle(waitingUsers);

                                    int winnersCount = Math.min(remainingCapacity, waitingUsers.size());

                                    WriteBatch batch = db.batch();

                                    for (int i = 0; i < waitingUsers.size(); i++) {
                                        DocumentReference docRef = waitingUsers.get(i).getReference();
                                        if (i < winnersCount) {
                                            batch.update(docRef, "status", "selected");
                                        } else {
                                            batch.update(docRef, "status", "not_chosen");
                                        }
                                    }

                                    batch.commit()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(
                                                        getContext(),
                                                        "Lottery complete! Selected " + winnersCount + " entrants.",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                                updateCapacityStatus();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(getContext(),
                                                            "Lottery failed: " + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show()
                                            );

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Failed to load waiting list: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );

                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Failed to check selected users: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );

        }).addOnFailureListener(e ->
                Toast.makeText(getContext(),
                        "Failed to load event data: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show()
        );
    }

    private void updateCapacityStatus() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnSuccessListener(eventSnap -> {
            if (!eventSnap.exists()) return;

            Long capacity = eventSnap.getLong("capacity");
            capacityText.setText("Capacity: " + (capacity != null ? capacity : 0));

            eventRef.collection("status")
                    .whereEqualTo("status", "selected")
                    .get()
                    .addOnSuccessListener(selectedSnap -> {
                        int selectedCount = (selectedSnap != null) ? selectedSnap.size() : 0;
                        selectedCountText.setText("Selected: " + selectedCount);

                        eventRef.collection("status")
                                .whereEqualTo("status", "accepted")
                                .get()
                                .addOnSuccessListener(acceptedSnap -> {
                                    int acceptedCount = (acceptedSnap != null) ? acceptedSnap.size() : 0;
                                    acceptedCountText.setText("Accepted: " + acceptedCount);
                                });
                    });
        });
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
        eventTitle.setText(doc.getString("eventTitle") != null ? doc.getString("eventTitle") : "No Title");
        eventDescription.setText(doc.getString("description") != null ? doc.getString("description") : "No Description");

        Timestamp regOpen = doc.getTimestamp("registrationOpensAt");
        Timestamp regClose = doc.getTimestamp("registrationClosesAt");
        signupDates.setText("Sign-up: " + formatTimestampRange(regOpen, regClose));

        Timestamp eventStart = doc.getTimestamp("eventStartAt");
        Timestamp eventEnd = doc.getTimestamp("eventEndAt");
        eventDates.setText("Event: " + formatTimestampRange(eventStart, eventEnd));

        if (waitlistCount != null) {
            waitlistCount.setText("Waiting: 0");
        }

        TextView geoConsentText = getView().findViewById(R.id.geoConsent);
        if (geoConsentText != null) {
            Boolean geoConsent = doc.getBoolean("geoConsent");
            geoConsentText.setText("Geo Consent Required: " + (geoConsent != null && geoConsent ? "Yes" : "No"));
        }

        TextView locationText = getView().findViewById(R.id.location);
        if (locationText != null) {
            com.google.firebase.firestore.GeoPoint location = doc.getGeoPoint("location");
            locationText.setText(location != null
                    ? "Location: " + location.getLatitude() + ", " + location.getLongitude()
                    : "Location: N/A");
        }

        TextView organizerText = getView().findViewById(R.id.organizerId);
        if (organizerText != null) {
            DocumentReference organizerRef = doc.getDocumentReference("organizerId");
            if (organizerRef != null) {
                organizerRef.get()
                        .addOnSuccessListener(organizerDoc -> {
                            if (organizerDoc.exists()) {
                                String organizerName = organizerDoc.getString("name");
                                organizerText.setText("Organizer: " + (organizerName != null ? organizerName : "N/A"));
                            } else {
                                organizerText.setText("Organizer: N/A");
                            }
                        })
                        .addOnFailureListener(e -> organizerText.setText("Organizer: N/A"));
            } else {
                organizerText.setText("Organizer: N/A");
            }
        }

        // Load event image
        String imageUrl = doc.getString("eventURL");
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = doc.getString("imageUrl"); // legacy fallback
        }
        if (eventUrlField != null) {
            eventUrlField.setText(imageUrl != null ? imageUrl : "");
        }
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl.trim())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .fitCenter()
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.mipmap.ic_launcher);
        }
    }

    /**
     * Updates the eventURL field for this event in Firestore.
     */
    private void updateEventUrl() {
        if (eventId == null || eventUrlField == null) return;
        String newUrl = eventUrlField.getText().toString().trim();

        DocumentReference eventRef = FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId);

        Object value = TextUtils.isEmpty(newUrl) ? FieldValue.delete() : newUrl;

        eventRef.update("eventURL", value)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                        "Event URL updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to update URL: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadWaitingCount(String eventId) {
        if (waitlistCount == null) return;
        FirebaseFirestore.getInstance()
                .collection("events").document(eventId)
                .collection("status")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(query -> {
                    int c = (query != null) ? query.size() : 0;
                    waitlistCount.setText(c + " is on the waiting list.");
                })
                .addOnFailureListener(e -> waitlistCount.setText("Waiting: 0"));
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

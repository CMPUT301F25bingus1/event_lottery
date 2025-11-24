package com.example.eventlotto.ui.organizer;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.example.eventlotto.ui.entrant.Ent_EventDetailsFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;

public class Org_EventDetailsFragment extends DialogFragment {

    /**
     * The ID of the event to display.
     */
    private String eventId;

    /**
     * Firestore service helper for interacting with the database.
     */
    private FirestoreService firestoreService;

    private ImageView eventImage;

    /**
     * TextView displaying status text.
     */
    private TextView statusText;
    private EditText lotteryNumberInput;


    /**
     * TextViews for event title, description, signup dates, event dates, and capacity.
     */
    private TextView eventTitle, eventDescription, signupDates, eventDates, waitlistCount;

    /**
     * Buttons for cancelling the dialog or joining/leaving the waitlist.
     */
    private Button cancelButton, lotteryButton;
    private TextView capacityText, selectedCountText, acceptedCountText;

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
        lotteryNumberInput = view.findViewById(R.id.lotteryNumberInput);
        capacityText = view.findViewById(R.id.capacityText);
        selectedCountText = view.findViewById(R.id.selectedCountText);
        acceptedCountText = view.findViewById(R.id.acceptedCountText);

        // View Entrants Button - UPDATED
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

        // Step 1: Get event capacity
        eventRef.get().addOnSuccessListener(eventSnap -> {
            if (!eventSnap.exists()) return;

            Long capacity = eventSnap.getLong("capacity");
            if (capacity == null) capacity = 0L;
            Long finalCapacity = capacity;

            // Step 2: Check how many users are already selected/accepted
            eventRef.collection("status")
                    .whereIn("status", java.util.Arrays.asList("selected", "accepted"))
                    .get()
                    .addOnSuccessListener(selectedSnapshot -> {
                        int alreadySelected = (selectedSnapshot != null) ? selectedSnapshot.size() : 0;

                        if (alreadySelected >= finalCapacity) {
                            Toast.makeText(getContext(), "Entrants at capacity", Toast.LENGTH_SHORT).show();
                            return; // Stop the lottery
                        }

                        // Step 3: Read number to select from input
                        int numberToSelect = 0;
                        String inputStr = lotteryNumberInput.getText().toString();
                        try {
                            numberToSelect = Integer.parseInt(inputStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int remainingCapacity = (int) (finalCapacity - alreadySelected);

                        // Guard: can't select more than remaining capacity
                        if (numberToSelect <= 0) {
                            Toast.makeText(getContext(), "Number must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (numberToSelect > remainingCapacity) {
                            Toast.makeText(getContext(), "Cannot select more than remaining capacity: " + remainingCapacity, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Step 4: Get waiting users
                        int finalNumberToSelect = numberToSelect;
                        eventRef.collection("status")
                                .whereEqualTo("status", "waiting")
                                .get()
                                .addOnSuccessListener(waitingSnapshot -> {
                                    if (waitingSnapshot == null || waitingSnapshot.isEmpty()) {
                                        Toast.makeText(getContext(), "No users waiting for lottery", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    java.util.List<DocumentSnapshot> waitingUsers = waitingSnapshot.getDocuments();
                                    java.util.Collections.shuffle(waitingUsers); // randomize order

                                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                                    // Limit winners to either the input number or available waiting users
                                    int winnersCount = Math.min(finalNumberToSelect, waitingUsers.size());

                                    for (int i = 0; i < waitingUsers.size(); i++) {
                                        DocumentReference docRef = waitingUsers.get(i).getReference();
                                        if (i < winnersCount) {
                                            batch.update(docRef, "status", "selected");
                                        } else {
                                            batch.update(docRef, "status", "not_chosen");
                                        }
                                    }

                                    batch.commit()
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(getContext(), "Lottery completed! " + winnersCount + " selected.", Toast.LENGTH_SHORT).show()
                                            )
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(getContext(), "Lottery failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                            );
                                    updateCapacityStatus();


                                }).addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Failed to get waiting users: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );

                    }).addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to check already selected users: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to get event data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void updateCapacityStatus() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnSuccessListener(eventSnap -> {
            if (!eventSnap.exists()) return;

            Long capacity = eventSnap.getLong("capacity");
            capacityText.setText("Capacity: " + (capacity != null ? capacity : 0));

            // Get selected count
            eventRef.collection("status")
                    .whereEqualTo("status", "selected")
                    .get()
                    .addOnSuccessListener(selectedSnap -> {
                        int selectedCount = (selectedSnap != null) ? selectedSnap.size() : 0;
                        selectedCountText.setText("Selected: " + selectedCount);

                        // Get accepted count
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


    /**
     * Fetches event data from Firestore and populates the UI.
     *
     * @param eventId The ID of the event to fetch.
     */
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

    /**
     * Populates the fragment UI with data from the Firestore document.
     *
     * @param doc Firestore document containing event data.
     */
    private void populateEvent(DocumentSnapshot doc) {
        // Basic fields
        eventTitle.setText(doc.getString("eventTitle") != null ? doc.getString("eventTitle") : "No Title");
        eventDescription.setText(doc.getString("description") != null ? doc.getString("description") : "No Description");

        // Registration and event dates
        Timestamp regOpen = doc.getTimestamp("registrationOpensAt");
        Timestamp regClose = doc.getTimestamp("registrationClosesAt");
        signupDates.setText("Sign-up: " + formatTimestampRange(regOpen, regClose));

        Timestamp eventStart = doc.getTimestamp("eventStartAt");
        Timestamp eventEnd = doc.getTimestamp("eventEndAt");
        eventDates.setText("Event: " + formatTimestampRange(eventStart, eventEnd));

        // Initialize waiting count label; actual count loaded separately
        if (waitlistCount != null) {
            waitlistCount.setText("Waiting: 0");
        }

        // Boolean fields
        TextView geoConsentText = getView().findViewById(R.id.geoConsent);

        if (geoConsentText != null) {
            Boolean geoConsent = doc.getBoolean("geoConsent");
            geoConsentText.setText("Geo Consent Required: " + (geoConsent != null && geoConsent ? "Yes" : "No"));
        }


        // Location
        TextView locationText = getView().findViewById(R.id.location);
        if (locationText != null) {
            com.google.firebase.firestore.GeoPoint location = doc.getGeoPoint("location");
            locationText.setText(location != null
                    ? "Location: " + location.getLatitude() + ", " + location.getLongitude()
                    : "Location: N/A");
        }

        // Organizer
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

    /**
     * Formats a start and end timestamp as a human-readable date range.
     *
     * @param start Start timestamp.
     * @param end   End timestamp.
     * @return Formatted date range or "N/A" if either timestamp is null.
     */
    private String formatTimestampRange(Timestamp start, Timestamp end) {
        if (start == null || end == null) return "N/A";
        DateFormat df = DateFormat.getDateInstance();
        return df.format(start.toDate()) + " - " + df.format(end.toDate());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Make dialog full width
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}

package com.example.eventlotto.ui.organizer;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.example.eventlotto.ui.entrant.Ent_EventDetailsFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;

public class Org_EventDetailsFragment extends DialogFragment {

    /** The ID of the event to display. */
    private String eventId;

    /** Firestore service helper for interacting with the database. */
    private FirestoreService firestoreService;

    private ImageView eventImage;

    /** TextView displaying status text. */
    private TextView statusText;

    /** TextViews for event title, description, signup dates, event dates, and capacity. */
    private TextView eventTitle, eventDescription, signupDates, eventDates, waitlistCount;

    /** Buttons for cancelling the dialog or joining/leaving the waitlist. */
    private Button cancelButton, lotteryButton;

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
        acceptDeclineLayout = view.findViewById(R.id.acceptDeclineLayout);
        acceptButton = view.findViewById(R.id.acceptButton);
        declineButton = view.findViewById(R.id.declineButton);

        if (eventId != null) {
            fetchEventData(eventId);
            loadWaitingCount(eventId);
        }

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

                        // Step 3: Get all users with status "waiting"
                        eventRef.collection("status")
                                .whereEqualTo("status", "waiting")
                                .get()
                                .addOnSuccessListener(waitingSnapshot -> {
                                    if (waitingSnapshot == null || waitingSnapshot.isEmpty()) {
                                        Toast.makeText(getContext(), "No users waiting for lottery", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    java.util.List<DocumentSnapshot> waitingUsers = waitingSnapshot.getDocuments();
                                    java.util.Collections.shuffle(waitingUsers); // randomize

                                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                                    // Calculate how many more users we can select
                                    int remainingCapacity = (int) (finalCapacity - alreadySelected);
                                    for (int i = 0; i < waitingUsers.size(); i++) {
                                        DocumentReference docRef = waitingUsers.get(i).getReference();
                                        if (i < remainingCapacity) {
                                            batch.update(docRef, "status", "selected");
                                        } else {
                                            batch.update(docRef, "status", "not_chosen");
                                        }
                                    }

                                    batch.commit()
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(getContext(), "Lottery completed! " + Math.min(waitingUsers.size(), remainingCapacity) + " selected.", Toast.LENGTH_SHORT).show()
                                            )
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(getContext(), "Lottery failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                            );

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
        String imageUrl = doc.getString("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(imageUrl);
                    final android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> eventImage.setImageBitmap(bmp));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
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
                    waitlistCount.setText(c+ " is on the waiting list.");
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

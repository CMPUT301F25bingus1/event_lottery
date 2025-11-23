package com.example.eventlotto.ui.entrant;

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
import com.example.eventlotto.model.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;

public class Ent_EventDetailsFragment extends DialogFragment {

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
    private Button cancelButton, joinWaitlistButton;

    private LinearLayout acceptDeclineLayout;
    private Button acceptButton, declineButton;

    public static Ent_EventDetailsFragment newInstance(String eventId) {
        Ent_EventDetailsFragment fragment = new Ent_EventDetailsFragment();
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

        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

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
        joinWaitlistButton = view.findViewById(R.id.joinWaitlistButton);
        cancelButton.setOnClickListener(v -> dismiss());
        statusText = view.findViewById(R.id.statusText);
        acceptDeclineLayout = view.findViewById(R.id.acceptDeclineLayout);
        acceptButton = view.findViewById(R.id.acceptButton);
        declineButton = view.findViewById(R.id.declineButton);

        if (eventId != null) {
            fetchEventData(eventId);
            loadWaitingCount(eventId);
        }

        if (acceptButton != null && declineButton != null) {
            acceptButton.setOnClickListener(v -> acceptInvitation());
            declineButton.setOnClickListener(v -> declineInvitation());
        }

        joinWaitlistButton.setOnClickListener(v -> joinWaitlist());

        return view;
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
                        checkIfOnWaitlist(eventId);
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

    /** Joins the current device to the event's waitlist in Firestore. */
    private void joinWaitlist() {
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(eventRef);

            Long entrantsApplied = snapshot.getLong("entrantsApplied");
            Long maxEntrants = snapshot.getLong("maxEntrants");

            if (entrantsApplied == null) entrantsApplied = 0L;

            // Check if event is full
            if (maxEntrants != null && entrantsApplied >= maxEntrants) {
                throw new IllegalStateException("This event is full.");
            }

            // Add user to status subcollection
            DocumentReference userStatusRef = eventRef
                    .collection("status")
                    .document(deviceId);

            transaction.set(userStatusRef, new java.util.HashMap<String, Object>() {{
                put("status", "waiting");
                put("joinedAt", Timestamp.now());
            }});

            // Increment entrantsApplied
            transaction.update(eventRef, "entrantsApplied", entrantsApplied + 1);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Successfully joined waitlist", Toast.LENGTH_SHORT).show();

            String nid = deviceId + "_" + eventId;
            Notification n = new Notification();
            n.setNid(nid);
            n.setUid(deviceId);
            n.setEid(eventId);

            firestoreService.saveNotification(n)
                    .addOnSuccessListener(v -> {
                        // Optional: small toast
                        Toast.makeText(requireContext(), "Auto-subscribed to notifications", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(ex -> {
                        Toast.makeText(requireContext(), "Failed auto-subscribe: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            if (statusText != null) applyStatusText(statusText, "waiting");
            showJoinedUI(eventId, deviceId);
            loadWaitingCount(eventId);
        }).addOnFailureListener(e -> {
            if (e instanceof IllegalStateException) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptInvitation() {

        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Update event status (signed_up)
        FirebaseFirestore.getInstance()
                .collection("events").document(eventId)
                .collection("status").document(deviceId)
                .update("status", "signed_up")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event accepted!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().setFragmentResult("eventStatusChanged", Bundle.EMPTY);
                    dismiss();
                })

                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void declineInvitation() {
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        // Update event status (cancelled)
        FirebaseFirestore.getInstance()
                .collection("events").document(this.eventId)
                .collection("status").document(deviceId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event declined", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().setFragmentResult("eventStatusChanged", Bundle.EMPTY);
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the UI to show that the user has joined the waitlist.
     *
     * @param eventId  The event ID.
     * @param deviceId The current device ID.
     */
    private void showJoinedUI(String eventId, String deviceId) {
        FirebaseFirestore.getInstance()
                .collection("events").document(eventId)
                .collection("status").document(deviceId)
                .get()
                .addOnSuccessListener(statusDoc -> {
                    String status = statusDoc.exists() ? statusDoc.getString("status") : "waiting";
                    if ("signed_up".equalsIgnoreCase(status)) {
                        joinWaitlistButton.setText("Leave Registration");
                        joinWaitlistButton.setBackgroundTintList(
                                getResources().getColorStateList(android.R.color.holo_red_light)
                        );
                        joinWaitlistButton.setOnClickListener(v -> leaveRegistration(eventId, deviceId));
                        acceptDeclineLayout.setVisibility(View.GONE);
                    } else {
                        joinWaitlistButton.setText("Leave Waitlist");
                        joinWaitlistButton.setBackgroundTintList(
                                getResources().getColorStateList(android.R.color.holo_red_light)
                        );
                        joinWaitlistButton.setOnClickListener(v -> leaveWaitlist(eventId, deviceId));
                        acceptDeclineLayout.setVisibility(View.GONE);
                    }
                });
    }

    /** Updates the UI to show that the user has not joined the waitlist. */
    private void showNotJoinedUI() {
        joinWaitlistButton.setText("Join Waitlist");
        joinWaitlistButton.setBackgroundTintList(
                getResources().getColorStateList(android.R.color.holo_green_dark)
        );
        joinWaitlistButton.setOnClickListener(v -> joinWaitlist());
        acceptDeclineLayout.setVisibility(View.GONE);
    }

    /**
     * Removes the device from the event's waitlist.
     *
     * @param eventId  The event ID.
     * @param deviceId The current device ID.
     */
    private void leaveWaitlist(String eventId, String deviceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);
        DocumentReference userStatusRef = eventRef.collection("status").document(deviceId);

        db.runTransaction(transaction -> {
            DocumentSnapshot eventSnap = transaction.get(eventRef);
            Long entrantsApplied = eventSnap.getLong("entrantsApplied");
            if (entrantsApplied == null || entrantsApplied <= 0) entrantsApplied = 0L;

            // Remove user’s status document
            transaction.delete(userStatusRef);

            // Decrement entrantsApplied safely
            transaction.update(eventRef, "entrantsApplied", Math.max(entrantsApplied - 1, 0));

            return null;
        }).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(), "Successfully left the waitlist", Toast.LENGTH_SHORT).show();
            if (statusText != null) statusText.setVisibility(View.GONE);
            showNotJoinedUI();
            loadWaitingCount(eventId);
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to leave waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    /**
     * Removes the device from event's registration
     * @param eventId The event ID.
     * @param deviceId The current device ID.
     */
    private void leaveRegistration(String eventId, String deviceId) {
        FirebaseFirestore.getInstance()
                .collection("events").document(eventId)
                .collection("status").document(deviceId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Registration cancelled", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().setFragmentResult("eventStatusChanged", Bundle.EMPTY);
                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to cancel registration: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Checks if the current device is already on the waitlist and updates the UI accordingly.
     *
     * @param eventId The event ID.
     */
    private void checkIfOnWaitlist(String eventId) {
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        FirebaseFirestore.getInstance()
                .collection("events").document(eventId)
                .collection("status").document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String raw = doc.getString("status");
                        if (statusText != null) applyStatusText(statusText, raw);
                        if ("selected".equalsIgnoreCase(raw) && acceptDeclineLayout != null) {
                            acceptDeclineLayout.setVisibility(View.VISIBLE);
                            joinWaitlistButton.setVisibility(View.GONE);
                        } else {
                            showJoinedUI(eventId, deviceId);
                        }
                        loadWaitingCount(eventId);
                    } else {
                        if (statusText != null) statusText.setVisibility(View.GONE);
                        showNotJoinedUI();
                        loadWaitingCount(eventId);
                    }
                });
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
     * Applies a status label and background to a TextView.
     *
     * @param tv        The TextView to update.
     * @param rawStatus The status string, may be null.
     */
    private void applyStatusText(@NonNull TextView tv, @Nullable String rawStatus) {
        String s = (rawStatus == null ? "waiting" : rawStatus).trim().toLowerCase();
        int drawableRes;
        String label;

        switch (s) {
            case "selected":
                drawableRes = R.drawable.bg_status_selected; label = "Selected"; break;
            case "signed up":
            case "signed_up":
                drawableRes = R.drawable.bg_status_signed_up; label = "Signed Up"; break;
            case "cancelled":
            case "canceled":
                drawableRes = R.drawable.bg_status_cancelled; label = "Cancelled"; break;
            case "not chosen":
            case "not_chosen":
                drawableRes = R.drawable.bg_status_not_chosen; label = "Not Chosen"; break;
            default:
                drawableRes = R.drawable.bg_status_waiting; label = "Waiting";
        }

        try {
            Drawable d = ContextCompat.getDrawable(requireContext(), drawableRes);
            int color = ((GradientDrawable) d).getColor() != null
                    ? ((GradientDrawable) d).getColor().getDefaultColor()
                    : ContextCompat.getColor(requireContext(), android.R.color.black);
            tv.setTextColor(color);
        } catch (Exception e) {
            e.printStackTrace();
            tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        }

        tv.setText(label);
        tv.setVisibility(View.VISIBLE);
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

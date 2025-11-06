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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;

import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;

public class Ent_EventDetailsFragment extends DialogFragment {

    private String eventId;
    private FirestoreService firestoreService;

    private ImageView eventImage;
    private TextView statusText;
    private TextView eventTitle, eventDescription, signupDates, eventDates, waitlistCount;
    private Button cancelButton, joinWaitlistButton;

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

        if (eventId != null) {
            fetchEventData(eventId);
            loadWaitingCount(eventId);
        }

        joinWaitlistButton.setOnClickListener(v -> joinWaitlist());

        return view;
    }

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

    private void populateEvent(DocumentSnapshot doc) {
        // Basic fields
        eventTitle.setText(doc.getString("eventTitle") != null ? doc.getString("eventTitle") : "No Title");
        eventDescription.setText(doc.getString("description") != null ? doc.getString("description") : "No Description");

        // Registration dates
        Timestamp regOpen = doc.getTimestamp("registrationOpensAt");
        Timestamp regClose = doc.getTimestamp("registrationClosesAt");
        signupDates.setText("Sign-up: " + formatTimestampRange(regOpen, regClose));

        // Event dates
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
            if (location != null) {
                locationText.setText("Location: " + location.getLatitude() + ", " + location.getLongitude());
            } else {
                locationText.setText("Location: N/A");
            }
        }

        // Organizer
        TextView organizerText = getView().findViewById(R.id.organizerId);
        if (organizerText != null) {
            DocumentReference organizerRef = doc.getDocumentReference("organizerId");
            if (organizerRef != null) {
                organizerRef.get()
                        .addOnSuccessListener(organizerDoc -> {
                            if (organizerDoc.exists()) {
                                String organizerName = organizerDoc.getString("name"); // adjust to your Firestore structure
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

        // Image
        String imageUrl = doc.getString("imageUrl"); // or replace with actual image field if different
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

    private void joinWaitlist() {
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        firestoreService.joinWaitlist(eventId, deviceId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Successfully joined waitlist", Toast.LENGTH_SHORT).show();
                    // show colored text (if you don't store a status yet, default to "waiting")
                    if (statusText != null) applyStatusText(statusText, /* rawStatus */ "waiting");

                    showJoinedUI(eventId, deviceId);
                    loadWaitingCount(eventId);
                })

                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showJoinedUI(String eventId, String deviceId) {
        joinWaitlistButton.setText("Leave Waitlist");
        joinWaitlistButton.setBackgroundTintList(
                getResources().getColorStateList(android.R.color.holo_red_light)
        );
        joinWaitlistButton.setOnClickListener(v -> leaveWaitlist(eventId, deviceId));
    }

    private void showNotJoinedUI() {
        joinWaitlistButton.setText("Join Waitlist");
        joinWaitlistButton.setBackgroundTintList(
                getResources().getColorStateList(android.R.color.holo_green_dark)
        );
        joinWaitlistButton.setOnClickListener(v -> joinWaitlist());
    }

    private void leaveWaitlist(String eventId, String deviceId) {
        FirebaseFirestore.getInstance()
                .collection("events").document(eventId)
                .collection("status").document(deviceId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Successfully left the waitlist", Toast.LENGTH_SHORT).show();
                    if (statusText != null) statusText.setVisibility(View.GONE);
                    showNotJoinedUI();
                    loadWaitingCount(eventId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to leave waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

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
                        String raw = doc.getString("status"); // may be null
                        if (statusText != null) applyStatusText(statusText, raw); // null -> waiting
                        showJoinedUI(eventId, deviceId);
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
                    waitlistCount.setText(c+ "is on the waiting list.");
                })
                .addOnFailureListener(e -> waitlistCount.setText("Waiting: 0"));
    }

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

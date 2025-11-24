package com.example.eventlotto.ui.organizer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotto.R;
import com.example.eventlotto.functions.scan.GenerateQRFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment for organizers to create new events in the EventLotto app.
 * <p>
 * Provides input fields for event details such as title, description, capacity,
 * location coordinates, date ranges for event and registration, and optional max entrants.
 * On successful creation, the event is stored in Firestore, and a QR code is displayed
 * for event access.
 * </p>
 */
public class Org_CreateEventFragment extends Fragment {

    /** Input field for the event title. */
    private EditText titleField;

    /** Input field for the event description. */
    private EditText descField;

    /** Input field for the event capacity. */
    private EditText capacityField;

    /** Input field for the event latitude. */
    private EditText latField;

    /** Input field for the event longitude. */
    private EditText lonField;

    /** Input field for event start date. */
    private EditText inputEventStart;

    /** Input field for event end date. */
    private EditText inputEventEnd;

    /** Input field for registration open date. */
    private EditText inputRegOpen;

    /** Input field for registration close date. */
    private EditText inputRegClose;

    /** Input field for maximum entrants allowed (optional). */
    private EditText maxEntrantsField;

    /** Input field for number of entrants already applied (optional). */
    private EditText entrantsAppliedField;

    /** Checkbox indicating organizer consent for geolocation. */
    private CheckBox geoConsentBox;

    /** Button to trigger event creation. */
    private Button createBtn;

    /** Firestore database instance for saving events. */
    private FirebaseFirestore db;

    /**
     * Called to create and return the view hierarchy associated with this fragment.
     * <p>
     * Initializes input fields, date pickers, and create button listeners.
     *
     * @param inflater LayoutInflater used to inflate the fragment's layout.
     * @param container Parent view that this fragment's UI should attach to.
     * @param savedInstanceState Previously saved state, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_events, container, false);

        // Initialize input fields
        titleField = view.findViewById(R.id.input_event_title);
        descField = view.findViewById(R.id.input_description);
        capacityField = view.findViewById(R.id.input_capacity);
        latField = view.findViewById(R.id.input_latitude);
        lonField = view.findViewById(R.id.input_longitude);
        geoConsentBox = view.findViewById(R.id.check_geo_consent);
        createBtn = view.findViewById(R.id.btn_create_event);
        maxEntrantsField = view.findViewById(R.id.input_max_entrants);
        inputEventStart = view.findViewById(R.id.input_event_start);
        inputEventEnd = view.findViewById(R.id.input_event_end);
        inputRegOpen = view.findViewById(R.id.input_reg_open);
        inputRegClose = view.findViewById(R.id.input_reg_close);

        db = FirebaseFirestore.getInstance();

        // Setup date pickers
        inputEventStart.setOnClickListener(v -> showDatePicker(inputEventStart));
        inputEventEnd.setOnClickListener(v -> showDatePicker(inputEventEnd));
        inputRegOpen.setOnClickListener(v -> showDatePicker(inputRegOpen));
        inputRegClose.setOnClickListener(v -> showDatePicker(inputRegClose));

        // Setup create event button
        createBtn.setOnClickListener(v -> createEvent());

        return view;
    }

    /**
     * Creates a new event using the provided input fields and stores it in Firestore.
     * <p>
     * Performs basic validation, parses numeric and date inputs, and constructs
     * a map of event data. On success, displays a QR code for the event.
     */
    private void createEvent() {
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        DocumentReference organizerRef = db.collection("users").document(deviceId);

        String title = titleField.getText().toString().trim();
        String desc = descField.getText().toString().trim();
        String capStr = capacityField.getText().toString().trim();
        String latStr = latField.getText().toString().trim();
        String lonStr = lonField.getText().toString().trim();
        String maxEntrantsStr = maxEntrantsField.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc) ||
                TextUtils.isEmpty(capStr) || TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lonStr)) {
            Toast.makeText(getContext(), "All required fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = Integer.parseInt(capStr);
        double lat = Double.parseDouble(latStr);
        double lon = Double.parseDouble(lonStr);
        int maxEntrants = TextUtils.isEmpty(maxEntrantsStr) ? 0 : Integer.parseInt(maxEntrantsStr);

        Timestamp now = Timestamp.now();
        Timestamp eventStart = parseDateToTimestamp(inputEventStart.getText().toString(), now);
        Timestamp eventEnd = parseDateToTimestamp(inputEventEnd.getText().toString(), now);
        Timestamp regOpen = parseDateToTimestamp(inputRegOpen.getText().toString(), now);
        Timestamp regClose = parseDateToTimestamp(inputRegClose.getText().toString(), now);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("organizerId", organizerRef);
        eventData.put("eventTitle", title);
        eventData.put("description", desc);
        eventData.put("capacity", capacity);
        eventData.put("createdAt", now);
        eventData.put("eventStartAt", eventStart);
        eventData.put("eventEndAt", eventEnd);
        eventData.put("registrationOpensAt", regOpen);
        eventData.put("registrationClosesAt", regClose);
        eventData.put("geoConsent", geoConsentBox.isChecked());
        eventData.put("entrantsApplied", 0);
        eventData.put("maxEntrants", maxEntrants);
        eventData.put("location", new GeoPoint(lat, lon));

        db.collection("events")
                .add(eventData)
                .addOnSuccessListener(documentReference -> {
                    String eventId = documentReference.getId();
                    db.collection("events").document(eventId).update("eventId", eventId);
                    Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_LONG).show();

                    GenerateQRFragment qrFragment = GenerateQRFragment.newInstance(eventId);
                    qrFragment.show(getParentFragmentManager(), "generate_qr_dialog");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Converts a date string to a Firestore {@link Timestamp}.
     * <p>
     * If parsing fails or input is empty, returns the fallback timestamp.
     *
     * @param dateStr Date string in "yyyy-MM-dd" format.
     * @param fallback Fallback timestamp to use if parsing fails.
     * @return Parsed {@link Timestamp} or fallback.
     */
    private Timestamp parseDateToTimestamp(String dateStr, Timestamp fallback) {
        if (TextUtils.isEmpty(dateStr)) return fallback;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date date = sdf.parse(dateStr);
            return new Timestamp(date);
        } catch (Exception e) {
            e.printStackTrace();
            return fallback;
        }
    }

    /**
     * Shows a {@link DatePickerDialog} and sets the selected date on the target EditText.
     *
     * @param target EditText to populate with the selected date.
     */
    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    target.setText(dateStr);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }
}
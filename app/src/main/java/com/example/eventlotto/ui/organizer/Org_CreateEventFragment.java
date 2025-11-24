package com.example.eventlotto.ui.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.eventlotto.R;
import com.example.eventlotto.functions.scan.GenerateQRFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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

    /** Input field for the event location. */
    private EditText locationField;

    /** Input field for the event latitude (hidden). */
    private EditText latField;

    /** Input field for the event longitude (hidden). */
    private EditText lonField;

    /** Input field for event start date. */
    private EditText inputEventStart;

    /** Input field for event end date. */
    private EditText inputEventEnd;

    /** Input field for event start time. */
    private EditText inputTimeStart;

    /** Input field for event end time. */
    private EditText inputTimeEnd;

    /** Input field for registration open date. */
    private EditText inputRegOpen;

    /** Input field for registration close date. */
    private EditText inputRegClose;

    /** Input field for maximum entrants allowed (optional). */
    private EditText maxEntrantsField;

    /** Input field for the poster URL. */
    private EditText posterUrlField;

    /** Toggle group for days of week selection. */
    private MaterialButtonToggleGroup daysToggleGroup;

    /** List to store selected days of the week. */
    private List<String> selectedDays = new ArrayList<>();

    /** Switch for geolocation consent. */
    private SwitchCompat geoConsentSwitch;

    /** Button to trigger event creation. */
    private Button createBtn;

    /** Firestore database instance for saving events. */
    private FirebaseFirestore db;

    /**
     * Called to create and return the view hierarchy associated with this fragment.
     * <p>
     * Initializes input fields, date pickers, time pickers, and create button listeners.
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
        locationField = view.findViewById(R.id.input_location);
        latField = view.findViewById(R.id.input_latitude);
        lonField = view.findViewById(R.id.input_longitude);
        geoConsentSwitch = view.findViewById(R.id.check_geo_consent);
        createBtn = view.findViewById(R.id.btn_create_event);
        maxEntrantsField = view.findViewById(R.id.input_max_entrants);
        posterUrlField = view.findViewById(R.id.input_poster_url);
        inputEventStart = view.findViewById(R.id.input_event_start);
        inputEventEnd = view.findViewById(R.id.input_event_end);
        inputTimeStart = view.findViewById(R.id.input_time_start);
        inputTimeEnd = view.findViewById(R.id.input_time_end);
        inputRegOpen = view.findViewById(R.id.input_reg_open);
        inputRegClose = view.findViewById(R.id.input_reg_close);
        daysToggleGroup = view.findViewById(R.id.days_toggle_group);

        db = FirebaseFirestore.getInstance();

        // Setup date pickers
        inputEventStart.setOnClickListener(v -> showDatePicker(inputEventStart));
        inputEventEnd.setOnClickListener(v -> showDatePicker(inputEventEnd));
        inputRegOpen.setOnClickListener(v -> showDatePicker(inputRegOpen));
        inputRegClose.setOnClickListener(v -> showDatePicker(inputRegClose));

        // Setup time pickers
        inputTimeStart.setOnClickListener(v -> showTimePicker(inputTimeStart));
        inputTimeEnd.setOnClickListener(v -> showTimePicker(inputTimeEnd));

        // Setup days of week toggle
        setupDaySelection();

        // Setup create event button
        createBtn.setOnClickListener(v -> createEvent());

        return view;
    }

    /**
     * Setup day toggle buttons - matching filter popup pattern
     */
    private void setupDaySelection() {
        daysToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            MaterialButton button = group.findViewById(checkedId);
            if (button != null) {
                String label = button.getText().toString();
                if (isChecked) {
                    if (!selectedDays.contains(label)) {
                        selectedDays.add(label);
                    }
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.day_selected));
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_text_selected));
                } else {
                    selectedDays.remove(label);
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.day_unselected));
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_text_unselected));
                }
            }
        });

        // Initialize buttons color
        for (int i = 0; i < daysToggleGroup.getChildCount(); i++) {
            View child = daysToggleGroup.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) child;
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.day_unselected));
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_text_unselected));
            }
        }
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
        String location = locationField.getText().toString().trim();
        String latStr = latField.getText().toString().trim();
        String lonStr = lonField.getText().toString().trim();
        String maxEntrantsStr = maxEntrantsField.getText().toString().trim();
        String posterUrl = posterUrlField.getText().toString().trim();

        // Validate required fields
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getContext(), "Event name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(capStr)) {
            Toast.makeText(getContext(), "Number of participants is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            Toast.makeText(getContext(), "Event description is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(location)) {
            Toast.makeText(getContext(), "Event location is required", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = Integer.parseInt(capStr);

        // Default coordinates if not provided (can be enhanced with geocoding)
        double lat = TextUtils.isEmpty(latStr) ? 0.0 : Double.parseDouble(latStr);
        double lon = TextUtils.isEmpty(lonStr) ? 0.0 : Double.parseDouble(lonStr);
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
        eventData.put("location", location);
        eventData.put("geoLocation", new GeoPoint(lat, lon));
        eventData.put("createdAt", now);
        eventData.put("eventStartAt", eventStart);
        eventData.put("eventEndAt", eventEnd);
        eventData.put("registrationOpensAt", regOpen);
        eventData.put("registrationClosesAt", regClose);
        eventData.put("geoConsent", geoConsentSwitch.isChecked());
        eventData.put("entrantsApplied", 0);
        eventData.put("maxEntrants", maxEntrants);
        eventData.put("daysOfWeek", new ArrayList<>(selectedDays));

        // Store poster URL if provided
        if (!TextUtils.isEmpty(posterUrl)) {
            eventData.put("posterUrl", posterUrl);
        }

        // Store time information if provided
        String timeStart = inputTimeStart.getText().toString();
        String timeEnd = inputTimeEnd.getText().toString();
        if (!TextUtils.isEmpty(timeStart)) {
            eventData.put("timeStart", timeStart);
        }
        if (!TextUtils.isEmpty(timeEnd)) {
            eventData.put("timeEnd", timeEnd);
        }

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

    /**
     * Shows a {@link TimePickerDialog} and sets the selected time on the target EditText.
     *
     * @param target EditText to populate with the selected time.
     */
    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    target.setText(timeStr);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // 24-hour format
        );
        dialog.show();
    }
}
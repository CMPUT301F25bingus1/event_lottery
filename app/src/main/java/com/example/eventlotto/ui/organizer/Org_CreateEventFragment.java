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

public class Org_CreateEventFragment extends Fragment {

    // Input fields
    private EditText titleField, descField, capacityField, latField, lonField;
    private EditText inputEventStart, inputEventEnd, inputRegOpen, inputRegClose;
    private CheckBox geoConsentBox, notifyBox;
    private Button createBtn;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_events, container, false);

        // Basic fields
        titleField = view.findViewById(R.id.input_event_title);
        descField = view.findViewById(R.id.input_description);
        capacityField = view.findViewById(R.id.input_capacity);
        latField = view.findViewById(R.id.input_latitude);
        lonField = view.findViewById(R.id.input_longitude);
        geoConsentBox = view.findViewById(R.id.check_geo_consent);
        notifyBox = view.findViewById(R.id.check_notify);
        createBtn = view.findViewById(R.id.btn_create_event);

        // Date fields
        inputEventStart = view.findViewById(R.id.input_event_start);
        inputEventEnd = view.findViewById(R.id.input_event_end);
        inputRegOpen = view.findViewById(R.id.input_reg_open);
        inputRegClose = view.findViewById(R.id.input_reg_close);

        db = FirebaseFirestore.getInstance();

        inputEventStart.setOnClickListener(v -> showDatePicker(inputEventStart));
        inputEventEnd.setOnClickListener(v -> showDatePicker(inputEventEnd));
        inputRegOpen.setOnClickListener(v -> showDatePicker(inputRegOpen));
        inputRegClose.setOnClickListener(v -> showDatePicker(inputRegClose));

        createBtn.setOnClickListener(v -> createEvent());

        return view;
    }

    private void createEvent() {
        // Organizer reference — assuming you have a "users" collection
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        DocumentReference organizerRef = db.collection("users").document(deviceId);

        // Read inputs
        String title = titleField.getText().toString().trim();
        String desc = descField.getText().toString().trim();
        String capStr = capacityField.getText().toString().trim();
        String latStr = latField.getText().toString().trim();
        String lonStr = lonField.getText().toString().trim();

        // Simple validation
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc) ||
                TextUtils.isEmpty(capStr) || TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lonStr)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = Integer.parseInt(capStr);
        double lat = Double.parseDouble(latStr);
        double lon = Double.parseDouble(lonStr);

        Timestamp now = Timestamp.now();

        // Parse date fields if they’re filled (otherwise default to now)
        Timestamp eventStart = parseDateToTimestamp(inputEventStart.getText().toString(), now);
        Timestamp eventEnd = parseDateToTimestamp(inputEventEnd.getText().toString(), now);
        Timestamp regOpen = parseDateToTimestamp(inputRegOpen.getText().toString(), now);
        Timestamp regClose = parseDateToTimestamp(inputRegClose.getText().toString(), now);

        // Build event map
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("organizerId", organizerRef); // 🔥 store as DocumentReference
        eventData.put("eventTitle", title);
        eventData.put("description", desc);
        eventData.put("capacity", capacity);
        eventData.put("createdAt", now);
        eventData.put("eventStartAt", eventStart);
        eventData.put("eventEndAt", eventEnd);
        eventData.put("registrationOpensAt", regOpen);
        eventData.put("registrationClosesAt", regClose);
        eventData.put("geoConsent", geoConsentBox.isChecked());
        eventData.put("notifyWhenNotSelected", notifyBox.isChecked());
        eventData.put("location", new GeoPoint(lat, lon));

        // Save event
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

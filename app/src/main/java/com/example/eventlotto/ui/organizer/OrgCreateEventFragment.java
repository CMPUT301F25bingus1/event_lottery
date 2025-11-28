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
import android.widget.ImageView;
import android.widget.TextView;
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

public class OrgCreateEventFragment extends Fragment {

    private EditText titleField;
    private EditText descField;
    private EditText urlField;
    private EditText capacityField;
    private EditText locationField;
    private EditText maxEntrantsField;
    private EditText inputEventStart;
    private EditText inputEventEnd;
    private EditText inputTimeStart;
    private EditText inputTimeEnd;
    private EditText inputRegOpen;
    private EditText inputRegClose;

    private MaterialButtonToggleGroup daysToggleGroup;
    private final List<String> selectedDays = new ArrayList<>();

    private SwitchCompat geoConsentSwitch;
    private Button createBtn;

    private FirebaseFirestore db;

    private ImageView geoInfoButton;
    private TextView geoInfoText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_events, container, false);

        titleField = view.findViewById(R.id.input_event_title);
        descField = view.findViewById(R.id.input_description);
        urlField = view.findViewById(R.id.input_event_url);
        capacityField = view.findViewById(R.id.input_capacity);
        locationField = view.findViewById(R.id.input_location);
        geoConsentSwitch = view.findViewById(R.id.check_geo_consent);
        createBtn = view.findViewById(R.id.btn_create_event);
        maxEntrantsField = view.findViewById(R.id.input_max_entrants);
        inputEventStart = view.findViewById(R.id.input_event_start);
        inputEventEnd = view.findViewById(R.id.input_event_end);
        inputTimeStart = view.findViewById(R.id.input_time_start);
        inputTimeEnd = view.findViewById(R.id.input_time_end);
        inputRegOpen = view.findViewById(R.id.input_reg_open);
        inputRegClose = view.findViewById(R.id.input_reg_close);
        daysToggleGroup = view.findViewById(R.id.days_toggle_group);

        geoInfoButton = view.findViewById(R.id.info_geo);
        geoInfoText = view.findViewById(R.id.txt_geo_info);

        db = FirebaseFirestore.getInstance();

        geoInfoButton.setOnClickListener(v -> {
            if (geoInfoText.getVisibility() == View.GONE) {
                geoInfoText.setVisibility(View.VISIBLE);
            } else {
                geoInfoText.setVisibility(View.GONE);
            }
        });

        inputEventStart.setOnClickListener(v -> showDatePicker(inputEventStart));
        inputEventEnd.setOnClickListener(v -> showDatePicker(inputEventEnd));
        inputRegOpen.setOnClickListener(v -> showDatePicker(inputRegOpen));
        inputRegClose.setOnClickListener(v -> showDatePicker(inputRegClose));

        inputTimeStart.setOnClickListener(v -> showTimePicker(inputTimeStart));
        inputTimeEnd.setOnClickListener(v -> showTimePicker(inputTimeEnd));

        setupDaySelection();

        createBtn.setOnClickListener(v -> createEvent());

        return view;
    }

    private void setupDaySelection() {
        daysToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            MaterialButton button = group.findViewById(checkedId);
            if (button != null) {
                String label = button.getText().toString();
                if (isChecked) {
                    if (!selectedDays.contains(label)) selectedDays.add(label);
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.day_selected));
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_text_selected));
                } else {
                    selectedDays.remove(label);
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.day_unselected));
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_text_unselected));
                }
            }
        });

        for (int i = 0; i < daysToggleGroup.getChildCount(); i++) {
            View child = daysToggleGroup.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) child;
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.day_unselected));
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_text_unselected));
            }
        }
    }

    private void createEvent() {
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        DocumentReference organizerRef = db.collection("users").document(deviceId);

        String title = titleField.getText().toString().trim();
        String desc = descField.getText().toString().trim();
        String eventUrl = urlField.getText().toString().trim();
        String capStr = capacityField.getText().toString().trim();
        String location = locationField.getText().toString().trim();
        String maxEntrantsStr = maxEntrantsField.getText().toString().trim();

        if (TextUtils.isEmpty(title)) { showToast("Event name is required"); return; }
        if (TextUtils.isEmpty(capStr)) { showToast("Number of participants is required"); return; }
        if (TextUtils.isEmpty(desc)) { showToast("Event description is required"); return; }
        if (TextUtils.isEmpty(location)) { showToast("Event location coordinates are required"); return; }

        int capacity = Integer.parseInt(capStr);

        double lat;
        double lon;

        if (!location.contains(",")) {
            showToast("Format: xxx.xxxx, xxx.xxxx");
            return;
        }

        String[] parts = location.split(",", 2);
        try {
            lat = Double.parseDouble(parts[0].trim());
            lon = Double.parseDouble(parts[1].trim());
        } catch (Exception e) {
            showToast("Invalid coordinates. Format: xxx.xxxx, xxx.xxxx");
            return;
        }
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
        if (!TextUtils.isEmpty(eventUrl)) eventData.put("eventURL", eventUrl);
        eventData.put("capacity", capacity);

        eventData.put("location", new GeoPoint(lat, lon)); // changed
        eventData.put("geoLocation", new GeoPoint(lat, lon)); // added

        eventData.put("createdAt", now);
        eventData.put("eventStartAt", eventStart);
        eventData.put("eventEndAt", eventEnd);
        eventData.put("registrationOpensAt", regOpen);
        eventData.put("registrationClosesAt", regClose);
        eventData.put("geoConsent", geoConsentSwitch.isChecked());
        eventData.put("entrantsApplied", 0);
        eventData.put("maxEntrants", maxEntrants);
        eventData.put("daysOfWeek", new ArrayList<>(selectedDays));

        String timeStart = inputTimeStart.getText().toString();
        String timeEnd = inputTimeEnd.getText().toString();
        if (!TextUtils.isEmpty(timeStart)) eventData.put("timeStart", timeStart);
        if (!TextUtils.isEmpty(timeEnd)) eventData.put("timeEnd", timeEnd);

        db.collection("events")
                .add(eventData)
                .addOnSuccessListener(docRef -> {
                    String eventId = docRef.getId();
                    db.collection("events").document(eventId).update("eventId", eventId);
                    showToast("Event created successfully!");
                    GenerateQRFragment.newInstance(eventId)
                            .show(getParentFragmentManager(), "generate_qr_dialog");
                })
                .addOnFailureListener(e -> showToast("Error: " + e.getMessage()));
    }

    private Timestamp parseDateToTimestamp(String dateStr, Timestamp fallback) {
        if (TextUtils.isEmpty(dateStr)) return fallback;

        try {
            String[] parts = dateStr.split("/");
            if (parts.length != 3) return fallback;

            int year = Integer.parseInt(parts[2]);
            int month = Integer.parseInt(parts[0]) - 1;
            int day = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, 0, 0, 0);
            return new Timestamp(calendar.getTime());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                R.style.CustomDatePicker,
                (view, year, month, dayOfMonth) -> target.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", month + 1, dayOfMonth, year)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                R.style.CustomDatePicker,
                (view, hourOfDay, minute) -> target.setText(
                        String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}

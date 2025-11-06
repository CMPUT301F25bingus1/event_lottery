package com.example.eventlotto.ui.entrant;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.eventlotto.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Filter popup for HomeFragment with day selection, registration status, date ranges, and location.
 */
public class Ent_FilterFragment extends DialogFragment {

    private MaterialButtonToggleGroup daysToggleGroup;
    private List<String> selectedDays = new ArrayList<>();
    private RadioGroup registrationStatusGroup;
    private EditText eventDateFrom, eventDateTo, registrationFrom, registrationTo, locationInput;

    /** Interface to send filters back to HomeFragment */
    public interface OnFilterAppliedListener {
        void onFilterApplied(FilterCriteria criteria);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter_search_popup, container, false);

        // Bind views
        eventDateFrom = view.findViewById(R.id.event_date_from);
        eventDateTo = view.findViewById(R.id.event_date_to);
        registrationFrom = view.findViewById(R.id.registration_from);
        registrationTo = view.findViewById(R.id.registration_to);
        locationInput = view.findViewById(R.id.location_input);
        daysToggleGroup = view.findViewById(R.id.days_toggle_group);
        registrationStatusGroup = view.findViewById(R.id.registration_status_group);

        // Date picker setup
        View.OnClickListener dateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker((EditText) v);
            }
        };
        eventDateFrom.setOnClickListener(dateClickListener);
        eventDateTo.setOnClickListener(dateClickListener);
        registrationFrom.setOnClickListener(dateClickListener);
        registrationTo.setOnClickListener(dateClickListener);

        // Day toggle buttons
        setupDaySelection();

        // Apply filters button
        view.findViewById(R.id.btn_apply_filters).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterCriteria criteria = collectFilters();
                applyFiltersToHome(criteria);
                dismiss();
            }
        });

        return view;
    }

    /** Day toggle buttons with colors */
    private void setupDaySelection() {
        daysToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                MaterialButton button = (MaterialButton) group.findViewById(checkedId);
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
            }
        });

        // Initialize all buttons
        for (int i = 0; i < daysToggleGroup.getChildCount(); i++) {
            View child = daysToggleGroup.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) child;
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.day_unselected));
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_text_unselected));
            }
        }
    }

    /** Shows a date picker with your custom theme */
    private void showDatePicker(final EditText target) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                R.style.CustomDatePicker, // keeps your custom colors
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int y, int m, int d) {
                        target.setText(String.format("%02d/%02d/%04d", d, m + 1, y));
                    }
                },
                year, month, day
        );

        datePicker.show();
    }

    /** Collect filter inputs into a FilterCriteria object */
    private FilterCriteria collectFilters() {
        String eventFrom = eventDateFrom.getText().toString().trim();
        String eventTo = eventDateTo.getText().toString().trim();
        String regFrom = registrationFrom.getText().toString().trim();
        String regTo = registrationTo.getText().toString().trim();
        String location = locationInput.getText().toString().trim();

        String regStatus = null;
        if (registrationStatusGroup.getCheckedRadioButtonId() != -1) {
            RadioButton selected = registrationStatusGroup.findViewById(registrationStatusGroup.getCheckedRadioButtonId());
            if (selected != null) regStatus = selected.getText().toString().toLowerCase();
        }

        return new FilterCriteria(eventFrom, eventTo, regFrom, regTo, new ArrayList<>(selectedDays), regStatus, location);
    }

    /** Sends filter data back to HomeFragment */
    private void applyFiltersToHome(FilterCriteria criteria) {
        Fragment parent = getParentFragmentManager().findFragmentById(R.id.fragment_container);
        if (parent instanceof OnFilterAppliedListener) {
            ((OnFilterAppliedListener) parent).onFilterApplied(criteria);
        }
    }

    /** Filter data container */
    public static class FilterCriteria {
        public String eventDateFrom, eventDateTo, registrationFrom, registrationTo, registrationStatus, location;
        public List<String> daysOfWeek;

        public FilterCriteria(String eventDateFrom, String eventDateTo,
                              String registrationFrom, String registrationTo,
                              List<String> daysOfWeek, String registrationStatus,
                              String location) {
            this.eventDateFrom = eventDateFrom;
            this.eventDateTo = eventDateTo;
            this.registrationFrom = registrationFrom;
            this.registrationTo = registrationTo;
            this.daysOfWeek = daysOfWeek;
            this.registrationStatus = registrationStatus;
            this.location = location;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}

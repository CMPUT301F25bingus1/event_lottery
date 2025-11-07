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

import com.example.eventlotto.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Filter popup for HomeFragment.
 * Keeps your previous UI but updated for the new Home fragment.
 */
public class Ent_FilterFragment extends DialogFragment {

    private MaterialButtonToggleGroup daysToggleGroup;
    private List<String> selectedDays = new ArrayList<>();
    private RadioGroup registrationStatusGroup;
    private EditText eventDateFrom, eventDateTo, registrationFrom, registrationTo, locationInput;

    /** Listener to send filters back to HomeFragment */
    public interface OnFilterAppliedListener {
        void onFilterApplied(String eventDateFrom, String eventDateTo,
                             String registrationFrom, String registrationTo,
                             List<String> selectedDays);
    }

    private OnFilterAppliedListener filterListener;

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.filterListener = listener;
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

        // Date pickers
        View.OnClickListener dateClickListener = v -> showDatePicker((EditText) v);
        eventDateFrom.setOnClickListener(dateClickListener);
        eventDateTo.setOnClickListener(dateClickListener);
        registrationFrom.setOnClickListener(dateClickListener);
        registrationTo.setOnClickListener(dateClickListener);

        // Days toggle buttons
        setupDaySelection();

        // Apply filters button
        view.findViewById(R.id.btn_apply_filters).setOnClickListener(v -> {
            if (filterListener != null) {
                filterListener.onFilterApplied(
                        eventDateFrom.getText().toString().trim(),
                        eventDateTo.getText().toString().trim(),
                        registrationFrom.getText().toString().trim(),
                        registrationTo.getText().toString().trim(),
                        new ArrayList<>(selectedDays)
                );
            }
            dismiss();
        });

        return view;
    }

    /** Setup day toggle buttons */
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

    /** Show date picker dialog */
    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                R.style.CustomDatePicker,
                (view, y, m, d) -> target.setText(String.format("%02d/%02d/%04d", d, m + 1, y)),
                year, month, day
        );

        datePicker.show();
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

package com.example.eventlotto.ui.entrant;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

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

public class Ent_FilterFragment extends DialogFragment {

    private MaterialButtonToggleGroup daysToggleGroup;
    private final List<String> selectedDays = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter_search_popup, container, false);

        // Date fields
        EditText eventDateFrom = view.findViewById(R.id.event_date_from);
        EditText eventDateTo = view.findViewById(R.id.event_date_to);
        EditText registrationFrom = view.findViewById(R.id.registration_from);
        EditText registrationTo = view.findViewById(R.id.registration_to);

        View.OnClickListener dateClickListener = v -> showDatePicker((EditText) v);
        eventDateFrom.setOnClickListener(dateClickListener);
        eventDateTo.setOnClickListener(dateClickListener);
        registrationFrom.setOnClickListener(dateClickListener);
        registrationTo.setOnClickListener(dateClickListener);

        // Setup day toggle buttons
        daysToggleGroup = view.findViewById(R.id.days_toggle_group);
        setupDaySelection(view);

        // Apply filters button
        view.findViewById(R.id.btn_apply_filters).setOnClickListener(v -> {
            String days = String.join(", ", selectedDays);
            Toast.makeText(getContext(),
                    "Filters applied!\nSelected days: " + days,
                    Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    private void setupDaySelection(View root) {
        daysToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            MaterialButton button = group.findViewById(checkedId);
            if (button != null) {
                String label = button.getText().toString();

                if (isChecked) {
                    // Add day and apply highlight color
                    if (!selectedDays.contains(label)) selectedDays.add(label);
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.day_selected));
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_text_selected));
                } else {
                    // Remove day and revert to default
                    selectedDays.remove(label);
                    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.day_unselected));
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_text_unselected));
                }
            }
        });

        // Initialize all buttons to default color
        for (int i = 0; i < daysToggleGroup.getChildCount(); i++) {
            View child = daysToggleGroup.getChildAt(i);
            if (child instanceof MaterialButton) {
                ((MaterialButton) child).setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.day_unselected)
                );
                ((MaterialButton) child).setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.day_text_unselected)
                );
            }
        }
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                R.style.CustomDatePicker, // Your theme
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
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}

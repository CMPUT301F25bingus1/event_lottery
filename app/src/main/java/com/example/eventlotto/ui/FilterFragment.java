package com.example.eventlotto.ui;

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
import androidx.fragment.app.DialogFragment;

import com.example.eventlotto.R;

import java.util.Calendar;

public class FilterFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter_search_popup, container, false);

        EditText eventDateFrom = view.findViewById(R.id.event_date_from);
        EditText eventDateTo = view.findViewById(R.id.event_date_to);
        EditText registrationFrom = view.findViewById(R.id.registration_from);
        EditText registrationTo = view.findViewById(R.id.registration_to);

        View.OnClickListener dateClickListener = v -> showDatePicker((EditText) v);
        eventDateFrom.setOnClickListener(dateClickListener);
        eventDateTo.setOnClickListener(dateClickListener);
        registrationFrom.setOnClickListener(dateClickListener);
        registrationTo.setOnClickListener(dateClickListener);

        view.findViewById(R.id.btn_apply_filters).setOnClickListener(v -> {
            // TODO: Later implement actual filter logic
            Toast.makeText(getContext(), "Filters applied!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, y, m, d) -> target.setText(d + "/" + (m + 1) + "/" + y),
                year, month, day);
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

package com.example.eventlotto.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotto.R;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Initialize UI elements ---
        ImageButton filterButton = view.findViewById(R.id.filter_button);
        ImageView searchIcon = view.findViewById(R.id.search_button);
        EditText searchEditText = view.findViewById(R.id.search_edit_text);

        // --- Handle Search icon click (optional placeholder) ---
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Search icon clicked", Toast.LENGTH_SHORT).show());
        }

        // --- Handle Filter button click: show FilterFragment popup ---
        if (filterButton != null) {
            filterButton.setOnClickListener(v -> {
                FilterFragment filterFragment = new FilterFragment();
                filterFragment.show(getParentFragmentManager(), "filter_fragment");
            });
        }

        // --- Optional: Handle text input search (for later functionality) ---
        if (searchEditText != null) {
            searchEditText.setOnEditorActionListener((v1, actionId, event) -> {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    Toast.makeText(getContext(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
                    // TODO: Implement search functionality later
                }
                return true;
            });
        }

        return view;
    }
}

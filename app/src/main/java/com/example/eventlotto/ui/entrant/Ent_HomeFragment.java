package com.example.eventlotto.ui.entrant;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.example.eventlotto.functions.events.EventAdapter;
import com.example.eventlotto.functions.events.EventDetailsFragment;
import com.example.eventlotto.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Ent_HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private List<Event> fullEventList; // Keep full list for search filtering
    private FirestoreService firestoreService;
    private final Map<String, String> statusByEid = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize FirestoreService
        firestoreService = new FirestoreService();

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_events);
        eventList = new ArrayList<>();
        fullEventList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(eventList, event -> {
            // Show the EventDetailsFragment as a dialog
            EventDetailsFragment fragment = EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });

        recyclerView.setAdapter(adapter);

        // Fetch events from Firestore
        fetchEvents();

        // --- Initialize UI elements ---
        ImageButton filterButton = view.findViewById(R.id.filter_button);
        ImageView searchIcon = view.findViewById(R.id.search_button);
        EditText searchEditText = view.findViewById(R.id.search_edit_text);

        // --- Handle Filter button click ---
        if (filterButton != null) {
            filterButton.setOnClickListener(v -> {
                Ent_FilterFragment entFilterFragment = new Ent_FilterFragment();
                entFilterFragment.show(getParentFragmentManager(), "filter_fragment");
            });
        }

        // --- Handle Search icon click (optional feedback) ---
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> {
                String query = searchEditText.getText().toString().trim();
                if (query.isEmpty()) {
                    Toast.makeText(getContext(), "Type something to search", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // --- Live Search Functionality ---
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEvents(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // --- Help popup setup ---
        View popup = view.findViewById(R.id.selection_popup);
        View gotItButton = view.findViewById(R.id.btn_got_it);
        View helpRow = view.findViewById(R.id.help_row);

        if (helpRow != null) {
            helpRow.setOnClickListener(v2 -> {
                if (popup != null) popup.setVisibility(View.VISIBLE);
            });
        }

        if (gotItButton != null) {
            gotItButton.setOnClickListener(v3 -> {
                if (popup != null) popup.setVisibility(View.GONE);
            });
        }

        return view;
    }

    /**
     * Fetch events from Firestore and store them in both eventList and fullEventList.
     */
    private void fetchEvents() {
        firestoreService.events()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    fullEventList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEid(doc.getId()); // set Firestore document ID
                            eventList.add(event);
                            fullEventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        getContext(),
                        "Error fetching events: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
    }

    /**
     * Filters the event list based on a search query.
     */
    private void filterEvents(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setEvents(new ArrayList<>(fullEventList)); // restore full list
            return;
        }

        String lowerQuery = query.toLowerCase(Locale.ROOT);
        List<Event> filteredList = new ArrayList<>();

        for (Event event : fullEventList) {
            String title = event.getEventTitle() != null ? event.getEventTitle().toLowerCase(Locale.ROOT) : "";
            String description = event.getDescription() != null ? event.getDescription().toLowerCase(Locale.ROOT) : "";

            if (title.contains(lowerQuery) || description.contains(lowerQuery)) {
                filteredList.add(event);
            }
        }

        adapter.setEvents(filteredList);
    }
}

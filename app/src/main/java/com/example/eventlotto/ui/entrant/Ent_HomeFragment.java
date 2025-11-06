package com.example.eventlotto.ui.entrant;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.example.eventlotto.functions.events.EventAdapter;
import com.example.eventlotto.model.Event;
import com.example.eventlotto.functions.events.EventDetailsFragment;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Ent_HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize FirestoreService
        firestoreService = new FirestoreService();

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_events);
        eventList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(eventList, event -> {
            // Show the EventDetailsFragment as a dialog
            EventDetailsFragment fragment = EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });

        recyclerView.setAdapter(adapter);


        recyclerView.setAdapter(adapter);

        // Fetch events from Firestore
        fetchEvents();

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
                Ent_FilterFragment entFilterFragment = new Ent_FilterFragment();
                entFilterFragment.show(getParentFragmentManager(), "filter_fragment");
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

    private void fetchEvents() {
        firestoreService.events()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEid(doc.getId()); // set Firestore document ID
                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}

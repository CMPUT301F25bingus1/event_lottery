package com.example.eventlotto.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Adm_HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private List<Event> fullEventList;
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firestoreService = new FirestoreService();

        recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        fullEventList = new ArrayList<>();

        adapter = new EventAdapter(eventList, event -> {
            Adm_EventDetailsFragment fragment =
                    Adm_EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "admin_event_details");
        });

        recyclerView.setAdapter(adapter);

        fetchEvents();

        ImageButton filterButton = view.findViewById(R.id.filter_button);
        EditText search = view.findViewById(R.id.search_edit_text);
        View popup = view.findViewById(R.id.selection_popup);
        View gotIt = view.findViewById(R.id.btn_got_it);
        View helpRow = view.findViewById(R.id.help_row);

        // Search
        if (search != null) {
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEvents(s.toString());
                }
            });
        }

        // Help popup
        if (helpRow != null)
            helpRow.setOnClickListener(v -> popup.setVisibility(View.VISIBLE));

        if (gotIt != null)
            gotIt.setOnClickListener(v -> popup.setVisibility(View.GONE));

        return view;
    }

    private void fetchEvents() {
        firestoreService.events().get()
                .addOnSuccessListener(query -> {
                    eventList.clear();
                    fullEventList.clear();

                    for (DocumentSnapshot doc : query) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEid(doc.getId());
                            eventList.add(event);
                            fullEventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show());
    }

    private void filterEvents(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setEvents(new ArrayList<>(fullEventList));
            return;
        }

        String lower = query.toLowerCase(Locale.ROOT);
        List<Event> filtered = new ArrayList<>();

        for (Event e : fullEventList) {
            String title = e.getEventTitle() != null ? e.getEventTitle().toLowerCase() : "";
            String desc = e.getDescription() != null ? e.getDescription().toLowerCase() : "";

            if (title.contains(lower) || desc.contains(lower))
                filtered.add(e);
        }

        adapter.setEvents(filtered);
    }
}

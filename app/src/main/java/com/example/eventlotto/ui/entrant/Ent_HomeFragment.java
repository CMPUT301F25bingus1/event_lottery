package com.example.eventlotto.ui.entrant;

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
import com.example.eventlotto.functions.events.EventDetailsFragment;
import com.example.eventlotto.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Entrant Home Fragment.
 * <p>
 * Displays all available events, allows searching by title/description,
 * and supports filtering by date range, day of week, and other criteria
 * via {@link Ent_FilterFragment}.
 */
public class Ent_HomeFragment extends Fragment implements Ent_FilterFragment.OnFilterAppliedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> fullEventList = new ArrayList<>();
    private FirestoreService firestoreService;

    /**
     * Called to create the view hierarchy for this fragment.
     *
     * @param inflater           LayoutInflater to inflate views
     * @param container          Optional parent view
     * @param savedInstanceState Saved instance data
     * @return The root view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firestoreService = new FirestoreService();
        recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter and click listener
        adapter = new EventAdapter(new ArrayList<>(), event -> {
            EventDetailsFragment fragment = EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });
        recyclerView.setAdapter(adapter);

        fetchEvents();

        // Filter button opens the filter popup
        ImageButton filterButton = view.findViewById(R.id.filter_button);
        if (filterButton != null) {
            filterButton.setOnClickListener(v -> {
                Ent_FilterFragment filterFragment = new Ent_FilterFragment();
                filterFragment.show(getParentFragmentManager(), "filter_fragment");
            });
        }

        // Search box for live title/description filtering
        EditText searchEditText = view.findViewById(R.id.search_edit_text);
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEventsByQuery(s.toString());
                }
            });
        }

        return view;
    }

    /**
     * Fetches all events from Firestore and updates the RecyclerView.
     */
    private void fetchEvents() {
        firestoreService.events().get()
                .addOnSuccessListener(querySnapshot -> {
                    fullEventList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEid(doc.getId());
                            fullEventList.add(event);
                        }
                    }
                    adapter.setEvents(new ArrayList<>(fullEventList));
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Error fetching events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Filters events based on a text query in title or description.
     *
     * @param query The search query entered by the user
     */
    private void filterEventsByQuery(String query) {
        if (query == null || query.isEmpty()) {
            adapter.setEvents(new ArrayList<>(fullEventList));
            return;
        }

        String q = query.toLowerCase(Locale.ROOT);
        List<Event> filtered = new ArrayList<>();

        for (Event e : fullEventList) {
            if ((e.getEventTitle() != null && e.getEventTitle().toLowerCase(Locale.ROOT).contains(q)) ||
                    (e.getDescription() != null && e.getDescription().toLowerCase(Locale.ROOT).contains(q))) {
                filtered.add(e);
            }
        }

        adapter.setEvents(filtered);
    }

    /**
     * Called when filters are applied in the {@link Ent_FilterFragment}.
     * Filters events based on event date range, selected days, etc.
     *
     * @param criteria Filter criteria provided by the filter dialog
     */
    @Override
    public void onFilterApplied(Ent_FilterFragment.FilterCriteria criteria) {
        List<Event> filtered = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        for (Event e : fullEventList) {
            boolean matches = true;

            try {
                // Parse filter and event dates
                Date filterFrom = criteria.eventDateFrom.isEmpty() ? null : sdf.parse(criteria.eventDateFrom);
                Date filterTo = criteria.eventDateTo.isEmpty() ? null : sdf.parse(criteria.eventDateTo);
                Date eventStart = (e.getEventStartAt() != null) ? e.getEventStartAt().toDate() : null;
                Date eventEnd = (e.getEventEndAt() != null) ? e.getEventEndAt().toDate() : null;

                // Inclusive date range check: include events that overlap the filter range
                if (filterFrom != null && filterTo != null && eventStart != null && eventEnd != null) {
                    if (eventEnd.before(filterFrom) || eventStart.after(filterTo)) {
                        matches = false; // no overlap
                    }
                } else if (filterFrom != null && eventEnd != null && eventEnd.before(filterFrom)) {
                    matches = false;
                } else if (filterTo != null && eventStart != null && eventStart.after(filterTo)) {
                    matches = false;
                }

            } catch (ParseException ignored) {}

            // Filter by selected days of the week
            if (criteria.daysOfWeek != null && !criteria.daysOfWeek.isEmpty() && e.getEventStartAt() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(e.getEventStartAt().toDate());
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

                String dayLetter = "";
                switch (dayOfWeek) {
                    case Calendar.MONDAY: dayLetter = "M"; break;
                    case Calendar.TUESDAY: dayLetter = "T"; break;
                    case Calendar.WEDNESDAY: dayLetter = "W"; break;
                    case Calendar.THURSDAY: dayLetter = "T"; break;
                    case Calendar.FRIDAY: dayLetter = "F"; break;
                    case Calendar.SATURDAY: dayLetter = "S"; break;
                    case Calendar.SUNDAY: dayLetter = "S"; break;
                }

                if (!criteria.daysOfWeek.contains(dayLetter)) {
                    matches = false;
                }
            }

            // TODO: Add registration status and location filters if needed

            if (matches) {
                filtered.add(e);
            }
        }

        adapter.setEvents(filtered);
    }
}

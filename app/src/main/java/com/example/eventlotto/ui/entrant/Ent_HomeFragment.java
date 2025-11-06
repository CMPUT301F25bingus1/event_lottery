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

public class Ent_HomeFragment extends Fragment implements Ent_FilterFragment.OnFilterAppliedListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> fullEventList = new ArrayList<>();
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firestoreService = new FirestoreService();
        recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(new ArrayList<Event>(), event -> {
            EventDetailsFragment fragment = EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });
        recyclerView.setAdapter(adapter);

        fetchEvents();

        // Filter button
        ImageButton filterButton = view.findViewById(R.id.filter_button);
        if (filterButton != null) {
            filterButton.setOnClickListener(v -> {
                Ent_FilterFragment filterFragment = new Ent_FilterFragment();
                filterFragment.show(getParentFragmentManager(), "filter_fragment");
            });
        }

        // Search live filtering
        EditText searchEditText = view.findViewById(R.id.search_edit_text);
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterBySearch(s.toString());
                }
            });
        }

        return view;
    }

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

    private void filterBySearch(String query) {
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

    @Override
    public void onFilterApplied(Ent_FilterFragment.FilterCriteria criteria) {
        List<Event> filtered = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        for (Event e : fullEventList) {
            boolean matches = true;

            // Event date filter
            try {
                if (criteria.eventDateFrom != null && !criteria.eventDateFrom.isEmpty()) {
                    Date from = sdf.parse(criteria.eventDateFrom);
                    if (e.getEventStartAt() != null && e.getEventStartAt().toDate().before(from)) matches = false;
                }
                if (criteria.eventDateTo != null && !criteria.eventDateTo.isEmpty()) {
                    Date to = sdf.parse(criteria.eventDateTo);
                    if (e.getEventEndAt() != null && e.getEventEndAt().toDate().after(to)) matches = false;
                }
            } catch (ParseException ignored) {}

            // Registration date filter
            try {
                if (criteria.registrationFrom != null && !criteria.registrationFrom.isEmpty()) {
                    Date regFrom = sdf.parse(criteria.registrationFrom);
                    if (e.getRegistrationOpensAt() != null && e.getRegistrationOpensAt().toDate().before(regFrom)) matches = false;
                }
                if (criteria.registrationTo != null && !criteria.registrationTo.isEmpty()) {
                    Date regTo = sdf.parse(criteria.registrationTo);
                    if (e.getRegistrationClosesAt() != null && e.getRegistrationClosesAt().toDate().after(regTo)) matches = false;
                }
            } catch (ParseException ignored) {}

            // Days of week filter
            if (criteria.daysOfWeek != null && !criteria.daysOfWeek.isEmpty() && e.getEventStartAt() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(e.getEventStartAt().toDate());
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday
                String dayLetter = "";
                switch (dayOfWeek) {
                    case Calendar.MONDAY:    dayLetter = "M"; break;
                    case Calendar.TUESDAY:   dayLetter = "T"; break;
                    case Calendar.WEDNESDAY: dayLetter = "W"; break;
                    case Calendar.THURSDAY:  dayLetter = "T"; break;
                    case Calendar.FRIDAY:    dayLetter = "F"; break;
                    case Calendar.SATURDAY:  dayLetter = "S"; break;
                    case Calendar.SUNDAY:    dayLetter = "S"; break;
                }
                if (!criteria.daysOfWeek.contains(dayLetter)) matches = false;
            }

            // Registration status
            Date now = new Date();
            if ("open".equals(criteria.registrationStatus)) {
                if (e.getRegistrationOpensAt() != null && e.getRegistrationClosesAt() != null) {
                    if (now.before(e.getRegistrationOpensAt().toDate()) || now.after(e.getRegistrationClosesAt().toDate()))
                        matches = false;
                }
            } else if ("future".equals(criteria.registrationStatus)) {
                if (e.getEventStartAt() != null && !e.getEventStartAt().toDate().after(now)) matches = false;
            }

            // Location
            if (criteria.location != null && !criteria.location.isEmpty() && e.getLocation() != null) {
                String loc = e.getLocation().getLatitude() + "," + e.getLocation().getLongitude();
                if (!loc.contains(criteria.location)) matches = false;
            }

            if (matches) filtered.add(e);
        }

        adapter.setEvents(filtered);
    }
}

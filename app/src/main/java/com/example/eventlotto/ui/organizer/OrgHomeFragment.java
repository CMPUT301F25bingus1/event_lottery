package com.example.eventlotto.ui.organizer;

import android.os.Bundle;
import android.provider.Settings;
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
import com.example.eventlotto.model.Event;
import com.example.eventlotto.ui.entrant.EntFilterFragment;
import com.example.eventlotto.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Displays the entrant's home screen containing a scrollable list of available events.
 * <p>
 * Users can:
 * <ul>
 *   <li>View event listings pulled dynamically from Firestore.</li>
 *   <li>Open event details.</li>
 *   <li>Filter events by date ranges or days of the week.</li>
 *   <li>Search events by title or description.</li>
 * </ul>
 */
public class OrgHomeFragment extends Fragment {

    /**
     * RecyclerView displaying the list of events.
     */
    private RecyclerView recyclerView;

    /**
     * Adapter for binding {@link Event} data to RecyclerView items.
     */
    private EventAdapter adapter;

    /**
     * The currently displayed list of events (after filters/search).
     */
    private List<Event> eventList;

    /**
     * The complete list of all fetched events.
     */
    private List<Event> fullEventList;

    /**
     * Firestore service instance for event data retrieval.
     */
    private FirestoreService firestoreService;
    /**
     * Device ID string to identify the id of the device.
     */

    private String deviceId;


    /**
     * Called to initialize the fragment's UI.
     * Inflates the layout, sets up RecyclerView, search, and filter logic.
     *
     * @param inflater           LayoutInflater to inflate views.
     * @param container          The parent view for this fragment.
     * @param savedInstanceState Previous instance state (if any).
     * @return The inflated fragment view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firestoreService = new FirestoreService();
        recyclerView = view.findViewById(R.id.recycler_view_events);
        eventList = new ArrayList<>();
        fullEventList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // Initialize the adapter and define event click behavior
        adapter = new EventAdapter(eventList, event -> {
            OrgEventDetailsFragment fragment = OrgEventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "org_event_details");
        });

        recyclerView.setAdapter(adapter);

        firestoreService = new FirestoreService();
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        initializeUserThenLoadEvents();

        ImageButton filterButton = view.findViewById(R.id.filter_button);
        EditText searchEditText = view.findViewById(R.id.search_edit_text);
        ImageView searchIcon = view.findViewById(R.id.search_button);

        if (filterButton != null) {
            filterButton.setOnClickListener(v -> {
                EntFilterFragment entFilterFragment = new EntFilterFragment();
                entFilterFragment.setOnFilterAppliedListener(
                        (eventDateFrom, eventDateTo, registrationFrom, registrationTo, selectedDays) ->
                                applyEventFilters(eventDateFrom, eventDateTo, registrationFrom, registrationTo, selectedDays)
                );
                entFilterFragment.show(getParentFragmentManager(), "filter_fragment");
            });
        }

        //Live search functionality
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEvents(s.toString());
                }
            });
        }

        return view;
    }

    private void initializeUserThenLoadEvents() {

        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        firestoreService.getUser(deviceId)
                .addOnSuccessListener(snapshot -> {

                    // If user doesn't exist, create it here just like profile does
                    if (!snapshot.exists()) {
                        User newUser = new User(deviceId, "Default Name", "", "");

                        firestoreService.updateUserProfile(newUser, success -> {
                            fetchEvents();   // load events AFTER user created
                        });

                    } else {
                        fetchEvents();       // load events normally
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to initialize user", Toast.LENGTH_SHORT).show();
                    fetchEvents();  // worst case: still try
                });
    }


    /**
     * Fetches all events from Firestore and populates the {@link #fullEventList} and {@link #eventList}.
     * <p>
     * If successful, updates the RecyclerView adapter with the new data.
     * If unsuccessful, displays an error message to the user.
     */
    private void fetchEvents() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get Device ID
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Create a reference to this user
        DocumentReference myRef = db.collection("users").document(deviceId);

        // Server-side filtering — the correct way!
        db.collection("events")
                .whereEqualTo("organizerId", myRef)
                .get(Source.SERVER)     // FORCE fresh fetch (fixes your bug)
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    eventList.clear();
                    fullEventList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
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
                        Toast.makeText(getContext(),
                                "Error fetching events: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }


    /**
     * Filters events in real-time based on user search input.
     * Matches against both title and description (case-insensitive).
     *
     * @param query The search term entered by the user.
     */
    private void filterEvents(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setEvents(new ArrayList<>(fullEventList));
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

    /**
     * Applies filters to the event list based on selected criteria such as date ranges
     * and specific days of the week.
     *
     * @param eventDateFrom    Start date of the event date range.
     * @param eventDateTo      End date of the event date range.
     * @param registrationFrom Start date of registration date range.
     * @param registrationTo   End date of registration date range.
     * @param selectedDays     List of selected days of the week.
     */
    private void applyEventFilters(String eventDateFrom, String eventDateTo,
                                   String registrationFrom, String registrationTo,
                                   List<String> selectedDays) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date eventFromDate = parseDate(eventDateFrom, sdf);
        Date eventToDate = parseDate(eventDateTo, sdf);
        Date regFromDate = parseDate(registrationFrom, sdf);
        Date regToDate = parseDate(registrationTo, sdf);

        List<Event> filtered = new ArrayList<>();

        for (Event e : fullEventList) {
            boolean matches = true;

            Date eventStart = e.getEventStartAt() != null ? e.getEventStartAt().toDate() : null;
            Date eventEnd = e.getEventEndAt() != null ? e.getEventEndAt().toDate() : null;

            // --- Event date range ---
            if (eventFromDate != null && (eventEnd == null || eventEnd.before(eventFromDate)))
                matches = false;
            if (eventToDate != null && (eventStart == null || eventStart.after(eventToDate)))
                matches = false;

            // --- Registration date range ---
            Date regStart = e.getRegistrationOpensAt() != null ? e.getRegistrationOpensAt().toDate() : null;
            Date regEnd = e.getRegistrationClosesAt() != null ? e.getRegistrationClosesAt().toDate() : null;
            if (regFromDate != null && (regEnd == null || regEnd.before(regFromDate)))
                matches = false;
            if (regToDate != null && (regStart == null || regStart.after(regToDate)))
                matches = false;

            // --- Days-of-week filter ---
            if (selectedDays != null && !selectedDays.isEmpty() && eventStart != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(eventStart);
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

                String dayLetter = "";
                switch (dayOfWeek) {
                    case Calendar.MONDAY:
                        dayLetter = "M";
                        break;
                    case Calendar.TUESDAY:
                        dayLetter = "T";
                        break;
                    case Calendar.WEDNESDAY:
                        dayLetter = "W";
                        break;
                    case Calendar.THURSDAY:
                        dayLetter = "T";
                        break;
                    case Calendar.FRIDAY:
                        dayLetter = "F";
                        break;
                    case Calendar.SATURDAY:
                        dayLetter = "S";
                        break;
                    case Calendar.SUNDAY:
                        dayLetter = "S";
                        break;
                }

                if (!selectedDays.contains(dayLetter)) matches = false;
            }

            if (matches) filtered.add(e);
        }

        adapter.setEvents(filtered);
    }

    /**
     * Safely parses a date string using the provided {@link SimpleDateFormat}.
     *
     * @param dateStr The date string to parse.
     * @param sdf     The formatter to use.
     * @return A {@link Date} object, or {@code null} if parsing fails.
     */
    private Date parseDate(String dateStr, SimpleDateFormat sdf) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
package com.example.eventlotto.ui.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.eventlotto.model.EventStatus;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fragment displaying the myEvents screen with a list of events based on the current user.
 * There are two tabs, history which shows events that have ended, and current events
 * which shows events that haven't happened yet.
 */
public class Ent_MyEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter myEventsAdapter;
    private List<Event> eventList;
    // List of event status objects corresponding to the current user
    private List<EventStatus> statusList;
    private FirestoreService firestoreService;

    private Timestamp currentTimestamp;

    private String deviceId;

    private Boolean eventsLoaded;
    private Boolean statusesLoaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_events, container, false);

        // Used to ensure firestore data has properly loaded, as it is asynchronous
        eventsLoaded = false;
        statusesLoaded = false;

        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // For testing purposes
        //deviceId = "5d061e8a1b289e4b";

        currentTimestamp = Timestamp.now();

        // Initialize Firestore Service
        firestoreService = new FirestoreService();

        // Setup RecyclerView
        recyclerView = root.findViewById(R.id.myEventsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        statusList = new ArrayList<>();

        TabLayout tabs = root.findViewById(R.id.tabFilter);
        tabs.addTab(tabs.newTab().setText(R.string.tab_current));
        tabs.addTab(tabs.newTab().setText(R.string.tab_history));

        myEventsAdapter = new EventAdapter(eventList, event -> {
            // Show the EventDetailsFragment as a dialog
            Ent_EventDetailsFragment fragment = Ent_EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });

        // Fetch events from firestore
        fetchEvents();
        fetchStatuses();

        recyclerView.setAdapter(myEventsAdapter);

        //Default tab is Current Events
        tabs.selectTab(tabs.getTabAt(0));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                applyFilter(tab.getPosition());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) {
                // This could be implemented in the future to 'refresh' the current tab?
            }
        });

        return root;
    }

    /**
     * Populates a list of Events based on the firestore database
     */
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
                    eventsLoaded = true;
                    applyInitialFilter();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Populates a list of eventStatuses based on statuses from the firestore database
     * Only gets statuses that correspond to the current user
     */
    private void fetchStatuses() {
        firestoreService.getEventStatusesForUser(deviceId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    statusList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        EventStatus eventStatus = doc.toObject(EventStatus.class);
                        if (eventStatus != null) {
                            // The document ID is the sid, but the service already sets it
                            statusList.add(eventStatus);
                        }
                    }
                    statusesLoaded = true;
                    applyInitialFilter();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching statuses: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Calls applyFilter but only after checking that the data from firestore is properly loaded;
     * Fixes a bug where events do not load immediately, but only after switching tabs
     */
    private void applyInitialFilter(){
        if (eventsLoaded && statusesLoaded) {
            applyFilter(0);
        }
    }

    /**
     * Filters the events to show based on what tab is to be displayed, and whether or not the
     * event(s) have ended yet.
     * @param tabPosition ; 0 if current events, 1 if history
     */
    private void applyFilter(int tabPosition) {
        List<Event> filteredEvents = new ArrayList<>();

        // Create a map of event IDs that have status entries for this user
        Map<String, EventStatus> userEventStatusMap = new HashMap<>();
        for (EventStatus status : statusList) {
            userEventStatusMap.put(status.getEid(), status);
        }

        // Filter events based on tab position and user status
        for (Event event : eventList) {
            // Check if this event has a status entry for the current user
            if (userEventStatusMap.containsKey(event.getEid())) {
                boolean isCurrentEvent = event.getEventEndAt().compareTo(currentTimestamp) >= 0;

                if (tabPosition == 0 && isCurrentEvent) {
                    // Current Events tab - event must not have ended yet
                    filteredEvents.add(event);
                } else if (tabPosition == 1 && !isCurrentEvent) {
                    // History tab - event must have ended
                    filteredEvents.add(event);
                }
            }
        }

        myEventsAdapter.setEvents(filteredEvents);
        myEventsAdapter.notifyDataSetChanged();

    }
}
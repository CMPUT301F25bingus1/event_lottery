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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Ent_MyEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter myEventsAdapter;
    private List<Event> eventList;
    private FirestoreService firestoreService;

    private Timestamp currentTimestamp;

    private String deviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_events, container, false);

        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        Date currentDate = new Date();
        currentTimestamp = new Timestamp(currentDate);

        // Initialize Firestore Service
        firestoreService = new FirestoreService();

        // Setup RecyclerView
        RecyclerView recyclerView = root.findViewById(R.id.myEventsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();

        // Fetch events form Firestore
        fetchEvents();
        TabLayout tabs = root.findViewById(R.id.tabFilter);
        tabs.addTab(tabs.newTab().setText(R.string.tab_current));
        tabs.addTab(tabs.newTab().setText(R.string.tab_history));

        myEventsAdapter = new EventAdapter(eventList, event -> {
            // Show the EventDetailsFragment as a dialog
            Ent_EventDetailsFragment fragment = Ent_EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });

        recyclerView.setAdapter(myEventsAdapter);

        //Default tab is Current Events
        tabs.selectTab(tabs.getTabAt(0));
        applyFilter(0);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                applyFilter(tab.getPosition());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
        // Fetch events form Firestore
        fetchEvents();
        return root;
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
                    myEventsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilter(int tabPosition) {
        List<Event> filteredEvents = new ArrayList<>();
        if (tabPosition== 0) { // Current events
            for (Event event : eventList) {
                if (event.getEventEndAt().compareTo(currentTimestamp) >= 0) {
                    filteredEvents.add(event);
                }
            }
        } else { // History
            for (Event event : eventList) {
                if (event.getEventEndAt().compareTo(currentTimestamp) < 0) {
                    filteredEvents.add(event);
                }
            }
        }
        myEventsAdapter.setEvents(filteredEvents);
    }
}
package com.example.eventlotto.ui.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import java.util.List;

/**
 * Fragment displaying the My Events screen with a list of events, separated into Current Events
 * (events which haven't ended yet) and History (events which have now ended)
 */
public class Ent_MyEventsFragment extends Fragment {

    private RecyclerView pendingRecyclerView;
    private EventAdapter pendingAdapter;
    private List<Event> pendingEvents;
    private List<Event> registeredEvents;
    private FirestoreService firestoreService;
    private int currentTab;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_events, container, false);

        // Tracks which tab is open
        currentTab = 0;

        firestoreService = new FirestoreService();

        pendingRecyclerView = root.findViewById(R.id.myEventsRecycler);

        // Disable nested scrolling to allow proper height calculation inside NestedScrollView
        pendingRecyclerView.setNestedScrollingEnabled(false);

        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        pendingEvents = new ArrayList<>();
        pendingAdapter = new EventAdapter(pendingEvents, event -> {
            // Show the EventDetailsFragment with accept/decline options
            Ent_EventDetailsFragment fragment = Ent_EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });
        pendingRecyclerView.setAdapter(pendingAdapter);

        // Sets up tabs (current events and history)
        TabLayout tabs = root.findViewById(R.id.tabFilter);
        tabs.addTab(tabs.newTab().setText(R.string.tab_current));
        tabs.addTab(tabs.newTab().setText(R.string.tab_history));

        getParentFragmentManager().setFragmentResultListener(
                "eventStatusChanged",
                this,
                (requestKey, bundle) -> fetchUserEvents(currentTab)
        );

        // Fetch user's events on load
        fetchUserEvents(0);

        //Default tab is Current Events
        tabs.selectTab(tabs.getTabAt(0));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // When tab is selected, load the appropriate events and data
                currentTab = tab.getPosition();
                fetchUserEvents(currentTab);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) {
                // This could be implemented in the future to 'refresh' the current tab?
            }
        });

        return root;
    }


    /**
     * Gets events and data based on the current user, then updates which events are to be shown
     * based on this data and the current tab
     * @param currentTab ; 0 if current tab is Current events, and 1 if it is History
     */
    public void fetchUserEvents(int currentTab) {
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Removes any events whose waitlist was left
        pendingEvents.clear();
        pendingAdapter.notifyDataSetChanged();

        // query ALL events
        firestoreService.events().get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setEid(doc.getId());
                                firestoreService.events()
                                        .document(event.getEid()).collection("status").document(deviceId).get()
                                        .addOnSuccessListener(statusDoc -> {
                                            if (statusDoc.exists()) {
                                                String status = statusDoc.getString("status");

                                                // Remove from list first to avoid duplicates
                                                pendingEvents.removeIf(e -> e.getEid().equals(event.getEid()));

                                                if ("waiting".equalsIgnoreCase(status) || "selected".equalsIgnoreCase(status) || "signed_up".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
                                                    // True if the event has not ended/happened yet, false if the event has ended
                                                    boolean isCurrentEvent = event.getEventEndAt().compareTo(Timestamp.now()) >= 0;

                                                    if (currentTab == 0 && isCurrentEvent){
                                                        pendingEvents.add(event);
                                                    }
                                                    else if (currentTab == 1 && !isCurrentEvent){
                                                        pendingEvents.add(event);
                                                    }
                                                }

                                                pendingAdapter.notifyDataSetChanged();
                                                updateVisibility();
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching events: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    /**
     * Updates visibility of elements to ensure UI is visible
     */
    private void updateVisibility() {
        // Ensure list is visible
        pendingRecyclerView.setVisibility(View.VISIBLE);
    }


    /**
     * Refreshes events when returning to the fragment
     */
    @Override
    public void onResume() {
        super.onResume();
        // Refresh events when returning to fragment
        fetchUserEvents(currentTab);
    }
}

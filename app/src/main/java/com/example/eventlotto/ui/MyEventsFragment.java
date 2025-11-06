package com.example.eventlotto.ui;

import android.os.Bundle;
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
import com.example.eventlotto.events.Event;
import com.example.eventlotto.events.EventAdapter;
import com.example.eventlotto.events.EventDetailsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter myEventsAdapter;
    private List<Event> eventList;
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_events, container, false);

        // Initialize Firestore Service
        firestoreService = new FirestoreService();

        // Setup RecyclerView
        RecyclerView recyclerView = root.findViewById(R.id.myEventsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();

        myEventsAdapter = new EventAdapter(eventList, event -> {
            // Show the EventDetailsFragment as a dialog
            EventDetailsFragment fragment = EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });

        recyclerView.setAdapter(myEventsAdapter);

        TabLayout tabs = root.findViewById(R.id.tabFilter);
        tabs.addTab(tabs.newTab().setText(R.string.tab_current));
        tabs.addTab(tabs.newTab().setText(R.string.tab_history));

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


    /*

        adapter = new NotificationsAdapter(new ArrayList<>(), (event, position) -> {
            applyFilter(tabs.getSelectedTabPosition());
        });
        rv.setAdapter(adapter);

        //Default tab is Following
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
        return root;
    }

     */
}


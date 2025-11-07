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
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Ent_MyEventsFragment extends Fragment {

    private RecyclerView pendingRecyclerView;
    private RecyclerView registeredRecyclerView;
    private EventAdapter pendingAdapter;
    private EventAdapter registeredAdapter;
    private List<Event> pendingEvents;
    private List<Event> registeredEvents;
    private FirestoreService firestoreService;
    private String currentUserId;
    private TextView pendingLabel;
    private TextView registeredLabel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_events, container, false);

        firestoreService = new FirestoreService();

        currentUserId =  Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        pendingLabel = root.findViewById(R.id.pendingLabel);
        registeredLabel = root.findViewById(R.id.registeredLabel);

        pendingRecyclerView = root.findViewById(R.id.pendingRecycler);
        registeredRecyclerView = root.findViewById(R.id.registeredRecycler);

        // Disable nested scrolling to allow proper height calculation inside NestedScrollView
        pendingRecyclerView.setNestedScrollingEnabled(false);
        registeredRecyclerView.setNestedScrollingEnabled(false);

        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        registeredRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        pendingEvents = new ArrayList<>();
        pendingAdapter = new EventAdapter(pendingEvents, event -> {
            // Show the EventDetailsFragment with accept/decline options
            Ent_EventDetailsFragment fragment = Ent_EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });
        pendingRecyclerView.setAdapter(pendingAdapter);

        registeredEvents = new ArrayList<>();
        registeredAdapter = new EventAdapter(registeredEvents, event -> {
            // Show the EventDetailsFragment (view only for registered events)
            Ent_EventDetailsFragment fragment = Ent_EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });
        registeredRecyclerView.setAdapter(registeredAdapter);

        getParentFragmentManager().setFragmentResultListener(
                "eventStatusChanged",
                this,
                (requestKey, bundle) -> fetchUserEvents()
        );

        // Fetch user's events on load
        fetchUserEvents();

        return root;
    }


   public void fetchUserEvents() {
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        pendingEvents.clear();
        registeredEvents.clear();

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

                                                // Remove from both lists first to avoid duplicates
                                                pendingEvents.removeIf(e -> e.getEid().equals(event.getEid()));
                                                registeredEvents.removeIf(e -> e.getEid().equals(event.getEid()));

                                                // Sort into corresponding section
                                                if ("waiting".equalsIgnoreCase(status) || "selected".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
                                                    pendingEvents.add(event);
                                                } else if ("signed_up".equalsIgnoreCase(status)) {
                                                    registeredEvents.add(event);
                                                }

                                                pendingAdapter.notifyDataSetChanged();
                                                registeredAdapter.notifyDataSetChanged();
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


    private void updateVisibility() {
        // show pending and registered sections
        if (pendingLabel != null) pendingLabel.setVisibility(View.VISIBLE);
        if (registeredLabel != null) registeredLabel.setVisibility(View.VISIBLE);
        pendingRecyclerView.setVisibility(View.VISIBLE);
        registeredRecyclerView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onResume() {
        super.onResume();
        // Refresh events when returning to fragment
        fetchUserEvents();
    }
}


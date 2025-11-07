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
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that shows the user's events, divided into "Pending" and "Registered" sections.
 * <p>
 * "Pending" shows the events the user is waiting for, has been selected for, or cancelled.
 * "Registered" shows the events the user has successfully signed up for.
 * <p>
 * This fragment fetches all events from Firestore and queries each event's status for the
 * current device ID to populate the respective lists. Users can tap an event to view details.
 */
public class Ent_MyEventsFragment extends Fragment {

    /** Shows pending events. */
    private RecyclerView pendingRecyclerView;

    /** Shows registered events. */
    private RecyclerView registeredRecyclerView;

    /** For pending events list. */
    private EventAdapter pendingAdapter;

    /** For registered events list. */
    private EventAdapter registeredAdapter;

    /** List containing pending events. */
    private List<Event> pendingEvents;

    /** List containing registered events. */
    private List<Event> registeredEvents;

    /** Firestore service used to query events and user status. */
    private FirestoreService firestoreService;

    /** Unique device ID used to identify current user. */
    private String currentUserId;

    /** Label TextView for pending events section. */
    private TextView pendingLabel;

    /** Label TextView for registered events section. */
    private TextView registeredLabel;

    /**
     * Called to create and return the view hierarchy associated with this fragment.
     * <p>
     * Initializes the RecyclerViews, adapters, and labels. Also sets up a
     * {@link androidx.fragment.app.FragmentResultListener} to refresh the lists when
     * event statuses change.
     *
     * @param inflater  LayoutInflater used to inflate the fragment's layout.
     * @param container Parent view that this fragment's UI attaches to.
     * @param savedInstanceState Previous saved state, if available.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_my_events, container, false);

        firestoreService = new FirestoreService();

        currentUserId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        pendingLabel = root.findViewById(R.id.pendingLabel);
        registeredLabel = root.findViewById(R.id.registeredLabel);

        // Setup pending events RecyclerView
        pendingRecyclerView = root.findViewById(R.id.pendingRecycler);
        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingEvents = new ArrayList<>();
        pendingAdapter = new EventAdapter(pendingEvents, event -> {
            Ent_EventDetailsFragment fragment = Ent_EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });
        pendingRecyclerView.setAdapter(pendingAdapter);

        // Setup registered events RecyclerView
        registeredRecyclerView = root.findViewById(R.id.registeredRecycler);
        registeredRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        registeredEvents = new ArrayList<>();
        registeredAdapter = new EventAdapter(registeredEvents, event -> {
            Ent_EventDetailsFragment fragment = Ent_EventDetailsFragment.newInstance(event.getEid());
            fragment.show(getParentFragmentManager(), "event_details");
        });
        registeredRecyclerView.setAdapter(registeredAdapter);

        // Listen for event status changes to refresh lists
        getParentFragmentManager().setFragmentResultListener(
                "eventStatusChanged",
                this,
                (requestKey, bundle) -> fetchUserEvents()
        );

        // Fetch events on initial load
        fetchUserEvents();

        return root;
    }

    /**
     * Fetches all events from Firestore and sorts them into pending and registered lists
     * based on the current user's status.
     */
    public void fetchUserEvents() {
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        pendingEvents.clear();
        registeredEvents.clear();

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

                                                // Remove duplicates
                                                pendingEvents.removeIf(e -> e.getEid().equals(event.getEid()));
                                                registeredEvents.removeIf(e -> e.getEid().equals(event.getEid()));

                                                // Sort event based on status
                                                if ("waiting".equalsIgnoreCase(status) ||
                                                        "selected".equalsIgnoreCase(status) ||
                                                        "cancelled".equalsIgnoreCase(status)) {
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

    /**
     * Updates the visibility of labels and RecyclerViews for pending and registered events.
     * Checks that both sections are visible if there is any content.
     */
    private void updateVisibility() {
        if (pendingLabel != null) pendingLabel.setVisibility(View.VISIBLE);
        if (registeredLabel != null) registeredLabel.setVisibility(View.VISIBLE);
        pendingRecyclerView.setVisibility(View.VISIBLE);
        registeredRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Refreshes the event lists whenever the fragment resumes.
     */
    @Override
    public void onResume() {
        super.onResume();
        fetchUserEvents();
    }
}

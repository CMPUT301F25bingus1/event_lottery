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
import com.example.eventlotto.model.FollowedEvent;
import com.example.eventlotto.functions.notifications.NotificationsAdapter;
import com.example.eventlotto.model.Notification;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fragment that displays notifications and followed events for an entrant.
 * Users can view all events or only the ones they are following,
 * subscribe/unsubscribe to notifications, and view event details.
 */
public class EntNotificationsFragment extends Fragment {

    private final List<FollowedEvent> allEvents = new ArrayList<>();
    private NotificationsAdapter adapter;
    private FirestoreService firestoreService;
    private TabLayout tabs;
    private String deviceId;
    @Nullable private ListenerRegistration subscriptionsReg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        firestoreService = new FirestoreService();
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Setup RecyclerView
        RecyclerView rv = root.findViewById(R.id.notificationsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Setup Tabs
        tabs = root.findViewById(R.id.tabFilter);
        tabs.addTab(tabs.newTab().setText(R.string.tab_following));
        tabs.addTab(tabs.newTab().setText(R.string.tab_all));

        // Setup adapter with listener for event clicks and toggle
        adapter = new NotificationsAdapter(new ArrayList<>(), new NotificationsAdapter.Listener() {
            @Override
            public void onNotificationToggle(FollowedEvent event, int position) {
                handleSubscriptionToggle(event);
                applyFilter(tabs.getSelectedTabPosition());
            }

            @Override
            public void onEventClick(FollowedEvent event) {
                EntEventDetailsFragment fragment = EntEventDetailsFragment.newInstance(event.getId());
                fragment.show(getParentFragmentManager(), "event_details");
            }
        });
        rv.setAdapter(adapter);

        // Default select "Following" tab
        tabs.selectTab(tabs.getTabAt(0));
        applyFilter(0);

        // Listen for tab changes to update displayed events
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                applyFilter(tab.getPosition());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        // Fetch events and user-specific data
        fetchAllEvents();
        return root;
    }

    /**
     * Fetches all events from Firestore and populates the local list.
     * Afterwards, it loads the user's subscriptions and event statuses.
     */
    private void fetchAllEvents() {
        firestoreService.events()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("eventTitle");
                        String description = doc.getString("description");
                        String imageUrl = doc.getString("eventURL");
                        String organizerMessage = doc.getString("organizerMessage"); // optional field from Firestore

                        FollowedEvent event = new FollowedEvent(
                                id,
                                name != null ? name : "Untitled",
                                description != null ? description : "",
                                R.mipmap.ic_launcher,
                                imageUrl,
                                false, // notificationsEnabled default
                                organizerMessage // optional message
                        );

                        allEvents.add(event);
                    }
                    // Once all events are loaded, load user subscriptions
                    loadUserSubscriptions();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching events: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }


    /**
     * Loads the user's current notification subscriptions and updates the events accordingly.
     */
    private void loadUserSubscriptions() {
        if (subscriptionsReg != null) {
            subscriptionsReg.remove();
            subscriptionsReg = null;
        }
        subscriptionsReg = firestoreService.notifications()
                .whereEqualTo("uid", deviceId)
                .addSnapshotListener((query, error) -> {
                    Set<String> subscribedIds = new HashSet<>();
                    if (query != null) {
                        for (DocumentSnapshot doc : query) {
                            String eid = doc.getString("eid");
                            if (eid != null) subscribedIds.add(eid);
                        }
                    }
                    for (FollowedEvent e : allEvents) {
                        e.setNotificationsEnabled(subscribedIds.contains(e.getId()));
                    }
                    applyFilter(tabs.getSelectedTabPosition());
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (subscriptionsReg != null) {
            subscriptionsReg.remove();
            subscriptionsReg = null;
        }
    }

    /**
     * Handles subscribing or unsubscribing the user to notifications for a given event.
     *
     * @param event The event being toggled.
     */
    private void handleSubscriptionToggle(FollowedEvent event) {
        String nid = deviceId + "_" + event.getId();
        if (event.isNotificationsEnabled()) {
            Notification n = new Notification();
            n.setNid(nid);
            n.setUid(deviceId);
            n.setEid(event.getId());

            // Optional: set a message if available from the event
            String organizerMessage = event.getMessage(); // Add message field to FollowedEvent
            if (organizerMessage != null && !organizerMessage.isEmpty()) {
                n.setMessage("Message from organizer: " + organizerMessage);
            }

            firestoreService.saveNotification(n)
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(ex ->
                            Toast.makeText(getContext(), "Failed to subscribe", Toast.LENGTH_SHORT).show());
        } else {
            firestoreService.notifications().document(nid).delete()
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(ex ->
                            Toast.makeText(getContext(), "Failed to unsubscribe", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Filters the events to display based on the currently selected tab.
     *
     * @param tabPosition Index of the selected tab (0 = Following, 1 = All)
     */
    private void applyFilter(int tabPosition) {
        List<FollowedEvent> filtered = new ArrayList<>();
        if (tabPosition == 0) { // Following
            for (FollowedEvent e : allEvents) {
                if (e.isNotificationsEnabled()) filtered.add(e);
            }
        } else { // All
            filtered.addAll(allEvents);
        }
        adapter.setItems(filtered);
    }


}

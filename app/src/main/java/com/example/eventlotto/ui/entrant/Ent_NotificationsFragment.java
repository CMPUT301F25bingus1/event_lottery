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
import com.example.eventlotto.functions.events.EventDetailsFragment;
import com.example.eventlotto.functions.notifications.FollowedEvent;
import com.example.eventlotto.functions.notifications.NotificationsAdapter;
import com.example.eventlotto.model.Notification;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Ent_NotificationsFragment extends Fragment {

    private final List<FollowedEvent> allEvents = new ArrayList<>();
    private NotificationsAdapter adapter;
    private FirestoreService firestoreService;
    private TabLayout tabs;
    private String deviceId;
    private final Map<String, String> statusByEid = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        firestoreService = new FirestoreService();
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        RecyclerView rv = root.findViewById(R.id.notificationsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        tabs = root.findViewById(R.id.tabFilter);
        tabs.addTab(tabs.newTab().setText(R.string.tab_following));
        tabs.addTab(tabs.newTab().setText(R.string.tab_all));

        adapter = new NotificationsAdapter(new ArrayList<>(), new NotificationsAdapter.Listener() {
            @Override
            public void onNotificationToggle(FollowedEvent event, int position) {
                handleSubscriptionToggle(event);
                applyFilter(tabs.getSelectedTabPosition());
            }

            @Override
            public void onEventClick(FollowedEvent event) {
                EventDetailsFragment fragment = EventDetailsFragment.newInstance(event.getId());
                fragment.show(getParentFragmentManager(), "event_details");
            }
        });
        rv.setAdapter(adapter);

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

        fetchAllEvents();
        return root;
    }

    private void fetchAllEvents() {
        firestoreService.events()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("eventTitle");
                        String description = doc.getString("description");
                        FollowedEvent e = new FollowedEvent(
                                id,
                                name != null ? name : "Untitled",
                                description != null ? description : "",
                                FollowedEvent.Status.WAITING,
                                R.drawable.event1,
                                false,
                                false
                        );
                        allEvents.add(e);
                    }
                    loadUserSubscriptions();
                    loadUserEventStatuses();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserSubscriptions() {
        firestoreService.notifications()
                .whereEqualTo("uid", deviceId)
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> subscribedIds = new HashSet<>();
                    for (DocumentSnapshot doc : query) {
                        String eid = doc.getString("eid");
                        if (eid != null) subscribedIds.add(eid);
                    }
                    for (FollowedEvent e : allEvents) {
                        e.setNotificationsEnabled(subscribedIds.contains(e.getId()));
                    }
                    applyFilter(tabs.getSelectedTabPosition());
                })
                .addOnFailureListener(e -> applyFilter(tabs.getSelectedTabPosition()));
    }

    private void handleSubscriptionToggle(FollowedEvent event) {
        String nid = deviceId + "_" + event.getId();
        if (event.isNotificationsEnabled()) {
            Notification n = new Notification();
            n.setNid(nid);
            n.setUid(deviceId);
            n.setEid(event.getId());
            firestoreService.saveNotification(n)
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(ex -> Toast.makeText(getContext(), "Failed to subscribe", Toast.LENGTH_SHORT).show());
        } else {
            firestoreService.notifications().document(nid).delete()
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(ex -> Toast.makeText(getContext(), "Failed to unsubscribe", Toast.LENGTH_SHORT).show());
        }
    }

    private void applyFilter(int tabPosition) {
        List<FollowedEvent> filtered = new ArrayList<>();
        if (tabPosition== 0) { // Following
            for (FollowedEvent e : allEvents) {
                if (e.isNotificationsEnabled()) filtered.add(e);
            }
        } else { // All
            filtered.addAll(allEvents);
        }
        adapter.setStatusMap(statusByEid);
        adapter.setItems(filtered);
    }

    private void loadUserEventStatuses() {
        firestoreService.getEventStatusesForUser(deviceId)
                .addOnSuccessListener(query -> {
                    statusByEid.clear();
                    for (DocumentSnapshot doc : query) {
                        String eid = doc.getString("eid");
                        String status = doc.getString("status");
                        if (eid != null && status != null) statusByEid.put(eid, status);
                    }
                    adapter.setStatusMap(statusByEid);
                    applyFilter(tabs.getSelectedTabPosition());
                })
                .addOnFailureListener(e -> { /* ignore */ });
    }
}

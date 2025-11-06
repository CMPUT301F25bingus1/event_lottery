package com.example.eventlotto.ui.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import com.example.eventlotto.R;
import com.example.eventlotto.functions.notifications.FollowedEvent;
import com.example.eventlotto.functions.notifications.NotificationsAdapter;

import java.util.ArrayList;
import java.util.List;

public class Ent_NotificationsFragment extends Fragment {

    private final List<FollowedEvent> allEvents = new ArrayList<>();
    private NotificationsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        RecyclerView rv= root.findViewById(R.id.notificationsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        allEvents.clear();
        allEvents.addAll(createMockEvents()); //TODO: implement actual event logic here

        TabLayout tabs = root.findViewById(R.id.tabFilter);
        tabs.addTab(tabs.newTab().setText(R.string.tab_following));
        tabs.addTab(tabs.newTab().setText(R.string.tab_all));

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

    private void applyFilter(int tabPosition) {
        List<FollowedEvent> filtered = new ArrayList<>();
        if (tabPosition== 0) { // Following
            for (FollowedEvent e : allEvents) {
                if (e.isNotificationsEnabled()) filtered.add(e);
            }
        } else { // All
            filtered.addAll(allEvents);
        }
        adapter.setItems(filtered);
    }

    private List<FollowedEvent> createMockEvents() { // Mockups
        List<FollowedEvent> list = new ArrayList<>();
        list.add(new FollowedEvent("1", "Event 1", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua", FollowedEvent.Status.WAITING, R.drawable.event1, true, false));
        list.add(new FollowedEvent("2", "Event 2", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua", FollowedEvent.Status.ACCEPTED, R.drawable.event2, true, false));
        list.add(new FollowedEvent("3", "Event 3", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua", FollowedEvent.Status.NOT_CHOSEN, R.drawable.event3, false, false));
        list.add(new FollowedEvent("4", "Event 4", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua", FollowedEvent.Status.WAITING, R.drawable.event1, false, true));
        return list;
    }
}

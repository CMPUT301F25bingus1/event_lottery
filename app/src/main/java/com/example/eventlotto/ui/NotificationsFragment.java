package com.example.eventlotto.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.R;
import com.example.eventlotto.notifications.FollowedEvent;
import com.example.eventlotto.notifications.NotificationsAdapter;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        RecyclerView rv= root.findViewById(R.id.notificationsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));


        List<FollowedEvent> all = createMockEvents(); // TODO: Following or All logic
        List<FollowedEvent> openOnly = new ArrayList<>();
        for (FollowedEvent e : all) {
            if (!e.isClosed()) openOnly.add(e);
        }

        NotificationsAdapter adapter = new NotificationsAdapter(openOnly);
        rv.setAdapter(adapter);
        return root;
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

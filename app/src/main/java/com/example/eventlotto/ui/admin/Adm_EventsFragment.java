package com.example.eventlotto.ui.admin;

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
import com.example.eventlotto.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Adm_EventsFragment extends Fragment {

    private final List<Event> events = new ArrayList<>();
    private Adm_EventsFragment.EventsAdapter adapter;
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_events, container, false);
        firestoreService = new FirestoreService();

        RecyclerView rv = v.findViewById(R.id.recycler_admin_events);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventsAdapter(events, eid -> deleteEvent(eid));
        rv.setAdapter(adapter);

        loadEvents();
        return v;
    }

    private void loadEvents() {
        firestoreService.events().get()
                .addOnSuccessListener(snaps -> {
                    events.clear();
                    for (DocumentSnapshot d : snaps) {
                        Event e = d.toObject(Event.class);
                        if (e != null) {
                            e.setEid(d.getId());
                            events.add(e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show());
    }

    private void deleteEvent(String eid) {
        firestoreService.events().document(eid).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                    loadEvents();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show());
    }

    private static class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {
        interface Listener { void onDelete(String eid); }
        private final List<Event> items; private final Listener listener;
        EventsAdapter(List<Event> items, Listener listener) { this.items = items; this.listener = listener; }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos), listener); }
        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            VH(@NonNull View itemView) { super(itemView); }
            void bind(Event e, Listener listener) {
                android.widget.TextView title = itemView.findViewById(R.id.text_event_title);
                android.widget.TextView desc = itemView.findViewById(R.id.text_event_desc);
                View btn = itemView.findViewById(R.id.btn_delete_event);
                title.setText(e.getEventTitle());
                desc.setText(e.getDescription());
                btn.setOnClickListener(v -> { if (listener != null && e.getEid() != null) listener.onDelete(e.getEid()); });
            }
        }
    }
}


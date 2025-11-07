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

/**
 * Fragment for admin users to view and manage all events.
 * <p>
 * Displays events in a RecyclerView with the option to delete each event.
 * </p>
 */
public class Adm_EventsFragment extends Fragment {

    /** List of all events retrieved from Firestore. */
    private final List<Event> events = new ArrayList<>();

    /** RecyclerView adapter for displaying events. */
    private EventsAdapter adapter;

    /** Service class for interacting with Firestore. */
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_events, container, false);

        firestoreService = new FirestoreService();

        RecyclerView recyclerView = root.findViewById(R.id.recycler_admin_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EventsAdapter(events, this::deleteEvent);
        recyclerView.setAdapter(adapter);

        loadEvents();

        return root;
    }

    /**
     * Fetches all events from Firestore and updates the RecyclerView.
     */
    private void loadEvents() {
        firestoreService.events().get()
                .addOnSuccessListener(snapshots -> {
                    events.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEid(doc.getId());
                            events.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Deletes an event by its ID and refreshes the list.
     *
     * @param eid Event ID
     */
    private void deleteEvent(String eid) {
        firestoreService.events().document(eid).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                    loadEvents();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * RecyclerView adapter for displaying admin events.
     */
    private static class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {

        /** Listener interface for delete actions. */
        interface Listener {
            void onDelete(String eid);
        }

        private final List<Event> items;
        private final Listener listener;

        EventsAdapter(List<Event> items, Listener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_event, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(items.get(position), listener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        /**
         * ViewHolder for individual event items.
         */
        static class VH extends RecyclerView.ViewHolder {
            VH(@NonNull View itemView) {
                super(itemView);
            }

            void bind(Event event, Listener listener) {
                android.widget.TextView title = itemView.findViewById(R.id.text_event_title);
                android.widget.TextView desc = itemView.findViewById(R.id.text_event_desc);
                View deleteBtn = itemView.findViewById(R.id.btn_delete_event);

                title.setText(event.getEventTitle());
                desc.setText(event.getDescription());

                deleteBtn.setOnClickListener(v -> {
                    if (listener != null && event.getEid() != null) {
                        listener.onDelete(event.getEid());
                    }
                });
            }
        }
    }
}

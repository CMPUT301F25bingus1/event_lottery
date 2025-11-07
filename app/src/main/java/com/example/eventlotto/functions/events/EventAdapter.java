package com.example.eventlotto.functions.events;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.R;
import com.example.eventlotto.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * RecyclerView adapter for displaying a list of Event objects.
 * It handles real-time updates of the user's waitlist status for each event.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;

    private final OnItemClickListener listener;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Interface definition for a callback to be invoked when an item is clicked.
     */
    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    /**
     * Constructor for EventAdapter.
     *
     * @param events   List of Event objects to show.
     * @param listener Listener for handling item clicks.
     */
    public EventAdapter(List<Event> events, OnItemClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    /**
     * Updates the list of events and refreshes the RecyclerView.
     *
     * @param events New list of Event objects.
     */
    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.title.setText(event.getEventTitle());
        holder.description.setText(event.getDescription());

        // Reset UI for recycling
        holder.status.setVisibility(View.GONE);

        // Remove any previous listener attached to this holder
        if (holder.waitlistReg != null) {
            holder.waitlistReg.remove();
            holder.waitlistReg = null;
        }

        String eid = event.getEid();
        if (eid == null || eid.isEmpty()) {
            // No id => nothing to listen to
            return;
        }

        // Keep track of which event this holder is currently bound to
        holder.boundEventId = eid;

        String deviceId = Settings.Secure.getString(
                holder.itemView.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Realtime listener: row updates immediately when you join/leave in the dialog
        holder.waitlistReg = db.collection("events").document(eid)
                .collection("status").document(deviceId)
                .addSnapshotListener((snap, err) -> {
                    // Ignore callbacks if this holder has been rebound to another item
                    if (holder.boundEventId == null || !eid.equals(holder.boundEventId)) return;

                    if (err != null || snap == null) {
                        holder.status.setVisibility(View.GONE);
                        return;
                    }

                    if (snap.exists()) {
                        // If you store a "status" field, map it; otherwise default to "Waiting"
                        String rawStatus = snap.getString("status");
                        applyStatusChip(holder.status, rawStatus); // sets text + background
                        holder.status.setVisibility(View.VISIBLE);
                    } else {
                        holder.status.setVisibility(View.GONE);
                    }
                });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(event);
        });
    }

    @Override
    public void onViewRecycled(@NonNull EventViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.waitlistReg != null) {
            holder.waitlistReg.remove();
            holder.waitlistReg = null;
        }
        holder.boundEventId = null;
        holder.status.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    /**
     * ViewHolder class for holding references to the views of each event item.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        TextView description;

        TextView status;

        @Nullable
        ListenerRegistration waitlistReg;

        @Nullable
        String boundEventId;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textName);
            description = itemView.findViewById(R.id.textDescription);
            status = itemView.findViewById(R.id.textStatus);
        }
    }

    /**
     * Applies the appropriate status chip background and label to a TextView.
     *
     * @param tv        The TextView to update.
     * @param rawStatus The raw status string from Firestore, may be null.
     */
    private void applyStatusChip(TextView tv, @Nullable String rawStatus) {
        String s = (rawStatus == null ? "waiting" : rawStatus).trim().toLowerCase();
        int bg;
        String label;
        switch (s) {
            case "selected":
                bg = R.drawable.bg_status_selected; label = "Selected"; break;
            case "signed up":
            case "signed_up":
                bg = R.drawable.bg_status_signed_up; label = "Signed Up"; break;
            case "cancelled":
            case "canceled":
                bg = R.drawable.bg_status_cancelled; label = "Cancelled"; break;
            case "not chosen":
            case "not_chosen":
                bg = R.drawable.bg_status_not_chosen; label = "Not Chosen"; break;
            default: // waiting
                bg = R.drawable.bg_status_waiting; label = "Waiting";
        }
        tv.setBackgroundResource(bg);
        tv.setText(label);
    }
}

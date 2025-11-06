package com.example.eventlotto.functions.events;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlotto.R;
import com.example.eventlotto.model.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;
    private OnItemClickListener listener;
    private Map<String, String> statusByEid = new HashMap<>();

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public EventAdapter(List<Event> events, OnItemClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public void setStatusByEid(Map<String, String> statusByEid) {
        this.statusByEid = statusByEid != null ? statusByEid : new HashMap<>();
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
        String eid = event.getEid();
        String status = eid != null ? statusByEid.get(eid) : null;
        if (!TextUtils.isEmpty(status)) {
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setText(capitalizeStatus(status));
        } else {
            holder.status.setText("");
            holder.status.setVisibility(View.GONE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, status;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textName);
            description = itemView.findViewById(R.id.textDescription);
            status = itemView.findViewById(R.id.textStatus);
        }
    }

    private static String capitalizeStatus(String s) {
        if (s == null || s.isEmpty()) return s;
        // keep exact words but capitalize first letter of each word
        String[] parts = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].length() > 0) {
                sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
            }
            if (i < parts.length - 1) sb.append(' ');
        }
        return sb.toString();
    }
}

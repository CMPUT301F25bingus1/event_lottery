package com.example.eventlotto.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.R;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    private final List<FollowedEvent> items;

    public NotificationsAdapter(List<FollowedEvent> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static int statusTextRes(FollowedEvent.Status s) {
        switch (s) {
            case ACCEPTED: return R.string.status_accepted;
            case WAITING: return R.string.status_waiting;
            default: return R.string.status_not_chosen;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView iconNotify;
        final ImageView imageEvent;
        final TextView textName;
        final TextView textStatus;
        final TextView textDescription;

        VH(@NonNull View itemView) {
            super(itemView);
            iconNotify = itemView.findViewById(R.id.iconNotify);
            imageEvent = itemView.findViewById(R.id.imageEvent);
            textName = itemView.findViewById(R.id.textName);
            textStatus = itemView.findViewById(R.id.textStatus);
            textDescription = itemView.findViewById(R.id.textDescription);
        }

        void bind(FollowedEvent e) {
            textName.setText(e.getName());
            textDescription.setText(e.getDescription());
            imageEvent.setImageResource(e.getImageResId());
            textStatus.setText(statusTextRes(e.getStatus()));
            iconNotify.setImageResource(e.isNotificationsEnabled()
                    ? R.drawable.notification_on
                    : R.drawable.notification_off);
            iconNotify.setOnClickListener(v -> {
                e.setNotificationsEnabled(!e.isNotificationsEnabled());
                // Update only this item
                RecyclerView.Adapter<?> a = getBindingAdapter();
                if (a != null) a.notifyItemChanged(getBindingAdapterPosition());
            });
        }
    }
}

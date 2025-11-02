package com.example.eventlotto.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.R;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    public interface Listener {
        void onNotificationToggle(FollowedEvent event, int position);
    }

    private final List<FollowedEvent> items;
    @Nullable private final Listener listener;

    public NotificationsAdapter(List<FollowedEvent> items, @Nullable Listener listener) {
        this.items = items;
        this.listener = listener;
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

    private static int statusBgRes(FollowedEvent.Status s) {
        switch (s) {
            case ACCEPTED: return R.drawable.bg_status_accepted;
            case WAITING: return R.drawable.bg_status_waiting;
            default: return R.drawable.bg_status_not_chosen;
        }
    }

    public void setItems(List<FollowedEvent> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
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
            textStatus.setBackgroundResource(statusBgRes(e.getStatus()));

            iconNotify.setImageResource(e.isNotificationsEnabled() //toggle between on and off
                    ? R.drawable.notification_on
                    : R.drawable.notification_off);

            iconNotify.setOnClickListener(v -> {
                e.setNotificationsEnabled(!e.isNotificationsEnabled()); //flip boolean
                RecyclerView.Adapter<?> a = getBindingAdapter();

                if (a != null) a.notifyItemChanged(getBindingAdapterPosition());
                RecyclerView.Adapter<?> adapter = getBindingAdapter();

                if (adapter instanceof NotificationsAdapter) {
                    NotificationsAdapter na = (NotificationsAdapter) adapter;

                    if (na.listener != null) {
                        na.listener.onNotificationToggle(e, getBindingAdapterPosition());
                    }
                }
            });
        }
    }
}

package com.example.eventlotto.functions.notifications;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
        void onEventClick(FollowedEvent event);
    }

    private final List<FollowedEvent> items;
    private static java.util.Map<String, String> statusByEid = new java.util.HashMap<>();
    @Nullable private final Listener listener;

    public NotificationsAdapter(List<FollowedEvent> items, @Nullable Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setStatusMap(java.util.Map<String, String> statusByEid) {
        this.statusByEid = statusByEid != null ? statusByEid : new java.util.HashMap<>();
        notifyDataSetChanged();
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
            // Prefer EventStatus from Firestore if available; otherwise, hide
            String status = statusByEid.get(e.getId());
            if (status != null && !status.isEmpty()) {
                textStatus.setVisibility(View.VISIBLE);
                textStatus.setText(formatStatus(status));
                Integer bg = statusBgFromString(status);
                if (bg != null) textStatus.setBackgroundResource(bg);
                else textStatus.setBackground(null);
            } else {
                textStatus.setText("");
                textStatus.setVisibility(View.GONE);
            }

            iconNotify.setImageResource(e.isNotificationsEnabled() //toggle between on and off
                    ? R.drawable.notification_on
                    : R.drawable.notification_off);

            iconNotify.setOnClickListener(v -> {
                if (e.isNotificationsEnabled()) {
                    View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_opt_out, null, false);
                    AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                            .setView(dialogView)
                            .create();
                    if (dialog.getWindow() != null) {
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }

                    View btnCancel = dialogView.findViewById(R.id.btn_cancel);
                    View btnOptOut = dialogView.findViewById(R.id.btn_opt_out);

                    btnCancel.setOnClickListener(vi -> dialog.dismiss());
                    btnOptOut.setOnClickListener(vi -> {
                        dialog.dismiss();
                        e.setNotificationsEnabled(false);
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
                    dialog.show();
                } else {
                    e.setNotificationsEnabled(true);
                    RecyclerView.Adapter<?> a = getBindingAdapter();
                    if (a != null) a.notifyItemChanged(getBindingAdapterPosition());
                    RecyclerView.Adapter<?> adapter = getBindingAdapter();
                    if (adapter instanceof NotificationsAdapter) {
                        NotificationsAdapter na = (NotificationsAdapter) adapter;
                        if (na.listener != null) {
                            na.listener.onNotificationToggle(e, getBindingAdapterPosition());
                        }
                    }
                }
            });

            itemView.setOnClickListener(v -> {
                RecyclerView.Adapter<?> adapter = getBindingAdapter();
                if (adapter instanceof NotificationsAdapter) {
                    NotificationsAdapter na = (NotificationsAdapter) adapter;
                    if (na.listener != null) {
                        na.listener.onEventClick(e);
                    }
                }
            });
        }
    }

    private static CharSequence formatStatus(String s) {
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

    private static Integer statusBgFromString(String s) {
        String k = s.toLowerCase();
        switch (k) {
            case "selected":
            case "signed up":
                return R.drawable.bg_status_accepted;
            case "waiting":
                return R.drawable.bg_status_waiting;
            case "not chosen":
            case "cancelled":
                return R.drawable.bg_status_not_chosen;
            default:
                return null;
        }
    }
}

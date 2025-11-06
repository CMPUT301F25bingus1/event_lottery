package com.example.eventlotto.functions.notifications;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.R;
import com.example.eventlotto.model.FollowedEvent;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    public interface Listener {
        void onNotificationToggle(FollowedEvent event, int position);
        void onEventClick(FollowedEvent event);
    }

    private final List<FollowedEvent> items;
    @Nullable private final Listener listener;

    public NotificationsAdapter(List<FollowedEvent> items, @Nullable Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    // removed status map; statuses now come live from Firestore per row

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
    public void onViewRecycled(@NonNull VH holder) {
        super.onViewRecycled(holder);
        if (holder.statusReg != null) {
            holder.statusReg.remove();
            holder.statusReg = null;
        }
        holder.boundEventId = null;
        holder.textStatus.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return items.size();
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
        @Nullable ListenerRegistration statusReg;
        @Nullable String boundEventId;

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

            // Clear previous status state and listener
            textStatus.setVisibility(View.GONE);
            if (statusReg != null) {
                statusReg.remove();
                statusReg = null;
            }

            String eid = e.getId();
            boundEventId = eid;

            String deviceId = Settings.Secure.getString(
                    itemView.getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            // Listen for this user's status under events/<eid>/status/<uid>
            statusReg = FirebaseFirestore.getInstance()
                    .collection("events").document(eid)
                    .collection("status").document(deviceId)
                    .addSnapshotListener((snap, err) -> {
                        if (boundEventId == null || !eid.equals(boundEventId)) return;
                        if (err != null || snap == null) {
                            textStatus.setVisibility(View.GONE);
                            return;
                        }
                        if (snap.exists()) {
                            String raw = snap.getString("status");
                            applyStatusChip(textStatus, raw);
                            textStatus.setVisibility(View.VISIBLE);
                        } else {
                            textStatus.setVisibility(View.GONE);
                        }
                    });

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

    private static void applyStatusChip(TextView tv, @Nullable String rawStatus) {
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

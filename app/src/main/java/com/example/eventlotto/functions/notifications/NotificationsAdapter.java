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

/**
 * Adapter class for displaying a list of followed events within a RecyclerView.
 * <p>
 * This adapter handles binding of event data, displaying notification toggle icons,
 * and listening for real-time updates of user-specific event status from Firestore.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    /**
     * Listener interface for handling user interactions with notifications and events.
     */
    public interface Listener {
        /**
         * Called when a user toggles the notification state for an event.
         * @param event    The {@link FollowedEvent} whose notification setting was changed.
         * @param position The adapter position of the affected item.
         */
        void onNotificationToggle(FollowedEvent event, int position);

        /**
         * Called when a user clicks on an event item.
         * @param event The {@link FollowedEvent} that was clicked.
         */
        void onEventClick(FollowedEvent event);
    }

    /** The list of followed events displayed in this adapter. */
    private final List<FollowedEvent> items;

    /** Optional listener for handling user interactions. */
    @Nullable private final Listener listener;

    /**
     * Constructs a new {@link NotificationsAdapter}.
     *
     * @param items    The list of {@link FollowedEvent} objects to display.
     * @param listener The listener to handle user actions (nullable).
     */
    public NotificationsAdapter(List<FollowedEvent> items, @Nullable Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    /**
     * Inflates the item layout for each event.
     *
     * @param parent   The parent {@link ViewGroup}.
     * @param viewType The view type (unused).
     * @return A new {@link VH} instance.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_event, parent, false);
        return new VH(v);
    }

    /**
     * Binds the event data to the given {@link VH}.
     *
     * @param holder   The ViewHolder instance.
     * @param position The position of the item being bound.
     */
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    /**
     * Cleans up listeners and resources when a ViewHolder is recycled.
     *
     * @param holder The ViewHolder being recycled.
     */
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

    /**
     * Returns the total number of events displayed.
     *
     * @return The number of items.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Replaces the current list of items with a new one and refreshes the view.
     *
     * @param newItems The new list of followed events.
     */
    public void setItems(List<FollowedEvent> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class that represents each event item in the RecyclerView.
     * <p>
     * This class manages view binding, click listeners, and Firestore snapshot listeners
     * for event status updates.
     */
    static class VH extends RecyclerView.ViewHolder {

        /** Notification toggle icon. */
        final ImageView iconNotify;

        /** Event thumbnail image. */
        final ImageView imageEvent;

        /** Event name text view. */
        final TextView textName;

        /** Event status chip text view. */
        final TextView textStatus;

        /** Event description text view. */
        final TextView textDescription;

        /** Firestore listener registration for real-time event status updates. */
        @Nullable ListenerRegistration statusReg;

        /** The ID of the event currently bound to this ViewHolder. */
        @Nullable String boundEventId;

        /**
         * Constructs a ViewHolder and initializes its views.
         *
         * @param itemView The root view of the item layout.
         */
        VH(@NonNull View itemView) {
            super(itemView);
            iconNotify = itemView.findViewById(R.id.iconNotify);
            imageEvent = itemView.findViewById(R.id.imageEvent);
            textName = itemView.findViewById(R.id.textName);
            textStatus = itemView.findViewById(R.id.textStatus);
            textDescription = itemView.findViewById(R.id.textDescription);
        }

        /**
         * Binds a {@link FollowedEvent} to the ViewHolder and sets up UI interactions.
         *
         * @param e The event to bind.
         */
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

            // Get device ID to track user-specific status
            String deviceId = Settings.Secure.getString(
                    itemView.getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            // Listen for user's event status under events/<eid>/status/<deviceId>
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

            // Set the notification icon based on current state
            iconNotify.setImageResource(e.isNotificationsEnabled()
                    ? R.drawable.notification_on
                    : R.drawable.notification_off);

            // Handle notification toggle click
            iconNotify.setOnClickListener(v -> {
                if (e.isNotificationsEnabled()) {
                    // Show opt-out confirmation dialog
                    View dialogView = LayoutInflater.from(v.getContext())
                            .inflate(R.layout.dialog_opt_out, null, false);
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
                        RecyclerView.Adapter<?> adapter = getBindingAdapter();
                        if (adapter != null) adapter.notifyItemChanged(getBindingAdapterPosition());
                        if (adapter instanceof NotificationsAdapter) {
                            NotificationsAdapter na = (NotificationsAdapter) adapter;
                            if (na.listener != null) {
                                na.listener.onNotificationToggle(e, getBindingAdapterPosition());
                            }
                        }
                    });
                    dialog.show();
                } else {
                    // Enable notifications
                    e.setNotificationsEnabled(true);
                    RecyclerView.Adapter<?> adapter = getBindingAdapter();
                    if (adapter != null) adapter.notifyItemChanged(getBindingAdapterPosition());
                    if (adapter instanceof NotificationsAdapter) {
                        NotificationsAdapter na = (NotificationsAdapter) adapter;
                        if (na.listener != null) {
                            na.listener.onNotificationToggle(e, getBindingAdapterPosition());
                        }
                    }
                }
            });

            // Handle event item click
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

    /**
     * Applies a visual chip style and label to a status TextView based on a raw status string.
     *
     * @param tv         The TextView to apply the status to.
     * @param rawStatus  The raw status string (e.g., "selected", "cancelled", etc.).
     */
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

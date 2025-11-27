package com.example.eventlotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment providing an administrative view of all notifications sent by organizers.
 * <p>
 * This screen retrieves every document within the top-level {@code notifications} collection
 * and renders each entry in chronological order with newest being first. Each document corresponds
 * to a notification sent to an entrant regarding a specific event. This fragment is visible
 * only to admin's.
 * </p>
 *
 * Implements: Getting all of notification from Firestore,
 * Displaying them with the user, event name, time stamp, and notification content
 */
public class AdmNotificationsFragment extends Fragment {

    /** RecyclerView displaying notification log entries. */
    private RecyclerView recyclerView;

    /** Progress bar shown while data is loaded from Firestore. */
    private ProgressBar progressBar;

    /** Text displayed if no notifications are available. */
    private TextView emptyView;

    /** Full snapshot list of notification documents returned from Firestore. */
    private final List<DocumentSnapshot> docs = new ArrayList<>();

    /** Firestore instance for database access. */
    private FirebaseFirestore db;

    /** Adapter providing rendering logic for notification cards. */
    private NotificationsAdapter adapter;

    /**
     * Inflates the fragment layout and initializes UI components.
     *
     * @param inflater Layout inflater for XML conversion.
     * @param container Optional parent container.
     * @param savedInstanceState Previous state bundle (unused here).
     * @return The fully constructed Fragment view hierarchy.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_adm_notifications, container, false);

        recyclerView = root.findViewById(R.id.recycler_notifications);
        progressBar = root.findViewById(R.id.progress_notifications);
        emptyView = root.findViewById(R.id.empty_notifications);

        db = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter(docs);
        recyclerView.setAdapter(adapter);

        fetchNotifications();

        return root;
    }

    /**
     * Fetches all documents within the Firestore {@code notifications} collection.
     * <p>
     * Documents are ordered by their {@code createdAt} timestamp in descending order,
     * ensuring the newest notifications appear first. Once documents are loaded, the UI is
     * updated to reflect the results.
     * </p>
     */
    private void fetchNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        db.collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    docs.clear();
                    docs.addAll(query.getDocuments());

                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (docs.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    if (getContext() != null) {
                        Toast.makeText(
                                getContext(),
                                "Failed to load notifications: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    emptyView.setVisibility(View.VISIBLE);
                });
    }

    /**
     * RecyclerView adapter responsible for creating and binding notification card views.
     * <p>
     * This adapter reads directly from Firestore document snapshots rather than using
     * a model class, ensuring compatibility with any future schema changes.
     * </p>
     */
    private static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

        /** Data set of Firestore document snapshots representing notifications. */
        private final List<DocumentSnapshot> items;

        /**
         * Constructs a new adapter for the provided dataset.
         *
         * @param items Notification documents to render.
         */
        NotificationsAdapter(List<DocumentSnapshot> items) {
            this.items = items != null ? items : new ArrayList<>();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.admin_card_notification, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            DocumentSnapshot doc = items.get(position);

            String message = safeString(doc.getString("message"));
            String eid = safeString(doc.getString("eid"));
            String uid = safeString(doc.getString("uid"));
            Object tsObj = doc.get("createdAt");

            String createdText = "Created: N/A";
            if (tsObj instanceof Timestamp) {
                DateFormat df = DateFormat.getDateTimeInstance();
                createdText = "Created: " + df.format(((Timestamp) tsObj).toDate());
            }

            holder.txtMessage.setText(message.isEmpty() ? "(no message)" : message);
            holder.txtEventId.setText("Event: " + eid);
            holder.txtUserId.setText("Entrant: " + uid);
            holder.txtCreatedAt.setText(createdText);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        /**
         * Ensures a non-null {@code String} value is returned.
         *
         * @param s Source string value.
         * @return The original string, or an empty string if null.
         */
        private static String safeString(String s) {
            return s == null ? "" : s;
        }

        /**
         * ViewHolder representing a single notification entry.
         */
        static class VH extends RecyclerView.ViewHolder {

            /** Notification message text. */
            final TextView txtMessage;

            /** Event identifier associated with this notification. */
            final TextView txtEventId;

            /** ID of the entrant who received the notification. */
            final TextView txtUserId;

            /** Timestamp describing when the notification was created. */
            final TextView txtCreatedAt;

            VH(@NonNull View itemView) {
                super(itemView);
                txtMessage = itemView.findViewById(R.id.txt_notification_message);
                txtEventId = itemView.findViewById(R.id.txt_notification_event);
                txtUserId = itemView.findViewById(R.id.txt_notification_user);
                txtCreatedAt = itemView.findViewById(R.id.txt_notification_created_at);
            }
        }
    }
}

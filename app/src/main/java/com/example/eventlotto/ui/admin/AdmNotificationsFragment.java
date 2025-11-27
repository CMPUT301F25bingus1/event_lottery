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

public class AdmNotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;

    private final List<DocumentSnapshot> docs = new ArrayList<>(); // added
    private FirebaseFirestore db; // added
    private NotificationsAdapter adapter; // added

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_adm_notifications, container, false);

        recyclerView = root.findViewById(R.id.recycler_notifications);
        progressBar = root.findViewById(R.id.progress_notifications);
        emptyView = root.findViewById(R.id.empty_notifications);

        db = FirebaseFirestore.getInstance(); // added

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter(docs); // added
        recyclerView.setAdapter(adapter);

        fetchNotifications();

        return root;
    }

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

    private static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

        private final List<DocumentSnapshot> items;

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

        private static String safeString(String s) {
            return s == null ? "" : s;
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView txtMessage;
            final TextView txtEventId;
            final TextView txtUserId;
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

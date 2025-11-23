package com.example.eventlotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.example.eventlotto.model.User;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for admin users to view and manage all user profiles.
 * <p>
 * Displays users in a RecyclerView with the option to delete each profile.
 * </p>
 */
public class Adm_ProfilesFragment extends Fragment {

    /** List of all users retrieved from Firestore. */
    private final List<User> users = new ArrayList<>();

    /** RecyclerView adapter for displaying users. */
    private UsersAdapter adapter;

    /** Service class for interacting with Firestore. */
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_profiles, container, false);

        firestoreService = new FirestoreService();

        RecyclerView recyclerView = root.findViewById(R.id.recycler_admin_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new UsersAdapter(users, this::deleteUser);
        recyclerView.setAdapter(adapter);

        loadUsers();

        return root;
    }

    /**
     * Fetches all users from Firestore and updates the RecyclerView.
     */
    private void loadUsers() {
        firestoreService.users().get()
                .addOnSuccessListener(snapshots -> {
                    users.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setUid(doc.getId());
                            users.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Deletes a user by their UID and refreshes the list.
     *
     * @param uid User ID
     */
    private void deleteUser(String uid) {
        firestoreService.deleteUser(uid, success -> {
            Toast.makeText(getContext(), success ? "Profile deleted" : "Delete failed", Toast.LENGTH_SHORT).show();
            loadUsers();
        });
    }

    /**
     * RecyclerView adapter for displaying admin user profiles.
     */
    private static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.VH> {

        /** Listener interface for delete actions. */
        interface Listener {
            void onDelete(String uid);
        }

        private final List<User> items;
        private final Listener listener;

        UsersAdapter(List<User> items, Listener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_user, parent, false);
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
         * ViewHolder for individual user items.
         */
        static class VH extends RecyclerView.ViewHolder {
            VH(@NonNull View itemView) {
                super(itemView);
            }

            void bind(User user, Listener listener) {
                android.widget.TextView name = itemView.findViewById(R.id.text_user_name);
                android.widget.TextView email = itemView.findViewById(R.id.text_user_email);
                android.widget.TextView role = itemView.findViewById(R.id.text_user_role);
                View deleteBtn = itemView.findViewById(R.id.btn_delete_user);

                name.setText(user.getFullName() != null ? user.getFullName() : user.getUid());
                email.setText(user.getEmail() != null ? user.getEmail() : "");

                if (user.getRole() != null) {
                    String formatRole = user.getRole().substring(0, 1).toUpperCase() +
                            user.getRole().substring(1).toLowerCase();
                    role.setText("Role: " + formatRole);
                } else {
                    role.setText("Role: Unknown");
                }

                deleteBtn.setOnClickListener(v -> {
                    if (listener != null && user.getUid() != null) {
                        listener.onDelete(user.getUid());
                    }
                });
            }
        }
    }
}

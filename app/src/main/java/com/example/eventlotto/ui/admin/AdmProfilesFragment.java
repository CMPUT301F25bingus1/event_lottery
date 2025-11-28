package com.example.eventlotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.example.eventlotto.model.User;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class AdmProfilesFragment extends Fragment {

    private final List<User> users = new ArrayList<>();

    private UsersAdapter adapter;
    private FirestoreService firestoreService;
    private Button btnOrganizers, btnUsers;
    private String currentFilter = "organizer"; // Default to organizers

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_profiles, container, false);

        firestoreService = new FirestoreService();

        RecyclerView recyclerView = root.findViewById(R.id.recycler_admin_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnOrganizers = root.findViewById(R.id.btn_organizers);
        btnUsers = root.findViewById(R.id.btn_users);

        adapter = new UsersAdapter(users, this::showUserDetails);
        recyclerView.setAdapter(adapter);

        // Tab switching
        btnOrganizers.setOnClickListener(v -> switchTab("organizer"));
        btnUsers.setOnClickListener(v -> switchTab("entrant"));

        loadUsers();

        return root;
    }

    private void switchTab(String role) {
        currentFilter = role;

        // Update tab UI
        if (role.equals("organizer")) {
            btnOrganizers.setBackgroundResource(R.drawable.bg_tab_selected);
            btnUsers.setBackgroundResource(R.drawable.bg_tab_unselected);
        } else {
            btnOrganizers.setBackgroundResource(R.drawable.bg_tab_unselected);
            btnUsers.setBackgroundResource(R.drawable.bg_tab_selected);
        }

        loadUsers();
    }

    private void loadUsers() {
        firestoreService.users().get()
                .addOnSuccessListener(snapshots -> {
                    users.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setUid(doc.getId());

                            // Filter by role
                            String userRole = user.getRole() != null ? user.getRole().toLowerCase() : "";
                            if (userRole.equals(currentFilter)) {
                                users.add(user);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show()
                );
    }

    private void showUserDetails(User user) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_admin_user_details, null, false);

        TextView name = dialogView.findViewById(R.id.detail_name);
        TextView role = dialogView.findViewById(R.id.detail_role);
        TextView email = dialogView.findViewById(R.id.detail_email);
        TextView phone = dialogView.findViewById(R.id.detail_phone);
        TextView device = dialogView.findViewById(R.id.detail_device);
        TextView avatar = dialogView.findViewById(R.id.detail_avatar);
        TextView created = dialogView.findViewById(R.id.detail_created);

        String displayName = user.getFullName() != null && !user.getFullName().isEmpty()
                ? user.getFullName()
                : (user.getEmail() != null ? user.getEmail() : "Unknown");
        name.setText(displayName);

        String prettyRole = user.getRole() != null && !user.getRole().isEmpty()
                ? user.getRole().substring(0, 1).toUpperCase() + user.getRole().substring(1).toLowerCase()
                : "User";
        role.setText(prettyRole);

        String emailVal = user.getEmail() != null && !user.getEmail().isEmpty()
                ? user.getEmail() : "No email on file";
        String phoneVal = user.getPhone() != null && !user.getPhone().isEmpty()
                ? user.getPhone() : "No phone on file";
        email.setText("Email: " + emailVal);
        phone.setText("Phone: " + phoneVal);
        device.setText("Device ID: " + (user.getDeviceId() != null && !user.getDeviceId().isEmpty()
                ? user.getDeviceId() : user.getUid()));

        String createdVal = "Unknown";
        if (user.getCreatedAt() != null) {
            createdVal = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT
            ).format(user.getCreatedAt().toDate());
        }
        if (created != null) {
            created.setText("Created: " + createdVal);
        }

        if (avatar != null) {
            String initial = displayName != null && !displayName.trim().isEmpty()
                    ? displayName.trim().substring(0, 1).toUpperCase()
                    : "?";
            avatar.setText(initial);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_close_user).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_delete_user).setOnClickListener(v -> {
            dialog.dismiss();
            confirmDelete(user);
        });

        dialog.show();
    }

    private void confirmDelete(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete user?")
                .setMessage("Remove " + (user.getFullName() != null ? user.getFullName() : "this user") + " permanently?")
                .setPositiveButton("Delete", (d, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        firestoreService.deleteUser(user.getUid(), success -> {
            Toast.makeText(getContext(), "User deleted", Toast.LENGTH_SHORT).show();
            loadUsers();
        });
    }

    private static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.VH> {

        private final List<User> items;
        private final OnUserClick listener;

        UsersAdapter(List<User> items, OnUserClick listener) {
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

        static class VH extends RecyclerView.ViewHolder {
            TextView name;
            TextView subtitle;
            TextView roleChip;
            TextView avatarInitial;

            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.text_user_name);
                subtitle = itemView.findViewById(R.id.text_user_subtitle);
                roleChip = itemView.findViewById(R.id.text_user_role_chip);
                avatarInitial = itemView.findViewById(R.id.avatar_initial);
            }

            void bind(User user, OnUserClick listener) {
                String displayName = user.getFullName() != null ? user.getFullName() :
                        (user.getEmail() != null ? user.getEmail() : user.getUid());
                name.setText(displayName);

                String secondary = user.getEmail();
                if (secondary == null || secondary.trim().isEmpty()) secondary = user.getPhone();
                if (secondary == null || secondary.trim().isEmpty()) secondary = user.getUid();
                if (subtitle != null) subtitle.setText(secondary);

                if (roleChip != null) {
                    String role = user.getRole() != null ? user.getRole() : "user";
                    String prettyRole = role.isEmpty() ? "User"
                            : role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                    roleChip.setText(prettyRole);
                }

                if (avatarInitial != null) {
                    String initial = displayName != null && !displayName.trim().isEmpty()
                            ? displayName.trim().substring(0, 1).toUpperCase()
                            : "?";
                    avatarInitial.setText(initial);
                }

                itemView.setOnClickListener(v -> listener.onClick(user));
            }
        }
    }

    private interface OnUserClick {
        void onClick(User user);
    }
}
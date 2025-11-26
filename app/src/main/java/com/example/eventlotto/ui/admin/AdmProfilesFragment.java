package com.example.eventlotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdmProfilesFragment extends Fragment {

    private final List<User> users = new ArrayList<>();
    private final Set<String> selectedUserIds = new HashSet<>();

    private UsersAdapter adapter;
    private FirestoreService firestoreService;
    private Button btnOrganizers, btnUsers, btnDelete;
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
        btnDelete = root.findViewById(R.id.btn_delete);
        Button btnCancel = root.findViewById(R.id.btn_cancel);

        adapter = new UsersAdapter(users, selectedUserIds);
        recyclerView.setAdapter(adapter);

        // Tab switching
        btnOrganizers.setOnClickListener(v -> switchTab("organizer"));
        btnUsers.setOnClickListener(v -> switchTab("entrant"));

        // Delete button
        btnDelete.setOnClickListener(v -> {
            if (selectedUserIds.isEmpty()) {
                Toast.makeText(getContext(), "No users selected", Toast.LENGTH_SHORT).show();
            } else {
                showDeleteConfirmationDialog();
            }
        });

        // Cancel button - clear selections
        btnCancel.setOnClickListener(v -> {
            selectedUserIds.clear();
            adapter.notifyDataSetChanged();
        });

        loadUsers();

        return root;
    }

    private void switchTab(String role) {
        currentFilter = role;
        selectedUserIds.clear();

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

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Selected?")
                .setMessage("Are you sure you want to delete selected user/organizers?")
                .setCancelable(true)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes", (dialog, which) -> deleteSelectedUsers())
                .show();
    }

    private void deleteSelectedUsers() {
        int totalToDelete = selectedUserIds.size();
        int[] deleteCount = {0};

        for (String uid : new ArrayList<>(selectedUserIds)) {
            firestoreService.deleteUser(uid, success -> {
                deleteCount[0]++;
                if (deleteCount[0] == totalToDelete) {
                    Toast.makeText(getContext(), "Deleted " + totalToDelete + " user(s)", Toast.LENGTH_SHORT).show();
                    selectedUserIds.clear();
                    loadUsers();
                }
            });
        }
    }

    private static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.VH> {

        private final List<User> items;
        private final Set<String> selectedIds;

        UsersAdapter(List<User> items, Set<String> selectedIds) {
            this.items = items;
            this.selectedIds = selectedIds;
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
            holder.bind(items.get(position), selectedIds);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView name;
            CheckBox checkbox;

            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.text_user_name);
                checkbox = itemView.findViewById(R.id.checkbox_select_user);
            }

            void bind(User user, Set<String> selectedIds) {
                String displayName = user.getFullName() != null ? user.getFullName() :
                        (user.getEmail() != null ? user.getEmail() : user.getUid());
                name.setText(displayName);

                boolean isSelected = selectedIds.contains(user.getUid());
                checkbox.setChecked(isSelected);

                // Toggle selection on click
                itemView.setOnClickListener(v -> {
                    if (selectedIds.contains(user.getUid())) {
                        selectedIds.remove(user.getUid());
                        checkbox.setChecked(false);
                    } else {
                        selectedIds.add(user.getUid());
                        checkbox.setChecked(true);
                    }
                });

                checkbox.setOnClickListener(v -> {
                    if (checkbox.isChecked()) {
                        selectedIds.add(user.getUid());
                    } else {
                        selectedIds.remove(user.getUid());
                    }
                });
            }
        }
    }
}
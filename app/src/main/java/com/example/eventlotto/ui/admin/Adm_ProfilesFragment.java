package com.example.eventlotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class Adm_ProfilesFragment extends Fragment {

    private final List<User> users = new ArrayList<>();
    private Adm_ProfilesFragment.UsersAdapter adapter;
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_profiles, container, false);
        firestoreService = new FirestoreService();

        RecyclerView rv = v.findViewById(R.id.recycler_admin_users);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UsersAdapter(users, uid -> deleteUser(uid));
        rv.setAdapter(adapter);

        loadUsers();
        return v;
    }

    private void loadUsers() {
        firestoreService.users().get()
                .addOnSuccessListener(snaps -> {
                    users.clear();
                    for (DocumentSnapshot d : snaps) {
                        User u = d.toObject(User.class);
                        if (u != null) {
                            u.setUid(d.getId());
                            users.add(u);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show());
    }

    private void deleteUser(String uid) {
        firestoreService.deleteUser(uid, ok -> {
            Toast.makeText(getContext(), ok ? "Profile deleted" : "Delete failed", Toast.LENGTH_SHORT).show();
            loadUsers();
        });
    }

    private static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.VH> {
        interface Listener { void onDelete(String uid); }
        private final List<User> items; private final Listener listener;
        UsersAdapter(List<User> items, Listener listener) { this.items = items; this.listener = listener; }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos), listener); }
        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            VH(@NonNull View itemView) { super(itemView); }
            void bind(User u, Listener listener) {
                android.widget.TextView name = itemView.findViewById(R.id.text_user_name);
                android.widget.TextView email = itemView.findViewById(R.id.text_user_email);
                View btn = itemView.findViewById(R.id.btn_delete_user);
                name.setText(u.getFullName() != null ? u.getFullName() : u.getUid());
                email.setText(u.getEmail() != null ? u.getEmail() : "");
                btn.setOnClickListener(v -> { if (listener != null && u.getUid() != null) listener.onDelete(u.getUid()); });
            }
        }
    }
}


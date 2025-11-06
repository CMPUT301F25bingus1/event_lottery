package com.example.eventlotto.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.example.eventlotto.model.User;
import com.example.eventlotto.ui.entrant.Ent_EntryFragment;

public class LoginFragment extends Fragment {

    private EditText nameField, emailField, phoneField;
    private Button updateBtn, deleteBtn;
    private FirestoreService firestoreService;
    private String deviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profiles, container, false);

        firestoreService = new FirestoreService();
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        nameField = view.findViewById(R.id.input_name);
        emailField = view.findViewById(R.id.input_email);
        phoneField = view.findViewById(R.id.input_phone);
        updateBtn = view.findViewById(R.id.btn_update_profile);
        deleteBtn = view.findViewById(R.id.btn_delete_profile);

        loadProfile();

        updateBtn.setOnClickListener(v -> updateProfile());
        deleteBtn.setOnClickListener(v -> confirmDelete());

        return view;
    }

    private void loadProfile() {
        firestoreService.getUser(deviceId).addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                User user = snapshot.toObject(User.class);
                if (user != null) {
                    nameField.setText(user.getFullName());
                    emailField.setText(user.getEmail());
                    phoneField.setText(user.getPhone());
                }
            }
        });
    }

    private void updateProfile() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Name and Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        User updatedUser = new User(name, email, phone, deviceId);
        firestoreService.saveUser(updatedUser)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed.", Toast.LENGTH_SHORT).show());
    }

    private void confirmDelete() {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete your profile?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProfile() {
        firestoreService.deleteUser(deviceId, success -> {
            if (success) {
                Toast.makeText(getContext(), "Profile deleted.", Toast.LENGTH_LONG).show();

                // Go back to WelcomeFragment
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new Ent_EntryFragment())
                        .commit();
            } else {
                Toast.makeText(getContext(), "Error deleting profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

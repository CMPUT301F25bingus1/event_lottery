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

        // Load existing user info
        firestoreService.getUser(deviceId)
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            nameField.setText(user.getFullName());
                            emailField.setText(user.getEmail());
                            phoneField.setText(user.getPhone());
                        }
                    } else {
                        Toast.makeText(requireContext(), "No existing profile found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load profile.", Toast.LENGTH_SHORT).show()
                );

        updateBtn.setOnClickListener(v -> updateUserProfile());
        deleteBtn.setOnClickListener(v -> confirmDeleteProfile());

        return view;
    }

    private void updateUserProfile() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        User updatedUser = new User(deviceId, name, email, phone);

        firestoreService.updateUserProfile(updatedUser, success -> {
            if (success) {
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
                Toast.makeText(getContext(), "Profile deleted.", Toast.LENGTH_LONG).show();

                // Go back to WelcomeFragment
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new Ent_EntryFragment())
                        .commit();
=======
                Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
>>>>>>> Stashed changes
=======
                Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
>>>>>>> Stashed changes
=======
                Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
>>>>>>> Stashed changes
=======
                Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
>>>>>>> Stashed changes
            } else {
                Toast.makeText(requireContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteProfile() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete, null);
        AlertDialog confirmDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(v -> confirmDialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            confirmDialog.dismiss();
            deleteUserProfile();
        });

        confirmDialog.show();
    }

    private void deleteUserProfile() {
        firestoreService.deleteUserProfile(deviceId, success -> {
            if (success) {
                View deletedView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile_deleted, null);
                AlertDialog deletedDialog = new AlertDialog.Builder(requireContext())
                        .setView(deletedView)
                        .setCancelable(false)
                        .create();

                Button btnGotIt = deletedView.findViewById(R.id.btnGotIt);
                btnGotIt.setOnClickListener(v -> {
                    deletedDialog.dismiss();

                    View navBar = requireActivity().findViewById(R.id.bottom_navigation);
                    if (navBar != null) {
                        navBar.setVisibility(View.GONE);
                    }

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new WelcomeFragment())
                            .commit();
                });

                deletedDialog.show();
            } else {
                Toast.makeText(requireContext(), "Failed to delete profile. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

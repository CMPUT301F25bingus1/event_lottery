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
import com.example.eventlotto.MainActivity;
import com.example.eventlotto.R;
import com.example.eventlotto.model.User;
import com.example.eventlotto.ui.entrant.Ent_WelcomeFragment;

/**
 * Fragment for managing a user's profile in the EventLotto app.
 * <p>
 * Provides functionality to view, update, and delete the current user's profile.
 * Uses the device ID as a unique identifier for the user and interacts with Firestore
 * via {@link FirestoreService}.
 * </p>
 */
public class LoginFragment extends Fragment {

    /** Input user's name. */
    private EditText nameField;

    /** Input user's email. */
    private EditText emailField;

    /** Input phone number. */
    private EditText phoneField;

    /** Button that updates the user's profile. */
    private Button updateBtn;

    /** Button that deletes the user's profile. */
    private Button deleteBtn;

    /** Firestore service instance to handle user data operations. */
    private FirestoreService firestoreService;

    /** Unique device ID used as the user's identifier. */
    private String deviceId;

    /**
     * Called to have the fragment instantiate its user interface view.
     * <p>
     * This method inflates the {@link R.layout#fragment_profiles} layout, initializes input fields,
     * loads existing user data from Firestore, and sets up the update and delete buttons.
     * </p>
     *
     * @param inflater The LayoutInflater used to inflate the fragment's view.
     * @param container The parent view that the fragment's UI should attach to.
     * @param savedInstanceState If non-null, the fragment is being re-created from a previous state.
     * @return The root {@link View} for the fragment's layout.
     */
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

        loadUserProfile();

        updateBtn.setOnClickListener(v -> updateUserProfile());
        deleteBtn.setOnClickListener(v -> confirmDeleteProfile());

        return view;
    }

    /** Loads the current user's profile from Firestore and populates the input fields. */
    private void loadUserProfile() {
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
    }

    /** Updates the user's profile in Firestore with the values from the input fields. */
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
                Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Shows a confirmation dialog before deleting the user's profile. */
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

    /** Deletes the user's profile from Firestore and navigates back to the welcome screen. */
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

                    ((MainActivity) requireActivity()).hideBottomNavigation();

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new Ent_WelcomeFragment())
                            .commit();
                });

                deletedDialog.show();
            } else {
                Toast.makeText(requireContext(), "Failed to delete profile. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
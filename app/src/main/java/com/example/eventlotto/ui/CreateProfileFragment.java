package com.example.eventlotto.ui;

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
import com.example.eventlotto.ui.entrant.Ent_HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Fragment that allows a new user (entrant) to create their profile in the EventLotto app.
 * <p>
 * This fragment collects basic user information such as name, email, and phone number,
 * associates it with the unique device ID, and stores the profile in Firestore.
 * Once the profile is successfully created, the user is redirected to the entrant home screen.
 * </p>
 */
public class CreateProfileFragment extends Fragment {

    /** Input field for the user's full name. */
    private EditText inputName;

    /** Input field for the user's email address. */
    private EditText inputEmail;

    /** Input field for the user's phone number. */
    private EditText inputPhone;

    /** Button that submits the profile creation form. */
    private Button btnCreateProfile;

    /** Firestore service responsible for user data storage and retrieval. */
    private FirestoreService firestoreService;

    /** Unique device ID used to identify the user. */
    private String deviceId;

    /**
     * Called to create and return the view hierarchy associated with this fragment.
     * <p>
     * This method inflates the profile creation layout, initializes input fields, and
     * sets up the button that triggers user profile creation.
     * </p>
     *
     * @param inflater  The {@link LayoutInflater} used to inflate the fragment's layout.
     * @param container The parent view that this fragment's UI should attach to.
     * @param savedInstanceState If non-null, the fragment is being re-created from a previous state.
     * @return The root {@link View} for the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_profile, container, false);

        // Initialize Firestore service and UI elements
        firestoreService = new FirestoreService();
        inputName = view.findViewById(R.id.input_name);
        inputEmail = view.findViewById(R.id.input_email);
        inputPhone = view.findViewById(R.id.input_phone);
        btnCreateProfile = view.findViewById(R.id.btn_create_profile);

        // Retrieve the device's unique ID to use as a user identifier
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Set up the "Create Profile" button listener
        btnCreateProfile.setOnClickListener(v -> createUserProfile());

        return view;
    }

    /**
     * Handles the profile creation process.
     * <p>
     * This method checks the user input fields, creates a User object,
     * and saves the data to Firestore via FirestoreService.
     * If the save is successful, the user is navigated to Ent_HomeFragment
     * and the bottom navigation bar is initialized for the entrant role.
     * </p>
     */
    private void createUserProfile() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user profile
        User user = new User(deviceId, name, email, phone);

        firestoreService.saveUserProfile(user, success -> {
            if (success) {
                Toast.makeText(requireContext(), "Profile created successfully!", Toast.LENGTH_SHORT).show();

                // Navigate to entrant home screen
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Ent_HomeFragment())
                        .commit();
                ((MainActivity) requireActivity()).initBottomNavForRole("entrant");

                // Make bottom navigation visible
                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(requireContext(), "Failed to create profile. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

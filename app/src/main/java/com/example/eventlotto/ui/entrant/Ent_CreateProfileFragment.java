package com.example.eventlotto.ui.entrant;

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

/**
 * Fragment that allows entrants to create their user profile in the EventLotto app.
 * <p>
 * This fragment collects the user's name, email, and phone number, associates
 * them with a unique device ID, and saves the information to Firestore.
 * After successful profile creation, the user is redirected to the entrant home screen.
 */
public class Ent_CreateProfileFragment extends Fragment {

    /** Input field for user's name. */
    private EditText inputName;

    /** Input field for user's email. */
    private EditText inputEmail;

    /** Input field for user's phone number. */
    private EditText inputPhone;

    /** Button to trigger profile creation. */
    private Button btnCreateProfile;

    /** Service class responsible for Firestore operations. */
    private FirestoreService firestoreService;

    /** The unique device ID used as the user's identifier. */
    private String deviceId;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater  The {@link LayoutInflater} object that can be used to inflate views.
     * @param container The parent view that this fragment's UI should attach to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed.
     * @return The root view for the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_create_profile, container, false);

        // Initialize Firestore service
        firestoreService = new FirestoreService();

        // Bind UI elements
        inputName = view.findViewById(R.id.input_name);
        inputEmail = view.findViewById(R.id.input_email);
        inputPhone = view.findViewById(R.id.input_phone);
        btnCreateProfile = view.findViewById(R.id.btn_create_profile);

        // Retrieve device ID to serve as a unique identifier for the user
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Hide bottom navigation while creating a profile
        ((MainActivity) requireActivity()).hideBottomNavigation();

        // Set up button listener
        btnCreateProfile.setOnClickListener(v -> createUserProfile());

        return view;
    }

    /**
     * Validates input fields and creates a new user profile in Firestore.
     * If successful, the method navigates the user to the {@link Ent_HomeFragment}
     * and initializes the bottom navigation for the entrant role.
     * Otherwise, an error message is displayed.
     */
    private void createUserProfile() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        // Validate user input
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user object
        User user = new User(deviceId, name, email, phone);

        // Save user profile to Firestore
        firestoreService.saveUserProfile(user, success -> {
            if (success) {
                Toast.makeText(requireContext(), "Profile created successfully!", Toast.LENGTH_SHORT).show();

                // Navigate to home fragment
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Ent_HomeFragment())
                        .commit();

                // Initialize bottom navigation for entrant role
                ((MainActivity) requireActivity()).initBottomNavForRole("entrant");
            } else {
                Toast.makeText(requireContext(), "Failed to create profile. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

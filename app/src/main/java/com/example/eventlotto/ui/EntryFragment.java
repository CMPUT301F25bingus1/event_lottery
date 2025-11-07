package com.example.eventlotto.ui;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.MainActivity;
import com.example.eventlotto.R;
import com.example.eventlotto.ui.entrant.Ent_HomeFragment;

/**
 * Fragment responsible for determining whether a user already has a profile
 * or needs to create one.
 * <p>
 * This fragment checks Firestore for a user record associated with the device ID.
 * If the user exists, it navigates to the entrant home screen ({@link Ent_HomeFragment}).
 * If the user does not exist, it shows a button to create a new profile.
 * </p>
 */
public class EntryFragment extends Fragment {

    /** Firestore service for querying user profiles. */
    private FirestoreService firestoreService;

    /** Button to navigate to the profile creation screen. */
    private Button createProfileBtn;

    /** Optional progress indicator while checking user existence. */
    private ProgressBar progressBar;

    /**
     * Called to create and return the view hierarchy associated with this fragment.
     * <p>
     * Inflates the welcome layout, initializes UI elements, and performs a Firestore check
     * to determine if a user profile already exists.
     * </p>
     *
     * @param inflater  LayoutInflater used to inflate the fragment's layout.
     * @param container Parent view that this fragment's UI should attach to.
     * @param savedInstanceState If non-null, fragment is being re-created from a previous state.
     * @return The root view for this fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        firestoreService = new FirestoreService();
        createProfileBtn = view.findViewById(R.id.btn_go_to_create_profile);

        // Retrieve device ID to identify the user
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Check Firestore if user already exists
        firestoreService.getUser(deviceId).addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new Ent_HomeFragment())
                        .commit();

                // Initialize bottom navigation for entrant
                ((MainActivity) requireActivity()).initBottomNavForRole("entrant");
            } else {
                createProfileBtn.setVisibility(View.VISIBLE);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            // Error fetching user: show create profile button
            createProfileBtn.setVisibility(View.VISIBLE);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        });

        // Navigate to profile creation when button is clicked
        createProfileBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CreateProfileFragment())
                    .addToBackStack(null)
                    .commit();
            ((MainActivity) requireActivity()).initBottomNavForRole("entrant");
        });

        return view;
    }
}

package com.example.eventlotto.ui.entrant;

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
import com.example.eventlotto.R;
import com.example.eventlotto.ui.LoginFragment;

/**
 * Fragment that is the entry point for an entrant.
 * Checks if the user already exists in Firestore then continues.
 * If the user doesn't exist, provides a button to create a profile.
 */
public class Ent_EntryFragment extends Fragment {

    /** Firestore service helper for interacting with user data. */
    private FirestoreService firestoreService;
    private Button createProfileBtn;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

<<<<<<< Updated upstream:app/src/main/java/com/example/eventlotto/ui/entrant/Ent_EntryFragment.java
        View view = inflater.inflate(R.layout.fragment_login, container, false);
=======
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
>>>>>>> Stashed changes:app/src/main/java/com/example/eventlotto/ui/EntryFragment.java

        firestoreService = new FirestoreService();
        createProfileBtn = view.findViewById(R.id.btn_go_to_create_profile);


        // Gets the current device ID
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(),Settings.Secure.ANDROID_ID);

<<<<<<< Updated upstream:app/src/main/java/com/example/eventlotto/ui/entrant/Ent_EntryFragment.java
        // Checks if the entrant already exists in Firestore
        firestoreService.getUser(deviceId).addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // If yes, navigate to Profile screen
                getParentFragmentManager()
=======

        firestoreService.getUser(deviceId).addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {

                requireActivity().getSupportFragmentManager()
>>>>>>> Stashed changes:app/src/main/java/com/example/eventlotto/ui/EntryFragment.java
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();


                View navView = requireActivity().findViewById(R.id.bottom_navigation);
                if (navView != null) navView.setVisibility(View.VISIBLE);

            } else {
<<<<<<< Updated upstream:app/src/main/java/com/example/eventlotto/ui/entrant/Ent_EntryFragment.java
                // If doesn't exist, show create profile button
=======
>>>>>>> Stashed changes:app/src/main/java/com/example/eventlotto/ui/EntryFragment.java
                createProfileBtn.setVisibility(View.VISIBLE);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {

            createProfileBtn.setVisibility(View.VISIBLE);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        });

<<<<<<< Updated upstream:app/src/main/java/com/example/eventlotto/ui/entrant/Ent_EntryFragment.java
        // Set click listener for creating a new profile
=======

>>>>>>> Stashed changes:app/src/main/java/com/example/eventlotto/ui/EntryFragment.java
        createProfileBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new Ent_CreateProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}

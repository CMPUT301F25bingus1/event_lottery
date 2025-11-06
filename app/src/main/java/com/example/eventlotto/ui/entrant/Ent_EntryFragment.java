package com.example.eventlotto.ui.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        firestoreService = new FirestoreService();
        Button createProfileBtn = view.findViewById(R.id.btn_create_profile);

        // Gets the current device ID
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(),Settings.Secure.ANDROID_ID);

        // Checks if the entrant already exists in Firestore
        firestoreService.getUser(deviceId).addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // If yes, navigate to Profile screen
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .commit();
            } else {
                // If doesn't exist, show create profile button
                createProfileBtn.setVisibility(View.VISIBLE);
            }
        });

        // Set click listener for creating a new profile
        createProfileBtn.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new Ent_CreateProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}

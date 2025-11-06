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

public class Ent_EntryFragment extends Fragment {

    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        firestoreService = new FirestoreService();
        Button createProfileBtn = view.findViewById(R.id.btn_create_profile);

        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Check if the entrant already exists in FireStone
        firestoreService.getUser(deviceId).addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // If entrant exists : go to Profile screen
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .commit();
            } else {
                // If the Entrant doesn't exist, go create profile
                createProfileBtn.setVisibility(View.VISIBLE);
            }
        });

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

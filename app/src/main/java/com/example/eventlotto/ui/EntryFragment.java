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
import com.example.eventlotto.MainActivity;

public class EntryFragment extends Fragment {

    private FirestoreService firestoreService;
    private Button createProfileBtn;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        firestoreService = new FirestoreService();
        createProfileBtn = view.findViewById(R.id.btn_go_to_create_profile);


        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );


        firestoreService.getUser(deviceId).addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new Ent_HomeFragment())
                        .commit();

                //initialize and show bottom navigation for the new user
                ((MainActivity) requireActivity()).initBottomNavForRole("entrant");

            } else {
                createProfileBtn.setVisibility(View.VISIBLE);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {

            createProfileBtn.setVisibility(View.VISIBLE);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        });


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

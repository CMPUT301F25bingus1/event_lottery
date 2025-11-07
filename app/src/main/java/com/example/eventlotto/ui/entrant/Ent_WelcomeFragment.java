package com.example.eventlotto.ui.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotto.R;
import com.example.eventlotto.ui.CreateProfileFragment;

/**
 * Fragment representing the welcome screen for entrant users.
 * <p>
 * This fragment serves as the entry point for new entrants who have not yet created
 * their user profile. From here, users can navigate to the profile creation screen.
 * </p>
 */
public class Ent_WelcomeFragment extends Fragment {

    /**
     * Called to create and return the view hierarchy associated with this fragment.
     * <p>
     * This method opens the welcome layout and initializes the "Create Profile" button.
     * When clicked, it navigates the user to {@link CreateProfileFragment} where
     * they can set up their entrant profile.
     * </p>
     *
     * @param inflater  The {@link LayoutInflater} used to inflate the fragment's layout.
     * @param container The parent view that this fragment's UI should attach to.
     * @param savedInstanceState If non-null, the fragment is being re-created from a previous state.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        // Initialize "Create Profile" button and set navigation action
        Button btnCreateProfile = view.findViewById(R.id.btn_go_to_create_profile);
        btnCreateProfile.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}

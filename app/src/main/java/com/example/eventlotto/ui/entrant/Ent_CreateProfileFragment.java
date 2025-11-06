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
import com.example.eventlotto.R;
import com.example.eventlotto.model.User;
<<<<<<< Updated upstream:app/src/main/java/com/example/eventlotto/ui/entrant/Ent_CreateProfileFragment.java
import com.example.eventlotto.ui.LoginFragment;
=======
import com.google.android.material.bottomnavigation.BottomNavigationView;
>>>>>>> Stashed changes:app/src/main/java/com/example/eventlotto/ui/CreateProfileFragment.java

/**
 * Fragment for creating a new user profile.
 * Collects name, email, and phone number and saves it to Firestore.
 */
public class Ent_CreateProfileFragment extends Fragment {

    private EditText nameInput;

    private EditText emailInput;

    private EditText phoneInput;

<<<<<<< Updated upstream:app/src/main/java/com/example/eventlotto/ui/entrant/Ent_CreateProfileFragment.java
=======
    private EditText inputName, inputEmail, inputPhone;
    private Button btnCreateProfile;
>>>>>>> Stashed changes:app/src/main/java/com/example/eventlotto/ui/CreateProfileFragment.java
    private FirestoreService firestoreService;
    private String deviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_profile, container, false);

        firestoreService = new FirestoreService();

<<<<<<< Updated upstream:app/src/main/java/com/example/eventlotto/ui/entrant/Ent_CreateProfileFragment.java
        // Bind views
        nameInput = view.findViewById(R.id.input_name);
        emailInput = view.findViewById(R.id.input_email);
        phoneInput = view.findViewById(R.id.input_phone);
        Button createBtn = view.findViewById(R.id.btn_create_profile);

        // Set click listener to create profile
        createBtn.setOnClickListener(v -> {
            String fullName = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            // Validate required fields
            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "Please enter name and email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get device ID to associate user profile with this device
            String deviceId = Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            // Create user object
            User user = new User(fullName, email, phone, deviceId);

            // Save user to Firestore
            firestoreService.saveUser(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile created successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to LoginFragment after successful creation
                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new LoginFragment())
                                .addToBackStack(null)
                                .commit();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error creating profile.", Toast.LENGTH_SHORT).show());
        });
=======
        inputName = view.findViewById(R.id.input_name);
        inputEmail = view.findViewById(R.id.input_email);
        inputPhone = view.findViewById(R.id.input_phone);
        btnCreateProfile = view.findViewById(R.id.btn_create_profile);

        // Get device ID
        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        btnCreateProfile.setOnClickListener(v -> createUserProfile());
>>>>>>> Stashed changes:app/src/main/java/com/example/eventlotto/ui/CreateProfileFragment.java

        return view;
    }

    private void createUserProfile() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        User user = new User(deviceId, name, email, phone);

        firestoreService.saveUserProfile(user, success -> {
            if (success) {
                Toast.makeText(requireContext(), "Profile created successfully!", Toast.LENGTH_SHORT).show();


                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();


                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(requireContext(), "Failed to create profile. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

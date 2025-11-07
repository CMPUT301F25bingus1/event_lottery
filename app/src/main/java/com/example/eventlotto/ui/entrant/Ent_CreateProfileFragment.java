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

public class Ent_CreateProfileFragment extends Fragment {

    private EditText inputName, inputEmail, inputPhone;
    private Button btnCreateProfile;
    private FirestoreService firestoreService;
    private String deviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_profile, container, false);

        firestoreService = new FirestoreService();

        inputName = view.findViewById(R.id.input_name);
        inputEmail = view.findViewById(R.id.input_email);
        inputPhone = view.findViewById(R.id.input_phone);
        btnCreateProfile = view.findViewById(R.id.btn_create_profile);

        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );


        ((MainActivity) requireActivity()).hideBottomNavigation();

        btnCreateProfile.setOnClickListener(v -> createUserProfile());

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
                        .replace(R.id.fragment_container, new Ent_HomeFragment())
                        .commit();
                ((MainActivity) requireActivity()).initBottomNavForRole("entrant");

                //initialize and show bottom navigation for the new user
                ((MainActivity) requireActivity()).initBottomNavForRole("entrant");
            } else {
                Toast.makeText(requireContext(), "Failed to create profile. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

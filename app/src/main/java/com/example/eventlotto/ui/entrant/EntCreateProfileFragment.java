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

public class EntCreateProfileFragment extends Fragment {

    private EditText nameInput, emailInput, phoneInput;
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_profile, container, false);

        firestoreService = new FirestoreService();

        nameInput = view.findViewById(R.id.input_name);
        emailInput = view.findViewById(R.id.input_email);
        phoneInput = view.findViewById(R.id.input_phone);
        Button createBtn = view.findViewById(R.id.btn_create_profile);

        createBtn.setOnClickListener(v -> {
            String fullName = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "Please enter name and email", Toast.LENGTH_SHORT).show();
                return;
            }

            String deviceId = Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            User user = new User(fullName, email, phone, deviceId);

            firestoreService.saveUser(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile created successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate manually to ProfileFragment
                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new EntLoginFragment())
                                .addToBackStack(null)
                                .commit();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error creating profile.", Toast.LENGTH_SHORT).show());
        });

        return view;
    }
}

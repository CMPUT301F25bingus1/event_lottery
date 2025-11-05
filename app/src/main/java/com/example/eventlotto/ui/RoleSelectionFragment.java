package com.example.eventlotto.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotto.R;

public class RoleSelectionFragment extends Fragment {

    public interface RoleSelectionListener {
        void onRoleSelected(String roleKey); // "entrant", "organizer", "admin"
    }

    private RoleSelectionListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RoleSelectionListener) {
            listener = (RoleSelectionListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_role_selection, container, false);

        Button entrantBtn = view.findViewById(R.id.btn_role_entrant);
        Button organizerBtn = view.findViewById(R.id.btn_role_organizer);
        Button adminBtn = view.findViewById(R.id.btn_role_admin);

        entrantBtn.setOnClickListener(v -> notifyRole("entrant"));
        organizerBtn.setOnClickListener(v -> notifyRole("organizer"));
        adminBtn.setOnClickListener(v -> notifyRole("admin"));

        return view;
    }

    private void notifyRole(String role) {
        if (listener != null) {
            listener.onRoleSelected(role);
        }
    }
}


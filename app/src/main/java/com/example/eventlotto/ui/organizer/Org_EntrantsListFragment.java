package com.example.eventlotto.ui.organizer;
import com.example.eventlotto.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Org_EntrantsListFragment extends DialogFragment {

    private static final String ARG_EVENT_ID = "eventId";
    private String eventId;

    public static Org_EntrantsListFragment newInstance(String eventId) {
        Org_EntrantsListFragment fragment = new Org_EntrantsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView entrantsRecycler;
    private EntrantsAdapter adapter;
    private List<DocumentSnapshot> entrantsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrants_list, container, false);

        eventId = getArguments() != null ? getArguments().getString(ARG_EVENT_ID) : null;

        entrantsRecycler = view.findViewById(R.id.entrantsRecycler);
        entrantsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EntrantsAdapter(entrantsList);
        entrantsRecycler.setAdapter(adapter);

        if (eventId != null) loadEntrants(eventId);

        View returnButton = view.findViewById(R.id.returnButton);
        returnButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }


    private void loadEntrants(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId)
                .collection("status")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    entrantsList.clear();
                    entrantsList.addAll(querySnapshot.getDocuments());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}

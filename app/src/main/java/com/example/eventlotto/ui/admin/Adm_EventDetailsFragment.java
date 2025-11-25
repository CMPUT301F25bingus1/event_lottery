package com.example.eventlotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class Adm_EventDetailsFragment extends DialogFragment {

    private String eventId;
    private FirestoreService firestoreService;

    private ImageView eventImage;
    private TextView eventTitle, eventDescription, statusText;
    private Button deleteButton;

    public static Adm_EventDetailsFragment newInstance(String eventId) {
        Adm_EventDetailsFragment fragment = new Adm_EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_details_admin, container, false);

        eventImage = view.findViewById(R.id.eventImage);
        eventTitle = view.findViewById(R.id.eventTitle);
        eventDescription = view.findViewById(R.id.eventDescription);
        statusText = view.findViewById(R.id.statusText);
        deleteButton = view.findViewById(R.id.btnDeleteEvent);

        eventId = getArguments() != null ? getArguments().getString("eventId") : null;
        firestoreService = new FirestoreService();

        if (eventId != null) {
            fetchEventData(eventId);
        }

        deleteButton.setOnClickListener(v -> {
            if (eventId != null) deleteEvent(eventId);
        });

        return view;
    }

    private void fetchEventData(String eventId) {
        firestoreService.events()
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        populateEvent(doc);
                    } else {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void populateEvent(DocumentSnapshot doc) {
        eventTitle.setText(doc.getString("eventTitle") != null ? doc.getString("eventTitle") : "No Title");
        eventDescription.setText(doc.getString("description") != null ? doc.getString("description") : "No Description");
        statusText.setText("Admin view: " + (doc.getString("status") != null ? doc.getString("status") : "N/A"));

        String imageUrl = doc.getString("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void deleteEvent(String eventId) {
        firestoreService.events()
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                    dismiss(); // close fragment after deletion
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}

package com.example.eventlotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.FirestoreService;
import com.example.eventlotto.R;
import com.example.eventlotto.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdmImagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventImageAdapter adapter;
    private List<EventImageItem> imageList;
    private FirestoreService firestoreService;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_images, container, false);

        firestoreService = new FirestoreService();

        recyclerView = view.findViewById(R.id.recycler_view_images);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        imageList = new ArrayList<>();

        adapter = new EventImageAdapter(imageList, this::onImageClick);
        recyclerView.setAdapter(adapter);

        fetchEventImages();

        return view;
    }

    private void fetchEventImages() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        firestoreService.events().get()
                .addOnSuccessListener(query -> {
                    imageList.clear();

                    for (DocumentSnapshot doc : query) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEid(doc.getId());

                            if (event.getEventURL() != null && !event.getEventURL().trim().isEmpty()) {
                                EventImageItem item = new EventImageItem(
                                        event.getEid(),
                                        event.getEventTitle(),
                                        event.getEventURL()
                                );
                                imageList.add(item);
                            }
                        }
                    }

                    progressBar.setVisibility(View.GONE);

                    if (imageList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Failed to load images", Toast.LENGTH_SHORT).show();
                });
    }

    private void onImageClick(EventImageItem item) {
        showDeleteConfirmationDialog(item);
    }

    private void showDeleteConfirmationDialog(EventImageItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete This Image?")
                .setMessage("Are you sure you want to delete this image?")
                .setCancelable(true)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes", (dialog, which) -> deleteImage(item))
                .show();
    }

    private void deleteImage(EventImageItem item) {
        firestoreService.events()
                .document(item.getEventId())
                .update("eventURL", null)
                .addOnSuccessListener(aVoid -> {
                    deleteFromStorageIfNeeded(item.getImageUrl());

                    imageList.remove(item);
                    adapter.notifyDataSetChanged();

                    if (imageList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }

                    Toast.makeText(getContext(), "Image deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to delete image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void deleteFromStorageIfNeeded(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("firebasestorage.googleapis.com")) {
            return;
        }

        try {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            storageRef.delete()
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(e -> {});
        } catch (Exception e) {}
    }

    public static class EventImageItem {
        private final String eventId;
        private final String eventTitle;
        private final String imageUrl;

        public EventImageItem(String eventId, String eventTitle, String imageUrl) {
            this.eventId = eventId;
            this.eventTitle = eventTitle;
            this.imageUrl = imageUrl;
        }

        public String getEventId() { return eventId; }
        public String getEventTitle() { return eventTitle; }
        public String getImageUrl() { return imageUrl; }
    }
}

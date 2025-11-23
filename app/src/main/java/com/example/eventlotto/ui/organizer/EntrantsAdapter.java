package com.example.eventlotto.ui.organizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotto.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EntrantsAdapter extends RecyclerView.Adapter<EntrantsAdapter.ViewHolder> {

    private final List<DocumentSnapshot> entrantsList;

    public EntrantsAdapter(List<DocumentSnapshot> entrantsList) {
        this.entrantsList = entrantsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DocumentSnapshot doc = entrantsList.get(position);
        String userId = doc.getId();
        String status = doc.getString("status");

        holder.statusText.setText(status != null ? status : "N/A");

        // --- Set color by status ---
        if (status != null) {
            switch (status) {
                case "waiting":
                    holder.statusText.setTextColor(0xFFFFA000); // amber
                    break;
                case "selected":
                    holder.statusText.setTextColor(0xFF4CAF50); // green
                    break;
                case "not_chosen":
                    holder.statusText.setTextColor(0xFFF44336); // red
                    break;
                default:
                    holder.statusText.setTextColor(0xFF000000); // black
            }
        }

        // --- Show Cancel button only for selected users ---
        if ("selected".equals(status)) {
            holder.cancelButton.setVisibility(View.VISIBLE);
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }

        // --- Load user full name ---
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userSnap -> {
                    if (userSnap.exists()) {
                        String name = userSnap.getString("fullName");
                        holder.nameText.setText(name != null ? name : "Unknown");
                    } else {
                        holder.nameText.setText("Unknown");
                    }
                })
                .addOnFailureListener(e -> holder.nameText.setText("Error"));

        // --- Handle Cancellation Button ---
        holder.cancelButton.setOnClickListener(v ->
                cancelEntrant(doc.getReference(), holder)
        );
    }

    private void cancelEntrant(DocumentReference entrantRef, ViewHolder holder) {
        entrantRef.update("status", "not_chosen")
                .addOnSuccessListener(aVoid -> {
                    holder.statusText.setText("not_chosen");
                    holder.statusText.setTextColor(0xFFF44336);
                    holder.cancelButton.setVisibility(View.GONE);
                })
                .addOnFailureListener(e ->
                        holder.statusText.setText("Error")
                );
    }

    @Override
    public int getItemCount() {
        return entrantsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameText, statusText;
        Button cancelButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            statusText = itemView.findViewById(R.id.statusText);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}
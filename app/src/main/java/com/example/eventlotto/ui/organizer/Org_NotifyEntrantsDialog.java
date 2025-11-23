package com.example.eventlotto.ui.organizer;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.eventlotto.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Org_NotifyEntrantsDialog extends DialogFragment {

    private static final String ARG_EVENT_ID = "eventId";
    private String eventId;

    public static Org_NotifyEntrantsDialog newInstance(String eventId ) {
        Org_NotifyEntrantsDialog d = new Org_NotifyEntrantsDialog();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        d.setArguments(b);
        return d;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        eventId = getArguments().getString(ARG_EVENT_ID);

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_notify_entrants, null);

        RadioButton rbWaiting = view.findViewById(R.id.rbWaiting);
        RadioButton rbSelected = view.findViewById(R.id.rbSelected);
        RadioButton rbCancelled = view.findViewById(R.id.rbCancelled);
        EditText messageInput = view.findViewById(R.id.messageInput);
        Button sendButton = view.findViewById(R.id.buttonSend);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        sendButton.setOnClickListener(v -> {

            String message = messageInput.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(getContext(), "Enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            String targetStatus;
            if (rbWaiting.isChecked()) targetStatus = "waiting";
            else if (rbSelected.isChecked()) targetStatus = "selected";
            else targetStatus = "not_chosen"; // cancelled

            sendNotifications(targetStatus, message);
            dialog.dismiss();
        });

        return dialog;
    }

    private void sendNotifications(String status, String message) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .document(eventId)
                .collection("entrants")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(snaps -> {

                    for (DocumentSnapshot entrant : snaps) {

                        String userId = entrant.getId();

                        Map<String, Object> n = new HashMap<>();
                        n.put("nid", userId + "_" + System.currentTimeMillis());
                        n.put("uid", userId);
                        n.put("eid", eventId);
                        n.put("message", message);
                        n.put("timestamp", System.currentTimeMillis());

                        db.collection("notifications")
                                .document((String) n.get("nid"))
                                .set(n);
                    }

                    if (getActivity() != null && !getActivity().isFinishing()) {
                        Toast.makeText(getActivity(), "Notifications sent!", Toast.LENGTH_LONG).show();
                    }

                })
                .addOnFailureListener(e -> {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        Toast.makeText(getActivity(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

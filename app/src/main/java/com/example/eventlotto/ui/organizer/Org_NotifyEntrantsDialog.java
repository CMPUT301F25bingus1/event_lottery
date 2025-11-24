package com.example.eventlotto.ui.organizer;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

    public static Org_NotifyEntrantsDialog newInstance(String eventId) {
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
            else if (rbCancelled.isChecked()) targetStatus = "cancelled";
            else targetStatus = "not_chosen";

            sendNotifications(targetStatus, message);
            dialog.dismiss();
        });

        return dialog;
    }

    private void sendNotifications(String targetStatus, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the status subcollection
        db.collection("events")
                .document(eventId)
                .collection("status")
                .get()
                .addOnSuccessListener(statusSnapshots -> {
                    Log.i("OrgNotify", "DEBUG — statusSnapshots size: " + statusSnapshots.size());

                    int notificationsSent = 0;

                    for (DocumentSnapshot uidDoc : statusSnapshots) {
                        String uid = uidDoc.getId();
                        String status = uidDoc.getString("status");

                        Log.i("OrgNotify", "DEBUG — UID doc = " + uid + " → " + uidDoc.getData());

                        // Only notify if status matches the selected targetStatus
                        if (status != null && status.equalsIgnoreCase(targetStatus)) {
                            String nid = uid + "_" + eventId;

                            Map<String, Object> n = new HashMap<>();
                            n.put("uid", uid);
                            n.put("eid", eventId);
                            n.put("nid", nid);
                            n.put("message", message);
                            n.put("lastSentAt", null);
                            n.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                            db.collection("notifications")
                                    .document(nid)
                                    .set(n)
                                    .addOnSuccessListener(v -> {
                                        Log.i("OrgNotify", "DEBUG — notification written for UID=" + uid + ", nid=" + nid);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("OrgNotify", "ERROR — failed to write notification for UID=" + uid + ", nid=" + nid, e);
                                    });

                            notificationsSent++;
                        }
                    }

                    // Show a toast if fragment is still attached
                    if (isAdded() && getActivity() != null) {
                        String msg = notificationsSent > 0
                                ? notificationsSent + " notifications sent!"
                                : "No entrants matched the selected status.";
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("OrgNotify", "ERROR — failed to fetch status docs", e);
                    if (isAdded() && getActivity() != null) {
                        Toast.makeText(getActivity(), "Failed to fetch entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
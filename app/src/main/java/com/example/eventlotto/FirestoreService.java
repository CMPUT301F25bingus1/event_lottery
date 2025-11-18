package com.example.eventlotto;

import com.example.eventlotto.model.Notification;
import com.example.eventlotto.model.EventStatus;
import com.example.eventlotto.model.User;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import android.util.Log;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Consumer;

public class FirestoreService {

    private static final String TAG = "FirestoreService";

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_EVENTS = "events";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_EVENT_STATUS = "event_status";

    private final FirebaseFirestore db;
    private final CollectionReference usersCollection;

    public FirestoreService() {
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection(COLLECTION_USERS);
    }

    // ----------------------------------------------------
    // Collection helpers
    // ----------------------------------------------------
    public CollectionReference users() { return db.collection(COLLECTION_USERS); }
    public CollectionReference events() { return db.collection(COLLECTION_EVENTS); }
    public CollectionReference notifications() { return db.collection(COLLECTION_NOTIFICATIONS); }
    public CollectionReference eventStatus() { return db.collection(COLLECTION_EVENT_STATUS); }

    // ----------------------------------------------------
    // USER PROFILE MANAGEMENT
    // ----------------------------------------------------

    public void saveUserProfile(User user, FirestoreCallback callback) {
        if (user == null || user.getDeviceId() == null) {
            callback.onCallback(false);
            return;
        }

        usersCollection.document(user.getDeviceId())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(r -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    public Task<DocumentSnapshot> getUser(String deviceId) {
        return usersCollection.document(deviceId).get();
    }

    public void updateUserProfile(User user, FirestoreCallback callback) {
        if (user == null || user.getDeviceId() == null) {
            callback.onCallback(false);
            return;
        }

        user.touch();
        usersCollection.document(user.getDeviceId())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(r -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    public void deleteUserProfile(String deviceId, FirestoreCallback callback) {
        if (deviceId == null || deviceId.isEmpty()) {
            callback.onCallback(false);
            return;
        }

        usersCollection.document(deviceId)
                .delete()
                .addOnSuccessListener(r -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    public void userExists(String uid, Consumer<Boolean> callback) {
        users().document(uid).get()
                .addOnSuccessListener(doc -> callback.accept(doc.exists()))
                .addOnFailureListener(e -> callback.accept(false));
    }

    public Task<Void> saveUser(User user) {
        if (user == null || user.getUid() == null)
            throw new IllegalArgumentException("user.uid is required");

        if (user.getCreatedAt() == null)
            user.setCreatedAt(Timestamp.now());
        user.setUpdatedAt(Timestamp.now());

        return users().document(user.getUid()).set(user, SetOptions.merge());
    }

    public void deleteUser(String uid, Consumer<Boolean> callback) {
        users().document(uid).delete()
                .addOnSuccessListener(r -> callback.accept(true))
                .addOnFailureListener(e -> callback.accept(false));
    }

    // ----------------------------------------------------
    // NOTIFICATIONS
    // ----------------------------------------------------

    public Task<Void> saveNotification(Notification notification) {
        if (notification == null || notification.getNid() == null)
            throw new IllegalArgumentException("notification.nid required");

        if (notification.getCreatedAt() == null)
            notification.setCreatedAt(Timestamp.now());

        return notifications().document(notification.getNid())
                .set(notification, SetOptions.merge());
    }

    // ----------------------------------------------------
    // EVENT STATUS
    // ----------------------------------------------------

    public Task<QuerySnapshot> getEventStatusesForUser(String uid) {
        return eventStatus().whereEqualTo("uid", uid).get();
    }

    public Task<Void> saveEventStatus(EventStatus status) {
        if (status == null)
            throw new IllegalArgumentException("status required");

        if (status.getSid() == null)
            status.setSid(status.getUid() + "_" + status.getEid());

        return eventStatus().document(status.getSid())
                .set(status, SetOptions.merge());
    }

    // ----------------------------------------------------
    // WAITLIST JOIN
    // ----------------------------------------------------

    public Task<Void> joinWaitlist(String eventId, String uid) {

        WriteBatch batch = db.batch();

        // event/{id}/status/{uid}
        DocumentReference statusRef = events().document(eventId)
                .collection("status")
                .document(uid);

        Map<String, Object> statusData = new HashMap<>();
        statusData.put("uid", uid);
        statusData.put("status", "waiting");
        statusData.put("timestamp", Timestamp.now());
        batch.set(statusRef, statusData, SetOptions.merge());

        // notifications/{uid_eventId}
        String nid = uid + "_" + eventId;
        DocumentReference notifRef = notifications().document(nid);
        Map<String, Object> notifData = new HashMap<>();
        notifData.put("nid", nid);
        notifData.put("uid", uid);
        notifData.put("eid", eventId);
        notifData.put("createdAt", Timestamp.now());
        batch.set(notifRef, notifData, SetOptions.merge());

        // event_status/{uid_eventId}
        String sid = uid + "_" + eventId;
        DocumentReference topRef = eventStatus().document(sid);
        Map<String, Object> topStatus = new HashMap<>();
        topStatus.put("sid", sid);
        topStatus.put("uid", uid);
        topStatus.put("eid", eventId);
        topStatus.put("status", "waiting");
        topStatus.put("updatedAt", Timestamp.now());
        batch.set(topRef, topStatus, SetOptions.merge());

        return batch.commit();
    }

    // ----------------------------------------------------
    // WITHDRAW
    // ----------------------------------------------------

    public void withdrawFromWaitlist(String eventId, String uid, FirestoreCallback callback) {
        if (eventId == null || uid == null) {
            callback.onCallback(false);
            return;
        }

        DocumentReference statusRef = events().document(eventId)
                .collection("status")
                .document(uid);

        Map<String, Object> data = new HashMap<>();
        data.put("status", "cancelled");
        data.put("timestamp", Timestamp.now());

        statusRef.set(data, SetOptions.merge())
                .addOnSuccessListener(r -> callback.onCallback(true))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "withdrawFromWaitlist failed", e);
                    callback.onCallback(false);
                });
    }

    // ----------------------------------------------------
    // NEW LOTTERY SYSTEM (LOCKED + BATCHED + SECURE)
    // ----------------------------------------------------

    public void rollLottery(String eventId, int maxWinners, FirestoreCallback callback) {

        if (eventId == null || eventId.isEmpty()) {
            callback.onCallback(false);
            return;
        }

        if (maxWinners <= 0) {
            Log.e(TAG, "maxWinners must be > 0");
            callback.onCallback(false);
            return;
        }

        DocumentReference eventRef = events().document(eventId);
        DocumentReference lockRef = eventRef.collection("meta").document("lottery_lock");

        // Step 1 — lock the lottery so it can't run twice
        db.runTransaction(transaction -> {
            DocumentSnapshot lockSnap = transaction.get(lockRef);

            Boolean locked = lockSnap.getBoolean("locked");
            if (locked != null && locked) {
                throw new FirebaseFirestoreException(
                        "Lottery already running",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            // Lock it
            transaction.set(lockRef, Collections.singletonMap("locked", true), SetOptions.merge());
            return null;
        }).addOnSuccessListener(r -> {

            // Step 2 — fetch waiting entrants
            eventRef.collection("status")
                    .whereEqualTo("status", "waiting")
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        ArrayList<String> entrants = new ArrayList<>();
                        for (DocumentSnapshot ds : snapshot)
                            entrants.add(ds.getId());

                        // Shuffle using SecureRandom
                        SecureRandom random = new SecureRandom();
                        Collections.shuffle(entrants, random);

                        ArrayList<String> winners = new ArrayList<>();
                        for (int i = 0; i < entrants.size() && i < maxWinners; i++)
                            winners.add(entrants.get(i));

                        // Prepare batched writes
                        ArrayList<WriteBatch> batches = new ArrayList<>();
                        batches.add(db.batch());
                        int batchIndex = 0;
                        int ops = 0;

                        Runnable nextBatch = () -> { batches.add(db.batch()); };

                        // Winners
                        for (String uid : winners) {
                            if (ops > 480) { nextBatch.run(); batchIndex++; ops = 0; }
                            DocumentReference ref = eventRef.collection("status").document(uid);
                            Map<String, Object> data = new HashMap<>();
                            data.put("status", "selected");
                            data.put("timestamp", Timestamp.now());
                            batches.get(batchIndex).set(ref, data, SetOptions.merge());
                            ops++;
                        }

                        // Non-winners
                        for (String uid : entrants) {
                            if (winners.contains(uid)) continue;
                            if (ops > 480) { nextBatch.run(); batchIndex++; ops = 0; }

                            DocumentReference ref = eventRef.collection("status").document(uid);
                            Map<String, Object> data = new HashMap<>();
                            data.put("status", "not_chosen");
                            data.put("timestamp", Timestamp.now());
                            batches.get(batchIndex).set(ref, data, SetOptions.merge());
                            ops++;
                        }

                        // Lottery meta doc
                        if (ops > 480) { nextBatch.run(); batchIndex++; ops = 0; }
                        DocumentReference metaRef = eventRef.collection("meta").document("lottery");
                        Map<String, Object> meta = new HashMap<>();
                        meta.put("winners", winners);
                        meta.put("rolledAt", Timestamp.now());
                        batches.get(batchIndex).set(metaRef, meta, SetOptions.merge());
                        ops++;

                        // Update event status
                        if (ops > 480) { nextBatch.run(); batchIndex++; ops = 0; }
                        batches.get(batchIndex).update(eventRef, "status", "completed");
                        ops++;

                        // Optional: reflect to event_status + notifications
                        for (String uid : entrants) {
                            if (ops > 470) { nextBatch.run(); batchIndex++; ops = 0; }

                            String sid = uid + "_" + eventId;
                            DocumentReference top = eventStatus().document(sid);
                            DocumentReference notif = notifications().document(sid);

                            String newStatus = winners.contains(uid) ? "selected" : "not_chosen";

                            Map<String, Object> upd = new HashMap<>();
                            upd.put("status", newStatus);
                            upd.put("updatedAt", Timestamp.now());

                            batches.get(batchIndex).set(top, upd, SetOptions.merge());
                            batches.get(batchIndex).set(notif, upd, SetOptions.merge());
                            ops += 2;
                        }

                        // Step 3 — commit batches sequentially
                        commitBatchesSequentially(batches, callback);

                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch waiting entrants", e);
                        callback.onCallback(false);
                    });

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lottery lock failed", e);
            callback.onCallback(false);
        });
    }

    // Commit batches in sequence
    private void commitBatchesSequentially(ArrayList<WriteBatch> batches, FirestoreCallback callback) {
        commitBatchRecursive(batches, 0, callback);
    }

    private void commitBatchRecursive(ArrayList<WriteBatch> batches, int index, FirestoreCallback callback) {
        if (index >= batches.size()) {
            callback.onCallback(true);
            return;
        }

        batches.get(index).commit()
                .addOnSuccessListener(r -> commitBatchRecursive(batches, index + 1, callback))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Batch commit failed at index " + index, e);
                    callback.onCallback(false);
                });
    }

    // ----------------------------------------------------
    // Callback interface
    // ----------------------------------------------------
    public interface FirestoreCallback {
        void onCallback(boolean success);
    }
}

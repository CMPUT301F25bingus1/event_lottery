package com.example.eventlotto;

import com.example.eventlotto.model.Notification;
import com.example.eventlotto.model.EventStatus;
import com.example.eventlotto.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Combined FirestoreService
 * Includes:
 *  - User profile CRUD (deviceId-based)
 *  - Event & Notification methods (team version)
 *  - Utility helpers
 *  - Lottery / waitlist management (new)
 */
public class FirestoreService {

    private static final String TAG = "FirestoreService";

    // ---------------------------
    // Collection names
    // ---------------------------
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_EVENTS = "events";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_EVENT_STATUS = "event_status";

    private final FirebaseFirestore db;
    private final CollectionReference usersCollection;

    public FirestoreService() {
        this.db = FirebaseFirestore.getInstance();
        this.usersCollection = db.collection(COLLECTION_USERS);
    }

    // ---------------------------
    // General Collection Accessors
    // ---------------------------
    public CollectionReference users() {
        return db.collection(COLLECTION_USERS);
    }

    public CollectionReference events() {
        return db.collection(COLLECTION_EVENTS);
    }

    public CollectionReference notifications() {
        return db.collection(COLLECTION_NOTIFICATIONS);
    }

    public CollectionReference eventStatus() {
        return db.collection(COLLECTION_EVENT_STATUS);
    }

    // ---------------------------
    // USER PROFILE MANAGEMENT
    // ---------------------------

    /** Save new user profile (deviceId = UID) */
    public void saveUserProfile(User user, FirestoreCallback callback) {
        if (user == null || user.getDeviceId() == null) {
            callback.onCallback(false);
            return;
        }

        usersCollection.document(user.getDeviceId())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    /** Get user profile by deviceId */
    public Task<DocumentSnapshot> getUser(String deviceId) {
        return usersCollection.document(deviceId).get();
    }

    /** Update user profile (and timestamp) */
    public void updateUserProfile(User user, FirestoreCallback callback) {
        if (user == null || user.getDeviceId() == null) {
            callback.onCallback(false);
            return;
        }

        user.touch(); // update timestamp
        usersCollection.document(user.getDeviceId())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    /** Delete user by deviceId */
    public void deleteUserProfile(String deviceId, FirestoreCallback callback) {
        if (deviceId == null || deviceId.isEmpty()) {
            callback.onCallback(false);
            return;
        }

        usersCollection.document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    /** Check if user exists */
    public void userExists(String uid, Consumer<Boolean> callback) {
        users().document(uid).get()
                .addOnSuccessListener(snapshot -> callback.accept(snapshot.exists()))
                .addOnFailureListener(e -> callback.accept(false));
    }

    /** Save user (team version, ensures timestamps) */
    public Task<Void> saveUser(User user) {
        if (user == null || user.getUid() == null) {
            throw new IllegalArgumentException("user.uid is required");
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Timestamp.now());
        }
        user.setUpdatedAt(Timestamp.now());
        return users().document(user.getUid()).set(user, SetOptions.merge());
    }

    /** Delete user with Consumer callback (team style) */
    public void deleteUser(String uid, Consumer<Boolean> callback) {
        users().document(uid).delete()
                .addOnSuccessListener(aVoid -> callback.accept(true))
                .addOnFailureListener(e -> callback.accept(false));
    }

    // ---------------------------
    // NOTIFICATION MANAGEMENT
    // ---------------------------

    public Task<Void> saveNotification(Notification notification) {
        if (notification == null || notification.getNid() == null) {
            throw new IllegalArgumentException("notification.nid is required");
        }
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(Timestamp.now());
        }
        return notifications().document(notification.getNid()).set(notification, SetOptions.merge());
    }

    // ---------------------------
    // EVENT STATUS MANAGEMENT
    // ---------------------------

    public Task<QuerySnapshot> getEventStatusesForUser(String uid) {
        return eventStatus().whereEqualTo("uid", uid).get();
    }

    public Task<Void> saveEventStatus(EventStatus status) {
        if (status == null) throw new IllegalArgumentException("status required");
        if (status.getSid() == null) {
            status.setSid(status.getUid() + "_" + status.getEid());
        }
        return eventStatus().document(status.getSid()).set(status, SetOptions.merge());
    }

    // ---------------------------
    // WAITLIST / BATCH ACTIONS (existing)
    // ---------------------------

    /**
     * Join waitlist (existing implementation preserved).
     *
     * Creates:
     *  - events/{eventId}/status/{uid} with status "waiting"
     *  - notifications/{uid_eventId} subscription doc
     *  - event_status/{uid_eventId} top-level doc
     */
    public Task<Void> joinWaitlist(String eventId, String uid) {
        WriteBatch batch = db.batch();

        // Status document under event
        DocumentReference statusRef = events().document(eventId)
                .collection("status")
                .document(uid);
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("uid", uid);
        statusData.put("status", "waiting");
        statusData.put("timestamp", Timestamp.now());
        batch.set(statusRef, statusData, SetOptions.merge());

        // Notification subscription doc
        String nid = uid + "_" + eventId;
        DocumentReference notifRef = notifications().document(nid);
        Map<String, Object> notifData = new HashMap<>();
        notifData.put("nid", nid);
        notifData.put("uid", uid);
        notifData.put("eid", eventId);
        notifData.put("createdAt", Timestamp.now());
        batch.set(notifRef, notifData, SetOptions.merge());

        // Top-level event_status
        String sid = uid + "_" + eventId;
        DocumentReference statusTopRef = eventStatus().document(sid);
        Map<String, Object> topStatus = new HashMap<>();
        topStatus.put("sid", sid);
        topStatus.put("uid", uid);
        topStatus.put("eid", eventId);
        topStatus.put("status", "waiting");
        topStatus.put("updatedAt", Timestamp.now());
        batch.set(statusTopRef, topStatus, SetOptions.merge());

        return batch.commit();
    }

    // ---------------------------
    // NEW: Withdrawal & Lottery System
    // ---------------------------

    /**
     * Withdraw an entrant from the event waitlist.
     * Sets events/{eventId}/status/{uid}.status = "cancelled" and updates timestamps.
     */
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
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "withdrawFromWaitlist failed", e);
                    callback.onCallback(false);
                });
    }

    /**
     * Roll the lottery for a given event:
     *  - Select up to maxWinners from entries where status == "waiting"
     *  - Set selected winners -> "selected"
     *  - Set all other entrants that were "waiting" -> "not_chosen"
     *  - Save meta doc at events/{eid}/meta/lottery { winners: [...], rolledAt: Timestamp }
     *  - Update events/{eid}.status = "completed"
     *
     * Calls callback.onCallback(true) on success, false on any failure.
     */
    public void rollLottery(String eventId, int maxWinners, FirestoreCallback callback) {
        if (eventId == null) {
            callback.onCallback(false);
            return;
        }

        db.collection(COLLECTION_EVENTS).document(eventId)
                .collection("status")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<String> entrants = new ArrayList<>();
                    // collect document ids (uid)
                    for (DocumentSnapshot ds : querySnapshot.getDocuments()) {
                        if (ds.exists()) entrants.add(ds.getId());
                    }

                    // shuffle to randomize
                    Collections.shuffle(entrants);

                    // pick winners up to maxWinners
                    ArrayList<String> winners = new ArrayList<>();
                    for (int i = 0; i < entrants.size() && i < maxWinners; i++) {
                        winners.add(entrants.get(i));
                    }

                    WriteBatch batch = db.batch();

                    // mark winners selected
                    for (String w : winners) {
                        DocumentReference winnerRef = events().document(eventId)
                                .collection("status").document(w);
                        Map<String, Object> wdata = new HashMap<>();
                        wdata.put("status", "selected");
                        wdata.put("timestamp", Timestamp.now());
                        batch.set(winnerRef, wdata, SetOptions.merge());
                    }

                    // mark the others as not_chosen
                    for (String e : entrants) {
                        if (!winners.contains(e)) {
                            DocumentReference otherRef = events().document(eventId)
                                    .collection("status").document(e);
                            Map<String, Object> odata = new HashMap<>();
                            odata.put("status", "not_chosen");
                            odata.put("timestamp", Timestamp.now());
                            batch.set(otherRef, odata, SetOptions.merge());
                        }
                    }

                    // save meta/lottery doc
                    DocumentReference metaRef = events().document(eventId)
                            .collection("meta")
                            .document("lottery");
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("winners", winners);
                    meta.put("rolledAt", Timestamp.now());
                    batch.set(metaRef, meta, SetOptions.merge());

                    // update event status to completed
                    batch.update(events().document(eventId), "status", "completed");

                    // commit batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onCallback(true))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "rollLottery batch.commit failed", e);
                                callback.onCallback(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "rollLottery: failed to fetch waiting entries", e);
                    callback.onCallback(false);
                });
    }
    // ---------------------------
    // Interface for Fragment callbacks
    // ---------------------------
    public interface FirestoreCallback {
        void onCallback(boolean success);
    }
}

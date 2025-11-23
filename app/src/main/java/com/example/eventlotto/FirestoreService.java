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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Combined FirestoreService
 * Includes:
 *  - User profile CRUD (deviceId-based)
 *  - Event & Notification methods (team version)
 *  - Utility helpers
 */
public class FirestoreService {

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

    /** Returns the DocumentReference for a user by deviceId */
    public DocumentReference getUserRef(String deviceId) {
        return usersCollection.document(deviceId);
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
    // WAITLIST / BATCH ACTIONS
    // ---------------------------

    public Task<Void> joinWaitlist(String eventId, String uid) {
        WriteBatch batch = db.batch();

        // Status document under event
        DocumentReference statusRef = events().document(eventId)
                .collection("status")
                .document(uid);
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("uid", uid);
        statusData.put("status", "waiting");
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
        batch.set(statusTopRef, topStatus, SetOptions.merge());

        return batch.commit();
    }

    // ---------------------------
    // Interface for Fragment callbacks
    // ---------------------------
    public interface FirestoreCallback {
        void onCallback(boolean success);
    }
}

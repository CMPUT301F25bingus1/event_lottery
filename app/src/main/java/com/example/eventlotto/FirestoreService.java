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

public class FirestoreService {
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_EVENTS = "events";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_EVENT_STATUS = "event_status";

    private final FirebaseFirestore db;

    public FirestoreService() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ---------------------------
    // Collection references
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

    public Task<DocumentSnapshot> getUser(String uid) {
        return users().document(uid).get();
    }

    public void deleteUser(String uid, Consumer<Boolean> callback) {
        users().document(uid).delete()
                .addOnSuccessListener(aVoid -> callback.accept(true))
                .addOnFailureListener(e -> callback.accept(false));
    }

    public void userExists(String uid, Consumer<Boolean> callback) {
        users().document(uid).get()
                .addOnSuccessListener(snapshot -> callback.accept(snapshot.exists()))
                .addOnFailureListener(e -> callback.accept(false));
    }

    public Task<Void> saveNotification(Notification notification) {
        if (notification == null || notification.getNid() == null) {
            throw new IllegalArgumentException("notification.nid is required");
        }
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(Timestamp.now());
        }
        return notifications().document(notification.getNid()).set(notification, SetOptions.merge());
    }

    public Task<QuerySnapshot> getEventStatusesForUser(String uid) {
        return eventStatus().whereEqualTo("uid", uid).get();
    }

    public Task<Void> saveEventStatus(EventStatus status) {
        if (status == null) throw new IllegalArgumentException("status required");
        if (status.getSid() == null) {
            // default sid composition: uid_eid
            status.setSid(status.getUid() + "_" + status.getEid());
        }
        return eventStatus().document(status.getSid()).set(status, SetOptions.merge());
    }

    public Task<Void> joinWaitlist(String eventId, String uid) {
        // Create/merge the status doc and a notification subscription in a single batch
        WriteBatch batch = db.batch();

        // Status document under the event
        DocumentReference statusRef = events().document(eventId)
                .collection("status")
                .document(uid);
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("uid", uid);
        statusData.put("status", "waiting");
        batch.set(statusRef, statusData, SetOptions.merge());

        // Notification subscription document
        String nid = uid + "_" + eventId;
        DocumentReference notifRef = notifications().document(nid);
        Map<String, Object> notifData = new HashMap<>();
        notifData.put("nid", nid);
        notifData.put("uid", uid);
        notifData.put("eid", eventId);
        if (Timestamp.now() != null) {
            notifData.put("createdAt", Timestamp.now());
        }
        batch.set(notifRef, notifData, SetOptions.merge());

        // Top-level event_status document to power Notifications screen status chips
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
}

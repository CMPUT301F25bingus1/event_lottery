package com.example.eventlotto;

import com.example.eventlotto.model.Event;
import com.example.eventlotto.model.Notification;
import com.example.eventlotto.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

public class FirestoreService {
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_EVENTS = "events";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";

    private final FirebaseFirestore db;

    public FirestoreService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public CollectionReference users() {
        return db.collection(COLLECTION_USERS);
    }

    public CollectionReference events() {
        return db.collection(COLLECTION_EVENTS);
    }

    public CollectionReference notifications() {
        return db.collection(COLLECTION_NOTIFICATIONS);
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

    public Task<Void> saveEvent(Event event) {
        if (event == null || event.getEid() == null) {
            throw new IllegalArgumentException("event.eid is required");
        }
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(Timestamp.now());
        }
        event.setUpdatedAt(Timestamp.now());
        return events().document(event.getEid()).set(event, SetOptions.merge());
    }

    public Task<DocumentSnapshot> getEvent(String eid) {
        return events().document(eid).get();
    }

    public Task<QuerySnapshot> getEventsByVisibility(String visibility) {
        return events().whereEqualTo("visibility", visibility).get();
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

    public Task<DocumentSnapshot> getNotification(String nid) {
        return notifications().document(nid).get();
    }
}


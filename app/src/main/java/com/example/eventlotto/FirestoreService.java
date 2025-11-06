package com.example.eventlotto;

<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
import com.example.eventlotto.model.Notification;
import com.example.eventlotto.model.EventStatus;
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FirestoreService {
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_EVENTS = "events";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_EVENT_STATUS = "event_status";
=======
public class FirestoreService {
>>>>>>> Stashed changes
=======
public class FirestoreService {
>>>>>>> Stashed changes
=======
public class FirestoreService {
>>>>>>> Stashed changes
=======
public class FirestoreService {
>>>>>>> Stashed changes

    private final FirebaseFirestore db;
    private final CollectionReference usersCollection;

    public FirestoreService() {
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("users");
    }


<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
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

    public void saveUserProfile(User user, FirestoreCallback callback) {
        if (user == null || user.getDeviceId() == null) {
            callback.onCallback(false);
            return;

        }

        usersCollection.document(user.getDeviceId())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));

    }


    public Task<DocumentSnapshot> getUser(String deviceId) {
        return usersCollection.document(deviceId).get();
    }


    public void getUserProfile(String deviceId, FirestoreUserCallback callback) {
        if (deviceId == null || deviceId.isEmpty()) {
            callback.onCallback(null);
            return;
        }

        usersCollection.document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onCallback(user);
                    } else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(null));
    }


<<<<<<< Updated upstream
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
=======
=======
    }


    public Task<DocumentSnapshot> getUser(String deviceId) {
        return usersCollection.document(deviceId).get();
    }


    public void getUserProfile(String deviceId, FirestoreUserCallback callback) {
        if (deviceId == null || deviceId.isEmpty()) {
            callback.onCallback(null);
            return;
        }

        usersCollection.document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onCallback(user);
                    } else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(null));
    }


>>>>>>> Stashed changes
=======
    }


    public Task<DocumentSnapshot> getUser(String deviceId) {
        return usersCollection.document(deviceId).get();
    }


    public void getUserProfile(String deviceId, FirestoreUserCallback callback) {
        if (deviceId == null || deviceId.isEmpty()) {
            callback.onCallback(null);
            return;
        }

        usersCollection.document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onCallback(user);
                    } else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(null));
    }


>>>>>>> Stashed changes
=======
    }


    public Task<DocumentSnapshot> getUser(String deviceId) {
        return usersCollection.document(deviceId).get();
    }


    public void getUserProfile(String deviceId, FirestoreUserCallback callback) {
        if (deviceId == null || deviceId.isEmpty()) {
            callback.onCallback(null);
            return;
        }

        usersCollection.document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onCallback(user);
                    } else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(null));
    }


>>>>>>> Stashed changes
    public void updateUserProfile(User user, FirestoreCallback callback) {
        if (user == null || user.getDeviceId() == null) {
            callback.onCallback(false);
            return;
        }


        user.touch();

        usersCollection.document(user.getDeviceId())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));
    }


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


    public interface FirestoreCallback {
        void onCallback(boolean success);
    }


    public interface FirestoreUserCallback {
        void onCallback(User user);
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
    }
}

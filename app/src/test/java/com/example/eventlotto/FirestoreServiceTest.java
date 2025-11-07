package com.example.eventlotto;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.eventlotto.model.EventStatus;
import com.example.eventlotto.model.Notification;
import com.example.eventlotto.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Map;

/**
 * Unit tests for FirestoreService (pure JVM + Mockito).
 * We stub FirebaseFirestore so no network/device is required.
 *
 * What is coveredd:
 *  - saveUserProfile(): happy path + null user (US 01.02.01 / 01.02.02)
 *  - saveEventStatus(): generates sid when missing (US 01.02.03)
 *  - saveNotification(): sets createdAt + writes (US 01.04.01 / 01.04.02)
 *  - joinWaitlist(): writes 3 docs in a batch + commit (US 01.01.01)
 */
public class FirestoreServiceTest {

    // Global firebase stubs shared by all tests
    private FirebaseFirestore db;
    private MockedStatic<FirebaseFirestore> firebaseStatic;

    private FirestoreService service;

    // Top-level collections used by the service
    private CollectionReference usersCol;
    private CollectionReference eventsCol;
    private CollectionReference notifCol;
    private CollectionReference eventStatusCol;

    @Before
    public void setup() {
        // Make Firestore.getInstance() return our mock
        db = mock(FirebaseFirestore.class);
        firebaseStatic = mockStatic(FirebaseFirestore.class);
        firebaseStatic.when(FirebaseFirestore::getInstance).thenReturn(db);

        // Top-level collection mocks
        usersCol = mock(CollectionReference.class);
        eventsCol = mock(CollectionReference.class);
        notifCol = mock(CollectionReference.class);
        eventStatusCol = mock(CollectionReference.class);

        when(db.collection(FirestoreService.COLLECTION_USERS)).thenReturn(usersCol);
        when(db.collection(FirestoreService.COLLECTION_EVENTS)).thenReturn(eventsCol);
        when(db.collection(FirestoreService.COLLECTION_NOTIFICATIONS)).thenReturn(notifCol);
        when(db.collection(FirestoreService.COLLECTION_EVENT_STATUS)).thenReturn(eventStatusCol);

        // Construct service after wiring the stubs above
        service = new FirestoreService();
    }

    @After
    public void tearDown() {
        if (firebaseStatic != null) firebaseStatic.close();
    }

    // Small helper so tests can easily bind one doc id to one mock
    private DocumentReference stubDoc(CollectionReference col, String id) {
        DocumentReference doc = mock(DocumentReference.class);
        when(col.document(id)).thenReturn(doc);
        return doc;
    }

    // ─────────────────────────────
    // USER PROFILE TESTS
    // ─────────────────────────────

    @Test
    public void saveUserProfile_success_callsCallbackTrue() {
        final String deviceId = "1234567890";
        DocumentReference userDoc = stubDoc(usersCol, deviceId);

        // Return a Task whose onSuccess fires immediately, so callback runs
        @SuppressWarnings("unchecked")
        Task<Void> setTask = mock(Task.class);
        when(userDoc.set(any(User.class), eq(SetOptions.merge()))).thenReturn(setTask);
        when(setTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            com.google.android.gms.tasks.OnSuccessListener<Void> l = inv.getArgument(0);
            l.onSuccess(null);
            return setTask; // allow chaining
        });
        when(setTask.addOnFailureListener(any())).thenReturn(setTask);

        FirestoreService.FirestoreCallback cb = mock(FirestoreService.FirestoreCallback.class);
        User u = new User(deviceId, "Name", "e@x.com", "5551234567");

        service.saveUserProfile(u, cb);

        // Assert
        verify(usersCol).document(deviceId);
        verify(userDoc).set(any(User.class), eq(SetOptions.merge()));
        verify(cb).onCallback(true);
    }

    @Test
    public void saveUserProfile_nullUser_callsCallbackFalse() {
        FirestoreService.FirestoreCallback cb = mock(FirestoreService.FirestoreCallback.class);

        service.saveUserProfile(null, cb);

        // Assert
        verify(cb).onCallback(false);
        // And we should not hit Firestore at all
        verify(usersCol, never()).document(anyString());
    }

    // ─────────────────────────────
    // EVENT STATUS TESTS
    // ─────────────────────────────

    @Test
    public void saveEventStatus_generatesSid_whenMissing() {
        DocumentReference statusDoc = stubDoc(eventStatusCol, "U1_E1");
        when(statusDoc.set(any(EventStatus.class), eq(SetOptions.merge())))
                .thenReturn(Tasks.forResult(null));

        EventStatus es = new EventStatus();
        es.setUid("U1");
        es.setEid("E1");
        es.setStatus("waiting");

        service.saveEventStatus(es);

        // Assert: it should write to doc id "U1_E1"
        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(eventStatusCol).document(cap.capture());
        assertEquals("U1_E1", cap.getValue());
        verify(statusDoc).set(any(EventStatus.class), eq(SetOptions.merge()));
    }

    // ─────────────────────────────
    // NOTIFICATION TESTS
    // ─────────────────────────────

    @Test
    public void saveNotification_addsCreatedAt_andWritesToDoc() {
        DocumentReference nDoc = stubDoc(notifCol, "N1");
        when(nDoc.set(any(Notification.class), eq(SetOptions.merge())))
                .thenReturn(Tasks.forResult(null));

        Notification n = new Notification();
        n.setNid("N1");
        n.setUid("U1");
        n.setEid("E1");

        service.saveNotification(n);

        // Assert
        verify(nDoc).set(any(Notification.class), eq(SetOptions.merge()));
        assertNotNull("createdAt should be set by service", n.getCreatedAt());
        assertTrue(n.getCreatedAt() instanceof Timestamp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveNotification_throwsWithoutNid() {
        // exception expected
        service.saveNotification(new Notification());
    }

    // ─────────────────────────────
    // WAITLIST / BATCH TESTS
    // ─────────────────────────────

    @Test
    public void joinWaitlist_writesThreeDocs_andCommits() {
        WriteBatch batch = mock(WriteBatch.class);
        when(db.batch()).thenReturn(batch);

        // event/{E1}/status/{UID}
        DocumentReference eventDoc = stubDoc(eventsCol, "E1");
        CollectionReference subStatusCol = mock(CollectionReference.class);
        when(eventDoc.collection("status")).thenReturn(subStatusCol);
        DocumentReference eventStatusDoc = stubDoc(subStatusCol, "9876543210");

        // notifications/{UID_EID}
        DocumentReference notifDoc = stubDoc(notifCol, "9876543210_E1");

        // event_status/{UID_EID}
        DocumentReference topStatusDoc = stubDoc(eventStatusCol, "9876543210_E1");

        // Make every batch.set(...) return the same batch for chaining
        when(batch.set(eq(eventStatusDoc), anyMap(), eq(SetOptions.merge()))).thenReturn(batch);
        when(batch.set(eq(notifDoc), anyMap(), eq(SetOptions.merge()))).thenReturn(batch);
        when(batch.set(eq(topStatusDoc), anyMap(), eq(SetOptions.merge()))).thenReturn(batch);
        when(batch.commit()).thenReturn(Tasks.forResult(null));

        Task<Void> t = service.joinWaitlist("E1", "9876543210");

        // Assert
        assertTrue(t.isComplete());
        assertTrue(t.isSuccessful());
        verify(batch, times(3)).set(any(DocumentReference.class), anyMap(), eq(SetOptions.merge()));
        verify(batch).commit();

    }
}

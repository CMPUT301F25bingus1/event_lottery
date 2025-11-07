package com.example.eventlotto;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlotto.model.User;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(com.example.eventlotto.AndroidJUnit4.class)
public class FirestoreServiceTest {

    @Test
    public void firestoreService_exposesCollections() {
        FirestoreService service = new FirestoreService();
        assertNotNull(service.users());
        assertNotNull(service.events());
        assertNotNull(service.notifications());
    }

    @Test
    public void deleteUser_acceptsCallback() {
        FirestoreService service = new FirestoreService();
        // just ensure method is callable with the right signature
        service.deleteUser("someUserId", success -> {
            // usually you'd assert based on Firestore state,
            // but this at least compiles and runs on device
        });
    }

    @Test
    public void saveUser_doesNotThrow_whenUserIsValid() {
        FirestoreService service = new FirestoreService();
        User user = new User("device123", "Test User", "test@example.com", "5555555555");
        // just make sure the Task is returned
        assertNotNull(service.saveUser(user));
    }
}
package com.example.eventlotto;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlotto.model.User;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FirestoreServiceTest {

    @Test
    public void firestoreService_exposesCollections() {
        FirestoreService service = new FirestoreService();
        assertNotNull(service.users());
        assertNotNull(service.events());
        assertNotNull(service.notifications());
    }

    @Test
    public void deleteUser_acceptsCallback() {
        FirestoreService service = new FirestoreService();
        // just ensure method is callable with the right signature
        service.deleteUser("someUserId", success -> {
            // usually you'd assert based on Firestore state,
            // but this at least compiles and runs on device
        });
    }

    @Test
    public void saveUser_doesNotThrow_whenUserIsValid() {
        FirestoreService service = new FirestoreService();
        User user = new User("device123", "Test User", "test@example.com", "5555555555");
        // just make sure the Task is returned
        assertNotNull(service.saveUser(user));
    }
}

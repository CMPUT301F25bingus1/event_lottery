package com.example.eventlotto;

import com.example.eventlotto.model.User;
import com.google.firebase.Timestamp;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void constructor_setsDefaults() {
        String deviceId = "1234567890";
        User u = new User(deviceId, "Name", "e@x.com", "5551234567");

        // uid mirrors deviceId in your constructor
        assertEquals(deviceId, u.getUid());
        assertEquals(deviceId, u.getDeviceId());

        // defaults set by constructor
        assertEquals("entrant", u.getRole());
        assertTrue(u.getNotifyWhenNotSelected());
        assertFalse(u.getGeoConsent());

        assertNotNull(u.getCreatedAt());
        assertNotNull(u.getUpdatedAt());
        assertNull(u.getDeletedAt());
        assertNull(u.getAvatarUrl());

        assertEquals("Name", u.getFullName());
        assertEquals("e@x.com", u.getEmail());
        assertEquals("5551234567", u.getPhone());
    }

    @Test
    public void touch_updatesUpdatedAt_only() throws InterruptedException {
        User u = new User("1234567890", "Name", "e@x.com", "5551234567");
        Timestamp createdAt = u.getCreatedAt();
        Timestamp updatedAt1 = u.getUpdatedAt();

        // Ensure time changes
        Thread.sleep(5);
        u.touch();
        Timestamp updatedAt2 = u.getUpdatedAt();

        assertEquals(createdAt, u.getCreatedAt()); // createdAt unchanged
        assertNotEquals(updatedAt1, updatedAt2);   // updatedAt bumped
    }

    @Test
    public void setters_and_getters_roundTrip() {
        User u = new User("1234567890", "Name", "e@x.com", "5551234567");
        u.setRole("organizer");
        u.setAvatarUrl(null);
        u.setGeoConsent(true);
        u.setNotifyWhenNotSelected(false);
        u.setFullName("New Name");
        u.setEmail("new@example.com");
        u.setPhone("9998887777");

        assertEquals("organizer", u.getRole());
        assertEquals(null, u.getAvatarUrl());
        assertTrue(u.getGeoConsent());
        assertFalse(u.getNotifyWhenNotSelected());
        assertEquals("New Name", u.getFullName());
        assertEquals("new@example.com", u.getEmail());
        assertEquals("9998887777", u.getPhone());
    }
}

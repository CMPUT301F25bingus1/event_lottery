package com.example.eventlotto;

import com.example.eventlotto.model.Notification;
import com.google.firebase.Timestamp;

import org.junit.Test;

import static org.junit.Assert.*;

public class NotificationTest {

    @Test
    public void default_constructor_leavesFieldsNull() {
        Notification n = new Notification();

        // Assert
        assertNull(n.getNid());
        assertNull(n.getUid());
        assertNull(n.getEid());
        assertNull(n.getCreatedAt());
        assertNull(n.getLastSentAt());
    }

    @Test
    public void setters_getters_roundTrip() {
        Notification n = new Notification();
        n.setNid("nid123");
        n.setUid("1234567890");
        n.setEid("evt42");

        Timestamp created = Timestamp.now();
        Timestamp lastSent = Timestamp.now();

        n.setCreatedAt(created);
        n.setLastSentAt(lastSent);

        // Assert
        assertEquals("nid123", n.getNid());
        assertEquals("1234567890", n.getUid());
        assertEquals("evt42", n.getEid());
        assertEquals(created, n.getCreatedAt());
        assertEquals(lastSent, n.getLastSentAt());
    }
}

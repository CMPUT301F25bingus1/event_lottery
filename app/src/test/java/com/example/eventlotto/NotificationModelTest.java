package com.example.eventlotto;

import com.example.eventlotto.model.Notification;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Notification model class
 * Tests cover notification creation and logging
 */
public class NotificationModelTest {

    private Notification testNotification;

    @Before
    public void setUp() {
        testNotification = new Notification();
    }

    /**
     * US 01.04.01: Test "won" notification creation
     */
    @Test
    public void testWonNotificationCreation() {
        testNotification.setNid("notif_001");
        testNotification.setUid("user_123");
        testNotification.setEid("event_456");
        testNotification.setMessage("Congratulations! You've been selected for the lottery.");
        testNotification.setCreatedAt(Timestamp.now());

        assertEquals("user_123", testNotification.getUid());
        assertEquals("event_456", testNotification.getEid());
        assertNotNull(testNotification.getCreatedAt());
        assertTrue(testNotification.getMessage().contains("selected"));
    }

    /**
     * US 01.04.02: Test "not selected" notification
     */
    @Test
    public void testNotSelectedNotificationCreation() {
        testNotification.setNid("notif_002");
        testNotification.setUid("user_456");
        testNotification.setEid("event_789");
        testNotification.setMessage("Unfortunately, you were not selected this time. Try again next time!");
        testNotification.setCreatedAt(Timestamp.now());

        assertEquals("user_456", testNotification.getUid());
        assertEquals("event_789", testNotification.getEid());
        assertTrue(testNotification.getMessage().contains("not selected"));
    }

    /**
     * US 02.07.01: Test bulk notification to waiting list
     */
    @Test
    public void testWaitingListNotification() {
        testNotification.setEid("event_123");
        testNotification.setMessage("Important update about your event!");
        testNotification.setCreatedAt(Timestamp.now());

        assertEquals("event_123", testNotification.getEid());
        assertNotNull(testNotification.getMessage());
    }

    /**
     * US 02.07.02: Test bulk notification to selected entrants
     */
    @Test
    public void testSelectedEntrantsNotification() {
        Notification notif1 = new Notification();
        notif1.setUid("user_1");
        notif1.setEid("event_X");
        notif1.setMessage("You've been selected!");
        notif1.setCreatedAt(Timestamp.now());

        Notification notif2 = new Notification();
        notif2.setUid("user_2");
        notif2.setEid("event_X");
        notif2.setMessage("You've been selected!");
        notif2.setCreatedAt(Timestamp.now());

        assertEquals("You've been selected!", notif1.getMessage());
        assertEquals("You've been selected!", notif2.getMessage());
    }

    /**
     * US 02.07.03: Test bulk notification to cancelled entrants
     */
    @Test
    public void testCancelledEntrantsNotification() {
        testNotification.setUid("user_cancelled");
        testNotification.setEid("event_456");
        testNotification.setMessage("Your registration has been cancelled.");
        testNotification.setCreatedAt(Timestamp.now());

        assertEquals("user_cancelled", testNotification.getUid());
        assertTrue(testNotification.getMessage().contains("cancelled"));
    }

    /**
     * US 03.08.01: Test notification logging
     */
    @Test
    public void testNotificationLogging() {
        testNotification.setNid("notif_audit_001");
        testNotification.setUid("user_123");
        testNotification.setEid("event_456");
        testNotification.setMessage("Test message");

        Timestamp sentAt = Timestamp.now();
        testNotification.setCreatedAt(sentAt);
        testNotification.setLastSentAt(sentAt);

        assertEquals("notif_audit_001", testNotification.getNid());
        assertEquals(sentAt, testNotification.getCreatedAt());
        assertEquals(sentAt, testNotification.getLastSentAt());
    }

    /**
     * Test notification retry tracking
     */
    @Test
    public void testNotificationResendTracking() {
        Timestamp initialSend = Timestamp.now();
        testNotification.setCreatedAt(initialSend);
        testNotification.setLastSentAt(initialSend);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Simulate resend
        Timestamp resendTime = Timestamp.now();
        testNotification.setLastSentAt(resendTime);

        assertTrue(testNotification.getLastSentAt().getSeconds() >=
                testNotification.getCreatedAt().getSeconds());
    }

    /**
     * Test notification message customization
     */
    @Test
    public void testNotificationMessageCustomization() {
        String customMessage = "Don't miss out! Registration closes in 2 hours.";
        testNotification.setMessage(customMessage);

        assertEquals(customMessage, testNotification.getMessage());
    }

    /**
     * Test notification targeting by event
     */
    @Test
    public void testNotificationTargetingByEvent() {
        Notification notif1 = new Notification();
        notif1.setEid("event_concert");
        notif1.setMessage("Concert update");

        Notification notif2 = new Notification();
        notif2.setEid("event_art_show");
        notif2.setMessage("Art show update");

        assertEquals("event_concert", notif1.getEid());
        assertEquals("event_art_show", notif2.getEid());
        assertNotEquals(notif1.getEid(), notif2.getEid());
    }

    /**
     * Test notification targeting by user
     */
    @Test
    public void testNotificationTargetingByUser() {
        Notification notif1 = new Notification();
        notif1.setUid("user_alice");
        notif1.setEid("event_X");
        notif1.setMessage("Message to Alice");

        Notification notif2 = new Notification();
        notif2.setUid("user_bob");
        notif2.setEid("event_X");
        notif2.setMessage("Message to Bob");

        assertEquals("user_alice", notif1.getUid());
        assertEquals("user_bob", notif2.getUid());
        assertNotEquals(notif1.getUid(), notif2.getUid());
    }

    /**
     * Test notification with empty user ID (broadcast)
     */
    @Test
    public void testBroadcastNotification() {
        testNotification.setUid(null);
        testNotification.setEid("event_broadcast");
        testNotification.setMessage("Event update for all entrants");

        assertNull(testNotification.getUid());
        assertEquals("event_broadcast", testNotification.getEid());
    }

    /**
     * Test notification tracking chain
     */
    @Test
    public void testNotificationLifecycle() {
        String nid = "notif_lifecycle_001";
        String uid = "user_test";
        String eid = "event_test";
        String message = "Test notification";

        // Creation
        testNotification.setNid(nid);
        testNotification.setUid(uid);
        testNotification.setEid(eid);
        testNotification.setMessage(message);
        testNotification.setCreatedAt(Timestamp.now());

        // Logging when sent
        testNotification.setLastSentAt(Timestamp.now());

        // Verification
        assertEquals(nid, testNotification.getNid());
        assertEquals(uid, testNotification.getUid());
        assertEquals(eid, testNotification.getEid());
        assertEquals(message, testNotification.getMessage());
        assertNotNull(testNotification.getCreatedAt());
        assertNotNull(testNotification.getLastSentAt());
    }
}


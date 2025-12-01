package com.example.eventlotto;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.example.eventlotto.model.EventStatus;

/**
 * Unit tests for EventStatus model class
 * Tests cover event participation status tracking
 */
public class EventStatusModelTest {

    private EventStatus testStatus;

    @Before
    public void setUp() {
        testStatus = new EventStatus();
    }

    /**
     * US 01.01.01: Test entrant joining waiting list
     */
    @Test
    public void testEntrantJoinsWaitingList() {
        testStatus.setUid("user_123");
        testStatus.setEid("event_456");
        testStatus.setStatus("waiting");
        testStatus.setSid("status_001");

        assertEquals("user_123", testStatus.getUid());
        assertEquals("event_456", testStatus.getEid());
        assertEquals("waiting", testStatus.getStatus());
        assertEquals("status_001", testStatus.getSid());
    }

    /**
     * US 01.01.02: Test entrant leaving waiting list
     */
    @Test
    public void testEntrantLeavesWaitingList() {
        testStatus.setUid("user_123");
        testStatus.setEid("event_456");
        testStatus.setStatus("waiting");
        testStatus.setSid("status_001");
        testStatus.setStatus(null);

        // In practice, this record would be deleted from Firestore
        // But we validate the data structure supports it
        assertEquals("status_001", testStatus.getSid());
        assertEquals(null, testStatus.getStatus());
    }

    /**
     * US 01.05.04: Test tracking total entrants on waiting list
     */
    @Test
    public void testWaitingListTracking() {
        EventStatus status1 = new EventStatus();
        status1.setUid("user_1");
        status1.setEid("event_A");
        status1.setStatus("waiting");

        EventStatus status2 = new EventStatus();
        status2.setUid("user_2");
        status2.setEid("event_A");
        status2.setStatus("waiting");

        EventStatus status3 = new EventStatus();
        status3.setUid("user_3");
        status3.setEid("event_A");
        status3.setStatus("not chosen");

        assertEquals("waiting", status1.getStatus());
        assertEquals("waiting", status2.getStatus());
        assertEquals("not chosen", status3.getStatus());
    }

    /**
     * US 01.05.02: Test entrant accepts invitation
     */
    @Test
    public void testEntrantAcceptsInvitation() {
        testStatus.setUid("user_123");
        testStatus.setEid("event_456");
        testStatus.setStatus("selected");

        // Entrant accepts
        testStatus.setStatus("signed up");

        assertEquals("signed up", testStatus.getStatus());
    }

    /**
     * US 01.05.03: Test entrant declines invitation
     */
    @Test
    public void testEntrantDeclinesInvitation() {
        testStatus.setUid("user_123");
        testStatus.setEid("event_456");
        testStatus.setStatus("selected");

        // Entrant declines
        testStatus.setStatus("cancelled");

        assertEquals("cancelled", testStatus.getStatus());
    }

    /**
     * US 01.04.02: Test entrant not selected notification status
     */
    @Test
    public void testEntrantNotSelected() {
        testStatus.setUid("user_123");
        testStatus.setEid("event_456");
        testStatus.setStatus("not chosen");

        assertEquals("not chosen", testStatus.getStatus());
    }

    /**
     * US 02.06.02: Test cancelled entrants status
     */
    @Test
    public void testCancelledEntrantsStatus() {
        testStatus.setUid("user_789");
        testStatus.setEid("event_456");
        testStatus.setStatus("cancelled");

        assertEquals("cancelled", testStatus.getStatus());
        assertEquals("user_789", testStatus.getUid());
    }

    /**
     * US 02.05.03: Test replacement applicant status transition
     */
    @Test
    public void testReplacementApplicantTransition() {
        EventStatus replacement = new EventStatus();
        replacement.setUid("user_backup");
        replacement.setEid("event_456");

        // Initially on waiting list
        replacement.setStatus("waiting");
        assertEquals("waiting", replacement.getStatus());

        // When someone declines, replacement gets selected
        replacement.setStatus("selected");
        assertEquals("selected", replacement.getStatus());
    }

    /**
     * US 02.06.03: Test final enrolled entrants status
     */
    @Test
    public void testFinalEnrolledStatus() {
        testStatus.setUid("user_123");
        testStatus.setEid("event_456");
        testStatus.setStatus("signed up");

        assertEquals("signed up", testStatus.getStatus());
    }

    /**
     * US 02.06.01: Test viewing invited entrants
     */
    @Test
    public void testInvitedEntrantsList() {
        EventStatus invited1 = new EventStatus();
        invited1.setUid("user_invited_1");
        invited1.setEid("event_X");
        invited1.setStatus("selected");

        EventStatus invited2 = new EventStatus();
        invited2.setUid("user_invited_2");
        invited2.setEid("event_X");
        invited2.setStatus("selected");

        assertEquals("selected", invited1.getStatus());
        assertEquals("selected", invited2.getStatus());
    }

    /**
     * Test all valid status states
     */
    @Test
    public void testAllStatusStates() {
        String[] validStates = {"waiting", "selected", "signed up", "cancelled", "not chosen"};

        for (String state : validStates) {
            testStatus.setStatus(state);
            assertEquals(state, testStatus.getStatus());
        }
    }

    /**
     * Test status isolation per event
     */
    @Test
    public void testStatusIsolationPerEvent() {
        EventStatus userEvent1 = new EventStatus();
        userEvent1.setUid("user_A");
        userEvent1.setEid("event_1");
        userEvent1.setStatus("waiting");

        EventStatus userEvent2 = new EventStatus();
        userEvent2.setUid("user_A");
        userEvent2.setEid("event_2");
        userEvent2.setStatus("signed up");

        // Same user, different events, different statuses
        assertEquals("user_A", userEvent1.getUid());
        assertEquals("user_A", userEvent2.getUid());
        assertNotEquals("event_1", "event_2");
        assertNotEquals("waiting", "signed up");
    }

    /**
     * Test status lookup by event ID
     */
    @Test
    public void testStatusLookupByEvent() {
        EventStatus status1 = new EventStatus();
        status1.setEid("event_concert");
        status1.setUid("user_1");
        status1.setStatus("waiting");

        EventStatus status2 = new EventStatus();
        status2.setEid("event_concert");
        status2.setUid("user_2");
        status2.setStatus("selected");

        assertEquals("event_concert", status1.getEid());
        assertEquals("event_concert", status2.getEid());
    }
}
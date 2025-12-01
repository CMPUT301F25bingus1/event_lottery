package com.example.eventlotto;
import com.example.eventlotto.model.Event;
import com.example.eventlotto.model.EventStatus;
import com.example.eventlotto.model.FollowedEvent;
import com.example.eventlotto.model.Notification;
import com.example.eventlotto.model.User;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Integration tests for EventLotto model classes.
 * Tests cover cross-model scenarios and workflows
 */
public class ModelIntegrationTest {

    private User testUser;
    private Event testEvent;

    @Before
    public void setUp() {
        testUser = new User("device_test_001", "John Doe", "john@example.com", null);
        testEvent = new Event();
        testEvent.setEid("event_test_001");
    }

    /**
     * US 01.02.03: Test user event history
     */
    @Test
    public void testUserEventHistory() {
        EventStatus history1 = new EventStatus();
        history1.setUid(testUser.getUid());
        history1.setEid("event_1");
        history1.setStatus("signed up");

        EventStatus history2 = new EventStatus();
        history2.setUid(testUser.getUid());
        history2.setEid("event_2");
        history2.setStatus("not chosen");

        EventStatus history3 = new EventStatus();
        history3.setUid(testUser.getUid());
        history3.setEid("event_3");
        history3.setStatus("cancelled");

        assertEquals(testUser.getUid(), history1.getUid());
        assertEquals(testUser.getUid(), history2.getUid());
        assertEquals(testUser.getUid(), history3.getUid());
        assertEquals("signed up", history1.getStatus());
        assertEquals("not chosen", history2.getStatus());
        assertEquals("cancelled", history3.getStatus());
    }

    /**
     * US 02.01.01 + 02.05.02: Test event with selected entrants
     */
    @Test
    public void testEventSelectionProcess() {
        testEvent.setCapacity(100L);
        testEvent.setEntrantsApplied(250L);

        // Lottery winner
        EventStatus winner = new EventStatus();
        winner.setUid("user_lucky");
        winner.setEid(testEvent.getEid());
        winner.setStatus("selected");

        // Lottery loser
        EventStatus loser = new EventStatus();
        loser.setUid("user_unlucky");
        loser.setEid(testEvent.getEid());
        loser.setStatus("not chosen");

        assertEquals(Long.valueOf(100L), testEvent.getCapacity());
        assertEquals(Long.valueOf(250L), testEvent.getEntrantsApplied());
        assertEquals("selected", winner.getStatus());
        assertEquals("not chosen", loser.getStatus());
    }

    /**
     * US 02.06.01-05: Test organizing entrant lists
     */
    @Test
    public void testEventEntrantStates() {
        // Different states of entrants for event
        EventStatus waiting = new EventStatus();
        waiting.setEid(testEvent.getEid());
        waiting.setUid("user_waiting");
        waiting.setStatus("waiting");

        EventStatus selected = new EventStatus();
        selected.setEid(testEvent.getEid());
        selected.setUid("user_selected");
        selected.setStatus("selected");

        EventStatus enrolled = new EventStatus();
        enrolled.setEid(testEvent.getEid());
        enrolled.setUid("user_enrolled");
        enrolled.setStatus("signed up");

        EventStatus cancelled = new EventStatus();
        cancelled.setEid(testEvent.getEid());
        cancelled.setUid("user_cancelled");
        cancelled.setStatus("cancelled");

        EventStatus notChosen = new EventStatus();
        notChosen.setEid(testEvent.getEid());
        notChosen.setUid("user_not_chosen");
        notChosen.setStatus("not chosen");

        assertEquals("waiting", waiting.getStatus());
        assertEquals("selected", selected.getStatus());
        assertEquals("signed up", enrolled.getStatus());
        assertEquals("cancelled", cancelled.getStatus());
        assertEquals("not chosen", notChosen.getStatus());
    }

    /**
     * US 01.05.02: Test entrant acceptance workflow
     */
    @Test
    public void testAcceptanceWorkflow() {
        User entrant = new User("device_entrant", "Jane Doe", "jane@example.com", null);

        // Entrant joins waiting list
        EventStatus status = new EventStatus();
        status.setUid(entrant.getUid());
        status.setEid("event_123");
        status.setStatus("waiting");
        assertEquals("waiting", status.getStatus());

        // Lottery happens, entrant selected
        status.setStatus("selected");
        assertEquals("selected", status.getStatus());

        // Notification sent
        Notification notif = new Notification();
        notif.setUid(entrant.getUid());
        notif.setEid("event_123");
        notif.setMessage("You've been selected!");
        assertNotNull(notif.getMessage());

        // Entrant accepts
        status.setStatus("signed up");
        assertEquals("signed up", status.getStatus());
    }

    /**
     * US 01.05.03: Test entrant decline workflow
     */
    @Test
    public void testDeclineAndReplacementWorkflow() {
        User selectedEntrant = new User("device_selected", "Bob Smith", "bob@example.com", null);
        User backupEntrant = new User("device_backup", "Alice Jones", "alice@example.com", null);

        // Selected entrant
        EventStatus selectedStatus = new EventStatus();
        selectedStatus.setUid(selectedEntrant.getUid());
        selectedStatus.setEid("event_456");
        selectedStatus.setStatus("selected");

        // Backup on waiting list
        EventStatus backupStatus = new EventStatus();
        backupStatus.setUid(backupEntrant.getUid());
        backupStatus.setEid("event_456");
        backupStatus.setStatus("waiting");

        // Selected declines
        selectedStatus.setStatus("cancelled");
        assertEquals("cancelled", selectedStatus.getStatus());

        // Notification to backup
        Notification backupNotif = new Notification();
        backupNotif.setUid(backupEntrant.getUid());
        backupNotif.setEid("event_456");
        backupNotif.setMessage("You've been selected as a replacement!");

        // Backup moves to selected
        backupStatus.setStatus("selected");
        assertEquals("selected", backupStatus.getStatus());
    }

    /**
     * Test complete user event registration flow
     */
    @Test
    public void testCompleteUserJourney() {
        // Step 1: User creates profile
        User user = new User("device_complete_journey", "Complete User", "user@example.com", "+1234567890");
        assertEquals("Complete User", user.getFullName());
        assertEquals("entrant", user.getRole());

        // Step 2: Event exists and organizer set it up
        Event event = new Event();
        event.setEid("event_complete");
        event.setCapacity(50L);
        event.setRegistrationOpensAt(Timestamp.now());
        assertEquals("event_complete", event.getEid());

        // Step 3: User finds event and joins waiting list
        EventStatus status = new EventStatus();
        status.setUid(user.getUid());
        status.setEid(event.getEid());
        status.setStatus("waiting");
        assertEquals("waiting", status.getStatus());

        // Step 4: Lottery runs, user selected
        status.setStatus("selected");
        assertEquals("selected", status.getStatus());

        // Step 5: Notification sent to user
        Notification notification = new Notification();
        notification.setUid(user.getUid());
        notification.setEid(event.getEid());
        notification.setMessage("Congratulations! You've been selected!");
        assertNotNull(notification.getMessage());

        // Step 6: User accepts invitation
        status.setStatus("signed up");
        assertEquals("signed up", status.getStatus());
    }

    /**
     * Test organizer event management workflow
     */
    @Test
    public void testOrganizerManagementWorkflow() {
        // Step 1: Organizer creates event
        Event event = new Event();
        event.setEid("event_org_001");
        event.setCapacity(30L);
        event.setMaxEntrants(100L);

        assertEquals("event_org_001", event.getEid());
        assertEquals(Long.valueOf(100L), event.getMaxEntrants());

        // Step 2: Entrants join waiting list
        EventStatus entrant1 = new EventStatus();
        entrant1.setUid("user_1");
        entrant1.setEid(event.getEid());
        entrant1.setStatus("waiting");

        EventStatus entrant2 = new EventStatus();
        entrant2.setUid("user_2");
        entrant2.setEid(event.getEid());
        entrant2.setStatus("waiting");

        EventStatus entrant3 = new EventStatus();
        entrant3.setUid("user_3");
        entrant3.setEid(event.getEid());
        entrant3.setStatus("waiting");

        // Step 3: Organizer runs lottery, selects winners
        entrant1.setStatus("selected");
        entrant2.setStatus("selected");
        entrant3.setStatus("not chosen");

        assertEquals("selected", entrant1.getStatus());
        assertEquals("selected", entrant2.getStatus());
        assertEquals("not chosen", entrant3.getStatus());

        // Step 4: Winners are notified
        Notification notif1 = new Notification();
        notif1.setUid("user_1");
        notif1.setEid(event.getEid());
        notif1.setMessage("Selected!");

        Notification notif3 = new Notification();
        notif3.setUid("user_3");
        notif3.setEid(event.getEid());
        notif3.setMessage("Not selected");

        // Step 5: Winners sign up
        entrant1.setStatus("signed up");
        entrant2.setStatus("signed up");

        assertEquals("signed up", entrant1.getStatus());
        assertEquals("signed up", entrant2.getStatus());
        assertEquals("not chosen", entrant3.getStatus());
    }

    /**
     * Test notification tracking across event lifecycle
     */
    @Test
    public void testNotificationAuditTrail() {
        String eventId = "event_audit";

        // Selection notifications
        Notification selectionNotif = new Notification();
        selectionNotif.setNid("notif_selection");
        selectionNotif.setUid("user_selected");
        selectionNotif.setEid(eventId);
        selectionNotif.setMessage("You were selected");
        selectionNotif.setCreatedAt(Timestamp.now());

        // Non-selection notification
        Notification rejectionNotif = new Notification();
        rejectionNotif.setNid("notif_rejection");
        rejectionNotif.setUid("user_rejected");
        rejectionNotif.setEid(eventId);
        rejectionNotif.setMessage("You were not selected");
        rejectionNotif.setCreatedAt(Timestamp.now());

        // Both logged
        assertNotNull(selectionNotif.getNid());
        assertNotNull(rejectionNotif.getNid());
        assertEquals(eventId, selectionNotif.getEid());
        assertEquals(eventId, rejectionNotif.getEid());
    }

    /**
     * Test user notification preference across events
     */
    @Test
    public void testUserNotificationPreferencesPerEvent() {
        User user = new User("device_prefs", "Pref User", "prefs@example.com", null);

        // Initially can receive notifications
        assertTrue(user.getNotifyWhenNotSelected());

        // User follows two events but with different notification preferences
        FollowedEvent event1 = new FollowedEvent(
                "event_pref_1",
                "Event 1",
                "Desc",
                android.R.drawable.ic_dialog_map,
                null,
                true, // notifications enabled
                null
        );

        FollowedEvent event2 = new FollowedEvent(
                "event_pref_2",
                "Event 2",
                "Desc",
                android.R.drawable.ic_dialog_map,
                null,
                false, // notifications disabled
                null
        );

        assertTrue(event1.isNotificationsEnabled());
        assertFalse(event2.isNotificationsEnabled());
    }
}
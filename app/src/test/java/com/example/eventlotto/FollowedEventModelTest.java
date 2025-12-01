package com.example.eventlotto;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.example.eventlotto.model.FollowedEvent;

/**
 * Unit tests for FollowedEvent model class
 * Tests cover event display and user preferences
 */
public class FollowedEventModelTest {

    private FollowedEvent testFollowedEvent;

    @Before
    public void setUp() {
        testFollowedEvent = new FollowedEvent(
                "event_123",
                "Summer Concert",
                "Join us for live music",
                android.R.drawable.ic_dialog_map, // Placeholder drawable ID
                null,
                true,
                null
        );
    }

    /**
     * US 01.01.03: Test viewing available events to join
     */
    @Test
    public void testFollowedEventDisplay() {
        assertEquals("event_123", testFollowedEvent.getId());
        assertEquals("Summer Concert", testFollowedEvent.getName());
        assertEquals("Join us for live music", testFollowedEvent.getDescription());
    }

    /**
     * US 01.04.03: Test entrant can disable notifications per event
     */
    @Test
    public void testFollowedEventNotificationToggle() {
        assertTrue(testFollowedEvent.isNotificationsEnabled());

        // Entrant disables notifications for this event
        testFollowedEvent.setNotificationsEnabled(false);
        assertFalse(testFollowedEvent.isNotificationsEnabled());

        // Re-enable
        testFollowedEvent.setNotificationsEnabled(true);
        assertTrue(testFollowedEvent.isNotificationsEnabled());
    }

    /**
     * US 01.05.05: Test lottery criteria/guidelines message
     */
    @Test
    public void testFollowedEventLotteryMessage() {
        String lotteryInfo = "Selection is random from all waiting list entrants.";
        testFollowedEvent.setMessage(lotteryInfo);

        assertEquals(lotteryInfo, testFollowedEvent.getMessage());
    }

    /**
     * US 02.04.01: Test followed event image URL
     */
    @Test
    public void testFollowedEventImageUrl() {
        String imageUrl = "https://example.com/posters/concert.jpg";
        FollowedEvent eventWithImage = new FollowedEvent(
                "event_456",
                "Art Exhibition",
                "Modern art display",
                android.R.drawable.ic_dialog_map,
                imageUrl,
                true,
                null
        );

        assertEquals(imageUrl, eventWithImage.getImageUrl());
    }

    /**
     * Test followed event with null image URL
     */
    @Test
    public void testFollowedEventFallbackImage() {
        FollowedEvent eventNoImage = new FollowedEvent(
                "event_789",
                "Basketball Tournament",
                "Friendly competition",
                android.R.drawable.ic_dialog_map,
                null,
                true,
                null
        );

        assertNull(eventNoImage.getImageUrl());
        assertEquals(android.R.drawable.ic_dialog_map, eventNoImage.getImageResId());
    }

    /**
     * Test followed event with both drawable and URL
     */
    @Test
    public void testFollowedEventImagePriority() {
        String imageUrl = "https://example.com/image.jpg";
        FollowedEvent event = new FollowedEvent(
                "event_dual",
                "Event with Images",
                "Description",
                android.R.drawable.ic_dialog_map,
                imageUrl,
                true,
                null
        );

        assertEquals(imageUrl, event.getImageUrl());
        assertEquals(android.R.drawable.ic_dialog_map, event.getImageResId());
    }

    /**
     * Test followed event name and description
     */
    @Test
    public void testFollowedEventMetadata() {
        String name = "Jazz Night";
        String description = "An evening of smooth jazz";

        FollowedEvent event = new FollowedEvent(
                "event_jazz",
                name,
                description,
                android.R.drawable.ic_dialog_map,
                null,
                true,
                null
        );

        assertEquals(name, event.getName());
        assertEquals(description, event.getDescription());
    }

    /**
     * Test followed event with organizer message
     */
    @Test
    public void testFollowedEventOrganizerMessage() {
        String message = "Please arrive 15 minutes early. Limited parking available.";

        FollowedEvent event = new FollowedEvent(
                "event_msg",
                "Outdoor Festival",
                "Community gathering",
                android.R.drawable.ic_dialog_map,
                null,
                true,
                message
        );

        assertEquals(message, event.getMessage());
    }

    /**
     * Test modifying followed event message
     */
    @Test
    public void testModifyFollowedEventMessage() {
        String originalMessage = "Original message";
        testFollowedEvent.setMessage(originalMessage);
        assertEquals(originalMessage, testFollowedEvent.getMessage());

        String updatedMessage = "Updated guidelines";
        testFollowedEvent.setMessage(updatedMessage);
        assertEquals(updatedMessage, testFollowedEvent.getMessage());
    }

    /**
     * Test multiple followed events independence
     */
    @Test
    public void testMultipleFollowedEventsIndependent() {
        FollowedEvent event1 = new FollowedEvent(
                "event_1",
                "Event 1",
                "Description 1",
                android.R.drawable.ic_dialog_map,
                null,
                true,
                null
        );

        FollowedEvent event2 = new FollowedEvent(
                "event_2",
                "Event 2",
                "Description 2",
                android.R.drawable.ic_dialog_map,
                null,
                false,
                null
        );

        // Event 1 notifications enabled
        assertTrue(event1.isNotificationsEnabled());

        // Event 2 notifications disabled
        assertFalse(event2.isNotificationsEnabled());

        // Toggle event 1 independently
        event1.setNotificationsEnabled(false);
        assertFalse(event1.isNotificationsEnabled());

        assertFalse(event2.isNotificationsEnabled());
    }

    /**
     * Test followed event unique IDs
     */
    @Test
    public void testFollowedEventUniqueIds() {
        FollowedEvent event1 = new FollowedEvent(
                "event_unique_1",
                "First Event",
                "Desc 1",
                android.R.drawable.ic_dialog_map,
                null,
                true,
                null
        );

        FollowedEvent event2 = new FollowedEvent(
                "event_unique_2",
                "Second Event",
                "Desc 2",
                android.R.drawable.ic_dialog_map,
                null,
                true,
                null
        );

        assertNotEquals(event1.getId(), event2.getId());
    }

    /**
     * Test followed event drawable resource ID
     */
    @Test
    public void testFollowedEventDrawableResId() {
        int drawableId = android.R.drawable.ic_dialog_map;

        FollowedEvent event = new FollowedEvent(
                "event_drawable",
                "Event",
                "Desc",
                drawableId,
                null,
                true,
                null
        );

        assertEquals(drawableId, event.getImageResId());
    }
}

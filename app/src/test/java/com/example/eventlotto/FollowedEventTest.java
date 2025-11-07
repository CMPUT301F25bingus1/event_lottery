package com.example.eventlotto;

import com.example.eventlotto.notifications.FollowedEvent;

import org.junit.Test;

import static org.junit.Assert.*;

public class FollowedEventTest {

    @Test
    public void followedEventConstructor_setsAllFields() {
        FollowedEvent event = new FollowedEvent(
                "E123",
                "Holiday Draw",
                "Big giveaway",
                FollowedEvent.Status.WAITING,
                0,
                true,
                false
        );

        assertEquals("E123", event.getId());
        assertEquals("Holiday Draw", event.getName());
        assertEquals("Big giveaway", event.getDescription());
        assertEquals(FollowedEvent.Status.WAITING, event.getStatus());
        assertTrue(event.isNotificationsEnabled());
        assertFalse(event.isClosed());
    }

    @Test
    public void canToggleNotifications() {
        FollowedEvent event = new FollowedEvent(
                "E123",
                "Holiday Draw",
                "Big giveaway",
                FollowedEvent.Status.ACCEPTED,
                0,
                true,
                false
        );

        event.setNotificationsEnabled(false);
        assertFalse(event.isNotificationsEnabled());
    }
}

package com.example.eventlotto;

import com.example.eventlotto.model.FollowedEvent;

import org.junit.Test;

import static org.junit.Assert.*;

public class FollowedEventTest {

    @Test
    public void constructor_setsFields_andIgnoresClosedParam() {
        FollowedEvent f = new FollowedEvent(
                "evt123",
                "Sample Event",
                "Desc",
                /* imageResId */ 12345,
                /* notificationsEnabled */ true,
                /* closed (ignored by model) */ true
        );

        // Assert
        assertEquals("evt123", f.getId());
        assertEquals("Sample Event", f.getName());
        assertEquals("Desc", f.getDescription());
        assertEquals(12345, f.getImageResId());
        assertTrue(f.isNotificationsEnabled());
    }

    @Test
    public void toggle_notificationsEnabled_roundTrip() {
        FollowedEvent f = new FollowedEvent("e1","n","d",1, false, false);
        assertFalse(f.isNotificationsEnabled());

        f.setNotificationsEnabled(true);
        assertTrue(f.isNotificationsEnabled());

        f.setNotificationsEnabled(false);
        assertFalse(f.isNotificationsEnabled());
    }
}

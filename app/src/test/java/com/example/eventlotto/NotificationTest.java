package com.example.eventlotto;

import com.example.eventlotto.model.Notification;
import com.google.firebase.Timestamp;

import org.junit.Test;

import static org.junit.Assert.*;

public class NotificationTest {

    @Test
    public void notificationSettersAndGetters_work() {
        Notification notification = new Notification();
        notification.setNid("N1");
        notification.setUid("U1");
        notification.setEid("E1");
        Timestamp now = Timestamp.now();
        notification.setCreatedAt(now);

        assertEquals("N1", notification.getNid());
        assertEquals("U1", notification.getUid());
        assertEquals("E1", notification.getEid());
        assertEquals(now, notification.getCreatedAt());
    }
}

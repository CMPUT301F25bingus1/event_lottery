package com.example.eventlotto;

import com.example.eventlotto.model.Event;

import org.junit.Test;

import static org.junit.Assert.*;

public class EventTest {

    @Test
    public void eventSettersAndGetters_work() {
        Event event = new Event();
        event.setEid("E123");
        event.setUid("organizer123");
        event.setTitle("Campus Lottery");
        event.setDescription("Win a tablet!");
        event.setVenueName("Student Center");
        event.setVenueAddress("123 College Way");
        event.setLat(49.2827);
        event.setLng(-123.1207);

        assertEquals("E123", event.getEid());
        assertEquals("organizer123", event.getUid());
        assertEquals("Campus Lottery", event.getTitle());
        assertEquals("Win a tablet!", event.getDescription());
        assertEquals("Student Center", event.getVenueName());
        assertEquals("123 College Way", event.getVenueAddress());
        assertEquals(Double.valueOf(49.2827), event.getLat());
        assertEquals(Double.valueOf(-123.1207), event.getLng());
    }
}

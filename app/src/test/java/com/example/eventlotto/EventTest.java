package com.example.eventlotto;

import com.example.eventlotto.model.Event;

import org.junit.Test;

import static org.junit.Assert.*;

public class EventTest {

    @Test
    public void defaults_areNull_and_setEidWorks() {
        Event e = new Event();

        // All fields except eid are accessed via getters only; default nulls
        assertNull(e.getEventTitle());
        assertNull(e.getDescription());
        assertNull(e.getCreatedAt());
        assertNull(e.getEventStartAt());
        assertNull(e.getEventEndAt());
        assertNull(e.getRegistrationOpensAt());
        assertNull(e.getRegistrationClosesAt());
        assertNull(e.getCapacity());
        assertNull(e.getGeoConsent());
        assertNull(e.getNotifyWhenNotSelected());
        assertNull(e.getImageId());
        assertNull(e.getOrganizerId());
        assertNull(e.getLocation());
        assertNull(e.getEid());

        // setEid and Assert
        e.setEid("E123");
        assertEquals("E123", e.getEid());
    }
}

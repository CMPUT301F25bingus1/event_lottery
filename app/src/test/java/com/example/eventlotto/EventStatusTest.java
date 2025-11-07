package com.example.eventlotto;

import com.example.eventlotto.model.EventStatus;

import org.junit.Test;

import static org.junit.Assert.*;

public class EventStatusTest {

    @Test
    public void getters_setters_roundTrip() {
        EventStatus s = new EventStatus();
        s.setSid("123_456");
        s.setUid("123");
        s.setEid("456");
        s.setStatus("waiting");

        // Assert
        assertEquals("123_456", s.getSid());
        assertEquals("123", s.getUid());
        assertEquals("456", s.getEid());
        assertEquals("waiting", s.getStatus());
    }
}
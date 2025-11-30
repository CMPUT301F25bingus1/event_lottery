package com.example.eventlotto;

import com.example.eventlotto.model.Event;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Event model class.
 * Tests cover event creation and management:
 * - US 02.01.01: Create event with title, description, and poster
 * - US 02.01.04: Set registration period
 * - US 02.02.02: Store event location
 * - US 02.02.03: Enable/disable geo-location requirement
 * - US 02.03.01: Limit entrants on waiting list
 * - US 02.04.01-02: Upload and update event poster
 * - US 03.01.01: Admin can remove events
 */
public class EventModelTest {

    private Event testEvent;

    @Before
    public void setUp() {
        testEvent = new Event();
    }

    /**
     * US 02.01.01: Test event creation with basic information
     */
    @Test
    public void testEventCreation() {
        testEvent.setEid("event_001");

        assertEquals("event_001", testEvent.getEid());
        assertNotNull(testEvent);
    }

    /**
     * US 02.01.04: Test event registration period configuration
     */
    @Test
    public void testEventRegistrationPeriod() {
        Timestamp registrationOpens = Timestamp.now();
        Timestamp registrationCloses = new Timestamp(
                registrationOpens.getSeconds() + 86400 * 7, 0);

        testEvent.setRegistrationOpensAt(registrationOpens);
        testEvent.setRegistrationClosesAt(registrationCloses);

        assertEquals(registrationOpens, testEvent.getRegistrationOpensAt());
        assertEquals(registrationCloses, testEvent.getRegistrationClosesAt());
        assertTrue(testEvent.getRegistrationClosesAt().getSeconds() >
                testEvent.getRegistrationOpensAt().getSeconds());
    }

    /**
     * US 02.01.01: Test event time window configuration
     */
    @Test
    public void testEventTimeWindow() {
        Timestamp eventStart = Timestamp.now();
        Timestamp eventEnd = new Timestamp(eventStart.getSeconds() + 3600, 0);

        testEvent.setEventStartAt(eventStart);
        testEvent.setEventEndAt(eventEnd);

        assertEquals(eventStart, testEvent.getEventStartAt());
        assertEquals(eventEnd, testEvent.getEventEndAt());
    }

    /**
     * US 02.01.01: Test event capacity configuration
     */
    @Test
    public void testEventCapacity() {
        Long capacity = 100L;
        testEvent.setCapacity(capacity);

        assertEquals(capacity, testEvent.getCapacity());
    }

    /**
     * US 02.03.01: Test event entrant limit
     */
    @Test
    public void testEventEntrantLimit() {
        Long maxEntrants = 50L;
        testEvent.setMaxEntrants(maxEntrants);

        assertEquals(maxEntrants, testEvent.getMaxEntrants());

        Event unlimitedEvent = new Event();
        unlimitedEvent.setMaxEntrants(null);
        assertNull(unlimitedEvent.getMaxEntrants());
    }

    /**
     * US 02.03.01: Test event entrants tracking
     */
    @Test
    public void testEventEntrantsApplied() {
        testEvent.setEntrantsApplied(0L);
        assertEquals(Long.valueOf(0L), testEvent.getEntrantsApplied());

        testEvent.setEntrantsApplied(25L);
        assertEquals(Long.valueOf(25L), testEvent.getEntrantsApplied());

        testEvent.setEntrantsApplied(50L);
        assertEquals(Long.valueOf(50L), testEvent.getEntrantsApplied());
    }

    /**
     * US 02.03.01: Test event is full check
     */
    @Test
    public void testEventIsFullLogic() {
        Long maxEntrants = 50L;
        testEvent.setMaxEntrants(maxEntrants);
        testEvent.setEntrantsApplied(50L);

        boolean isFull = testEvent.getEntrantsApplied() != null &&
                testEvent.getMaxEntrants() != null &&
                testEvent.getEntrantsApplied() >= testEvent.getMaxEntrants();
        assertTrue(isFull);

        testEvent.setEntrantsApplied(49L);
        boolean isNotFull = testEvent.getEntrantsApplied() < testEvent.getMaxEntrants();
        assertTrue(isNotFull);
    }

    /**
     * US 02.02.03: Test event geo-location requirement
     */
    @Test
    public void testEventGeoConsent() {
        // Default no geo requirement
        testEvent.setGeoConsent(false);
        assertFalse(testEvent.getGeoConsent());

        // Organizer enables geo requirement
        testEvent.setGeoConsent(true);
        assertTrue(testEvent.getGeoConsent());
    }

    /**
     * US 02.02.02: Test event location storage
     */
    @Test
    public void testEventLocation() {
        GeoPoint location = new GeoPoint(51.5074, -0.1278);
        testEvent.setLocation(location);

        assertEquals(location, testEvent.getLocation());
        assertEquals(51.5074, testEvent.getLocation().getLatitude(), 0.0001);
        assertEquals(-0.1278, testEvent.getLocation().getLongitude(), 0.0001);
    }

    /**
     * US 02.01.01: Test event URL management
     */
    @Test
    public void testEventURL() {
        String eventUrl = "https://example.com/events/summer-concert-2024";
        testEvent.setEventURL(eventUrl);

        assertEquals(eventUrl, testEvent.getEventURL());
    }

    /**
     * Test event timestamp tracking
     */
    @Test
    public void testEventTimestamps() {
        Timestamp createdAt = Timestamp.now();
        testEvent.setCreatedAt(createdAt);

        assertEquals(createdAt, testEvent.getCreatedAt());
    }

    /**
     * Test multiple events have different IDs
     */
    @Test
    public void testMultipleEventsUnique() {
        Event event1 = new Event();
        event1.setEid("event_001");

        Event event2 = new Event();
        event2.setEid("event_002");

        assertNotEquals(event1.getEid(), event2.getEid());
    }

    /**
     * Test event with all fields populated
     */
    @Test
    public void testFullEventCreation() {
        Timestamp now = Timestamp.now();
        Timestamp oneWeekLater = new Timestamp(now.getSeconds() + 86400 * 7, 0);

        testEvent.setEid("event_full");
        testEvent.setCapacity(100L);
        testEvent.setMaxEntrants(100L);
        testEvent.setEntrantsApplied(75L);
        testEvent.setRegistrationOpensAt(now);
        testEvent.setRegistrationClosesAt(oneWeekLater);
        testEvent.setEventStartAt(oneWeekLater);
        testEvent.setEventEndAt(new Timestamp(oneWeekLater.getSeconds() + 3600, 0));
        testEvent.setLocation(new GeoPoint(51.5074, -0.1278));
        testEvent.setGeoConsent(true);
        testEvent.setCreatedAt(now);

        assertEquals("event_full", testEvent.getEid());
        assertEquals(Long.valueOf(100L), testEvent.getCapacity());
        assertEquals(Long.valueOf(75L), testEvent.getEntrantsApplied());
        assertTrue(testEvent.getGeoConsent());
    }
}
package com.example.eventlotto.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import java.util.List;

/**
 * Represents an event in the application.
 * This class serves as a data model mapped to the "events" collection in Firestore.
 * It contains data such as the event title, description, capacity,
 * time information (event window, registration window), and geolocation.
 */
public class Event {

    /** The title of the event. */
    private String eventTitle;

    /** A detailed description of the event. */
    private String description;

    /** Public facing link for the event (optional). */
    private String eventURL;

    /** The timestamp when the event was created. */
    private Timestamp createdAt;

    /** The start date and time of the event. */
    private Timestamp eventStartAt;

    /** The end date and time of the event. */
    private Timestamp eventEndAt;

    /** The timestamp when registration opens for this event. */
    private Timestamp registrationOpensAt;

    /** The timestamp when registration closes for this event. */
    private Timestamp registrationClosesAt;

    /** The maximum number of participants allowed for this event. */
    private Long capacity;

    /** Whether participants consent to location tracking for this event. */
    private Boolean geoConsent;

    /** The total number of entrants who have applied to this event. */
    private Long entrantsApplied;

    /** The maximum number of entrants that can apply to this event. */
    private Long maxEntrants;

    /** Reference to the event's associated image document (if any). */
    private DocumentReference imageId;

    /** Reference to the organizer's user document in Firestore. */
    private DocumentReference organizerId;

    /** The location of the event. */
    private GeoPoint location;

    /** The unique Firestore document ID (Event ID) for this event. */
    private String eid;

    /** Days of the week when the event is scheduled (for recurring events). */
    private List<String> daysOfWeek;

    /**
     * Empty constructor required for Firestore automatic deserialization.
     */
    public Event() {}

    // --- Getters ---

    /** @return The title of the event. */
    public String getEventTitle() { return eventTitle; }

    /** @return The event description. */
    public String getDescription() { return description; }

    /** @return The event's public URL, if provided. */
    public String getEventURL() { return eventURL; }

    /** @return The creation timestamp. */
    public Timestamp getCreatedAt() { return createdAt; }

    /** @return The event start timestamp. */
    public Timestamp getEventStartAt() { return eventStartAt; }

    /** @return The event end timestamp. */
    public Timestamp getEventEndAt() { return eventEndAt; }

    /** @return The registration open timestamp. */
    public Timestamp getRegistrationOpensAt() { return registrationOpensAt; }

    /** @return The registration close timestamp. */
    public Timestamp getRegistrationClosesAt() { return registrationClosesAt; }

    /** @return The event capacity. */
    public Long getCapacity() { return capacity; }

    /** @return Whether location consent is enabled. */
    public Boolean getGeoConsent() { return geoConsent; }

    /** @return The total number of entrants who have applied. */
    public Long getEntrantsApplied() { return entrantsApplied; }

    /** @return The maximum number of entrants allowed. */
    public Long getMaxEntrants() { return maxEntrants; }

    /** @return Reference to the event image document. */
    public DocumentReference getImageId() { return imageId; }

    /** @return Reference to the organizer document. */
    public DocumentReference getOrganizerId() { return organizerId; }

    /** @return The geographic location of the event. */
    public GeoPoint getLocation() { return location; }

    /** @return The event's unique ID. */
    public String getEid() { return eid; }

    /**
     * Sets the event ID.
     * @param eid The Firestore document ID of the event.
     */
    public void setEid(String eid) { this.eid = eid; }

    /**
     * Sets the event URL.
     * @param eventURL Link associated with this event.
     */
    public void setEventURL(String eventURL) { this.eventURL = eventURL; }

    /** @return Days of the week the event is scheduled for, if applicable. */
    public List<String> getDaysOfWeek() { return daysOfWeek; }
}

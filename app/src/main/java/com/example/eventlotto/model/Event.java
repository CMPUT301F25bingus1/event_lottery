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

    /**
     * The title of the event.
     */
    private String eventTitle;

    /**
     * A detailed description of the event.
     */
    private String description;

    /**
     * Public facing link for the event (optional).
     */
    private String eventURL;

    /**
     * The timestamp when the event was created.
     */
    private Timestamp createdAt;

    /**
     * The start date and time of the event.
     */
    private Timestamp eventStartAt;

    /**
     * The end date and time of the event.
     */
    private Timestamp eventEndAt;

    /**
     * The timestamp when registration opens for this event.
     */
    private Timestamp registrationOpensAt;

    /**
     * The timestamp when registration closes for this event.
     */
    private Timestamp registrationClosesAt;

    /**
     * The maximum number of participants allowed for this event.
     */
    private Long capacity;

    /**
     * Whether participants consent to location tracking for this event.
     */
    private Boolean geoConsent;

    /**
     * The total number of entrants who have applied to this event.
     */
    private Long entrantsApplied;

    /**
     * The maximum number of entrants that can apply to this event.
     */
    private Long maxEntrants;

    /**
     * Reference to the event's associated image document (if any).
     */
    private DocumentReference imageId;

    /**
     * Reference to the organizer's user document in Firestore.
     */
    private DocumentReference organizerId;

    /**
     * The location of the event.
     */
    private GeoPoint location;

    /**
     * The unique Firestore document ID (Event ID) for this event.
     */
    private String eid;

    /** Days of the week when the event is scheduled (for recurring events). */
    private List<String> daysOfWeek;
    private List<String> tags;

    /**
     * Empty constructor required for Firestore automatic deserialization.
     */
    public Event() {
    }

    // --- Getters ---

    /**
     * @return The title of the event.
     */
    public String getEventTitle() {
        return eventTitle;
    }

    /**
     * @return The event description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The event's public URL, if provided.
     */
    public String getEventURL() {
        return eventURL;
    }

    /**
     * @return The creation timestamp.
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * @return The event start timestamp.
     */
    public Timestamp getEventStartAt() {
        return eventStartAt;
    }

    /**
     * @return The event end timestamp.
     */
    public Timestamp getEventEndAt() {
        return eventEndAt;
    }

    /**
     * @return The registration open timestamp.
     */
    public Timestamp getRegistrationOpensAt() {
        return registrationOpensAt;
    }

    /**
     * @return The registration close timestamp.
     */
    public Timestamp getRegistrationClosesAt() {
        return registrationClosesAt;
    }

    /**
     * @return The event capacity.
     */
    public Long getCapacity() {
        return capacity;
    }

    /**
     * @return Whether location consent is enabled.
     */
    public Boolean getGeoConsent() {
        return geoConsent;
    }

    /**
     * @return The total number of entrants who have applied.
     */
    public Long getEntrantsApplied() {
        return entrantsApplied;
    }

    /**
     * @return The maximum number of entrants allowed.
     */
    public Long getMaxEntrants() {
        return maxEntrants;
    }

    /**
     * @return Reference to the event image document.
     */
    public DocumentReference getImageId() {
        return imageId;
    }

    /**
     * @return Reference to the organizer document.
     */
    public DocumentReference getOrganizerId() {
        return organizerId;
    }

    /**
     * @return The geographic location of the event.
     */
    public GeoPoint getLocation() {
        return location;
    }

    /**
     * @return The event's unique ID.
     */
    public String getEid() {
        return eid;
    }

    /**
     * Sets the event ID. @param eid The Firestore document ID of the event.
     */
    public void setEid(String eid) {
        this.eid = eid;
    }

    /**
     * Sets the event URL. @param eventURL Link associated with this event.
     */
    public void setEventURL(String eventURL) {
        this.eventURL = eventURL;
    }

    public List<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    /**
     * Sets the event title. @param eventTitle The name of the event.
     */
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    /**
     * Sets the event description. @param description Detailed info about the event.
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Sets the event creation timestamp. @param createdAt When the event was created.
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Sets the event start time. @param eventStartAt When the event begins.
     */
    public void setEventStartAt(Timestamp eventStartAt) {
        this.eventStartAt = eventStartAt;
    }

    /**
     * Sets the event end time. @param eventEndAt When the event concludes.
     */
    public void setEventEndAt(Timestamp eventEndAt) {
        this.eventEndAt = eventEndAt;
    }

    /**
     * Sets registration open time. @param registrationOpensAt When entrants can register.
     */
    public void setRegistrationOpensAt(Timestamp registrationOpensAt) {
        this.registrationOpensAt = registrationOpensAt;
    }

    /**
     * Sets registration close time. @param registrationClosesAt When registration ends.
     */
    public void setRegistrationClosesAt(Timestamp registrationClosesAt) {
        this.registrationClosesAt = registrationClosesAt;
    }

    /**
     * Sets event capacity. @param capacity Maximum number of participants.
     */
    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    /**
     * Sets geo-location consent requirement. @param geoConsent True if location tracking required.
     */
    public void setGeoConsent(Boolean geoConsent) {
        this.geoConsent = geoConsent;
    }

    /**
     * Sets applied entrants count. @param entrantsApplied Total number of applicants.
     */
    public void setEntrantsApplied(Long entrantsApplied) {
        this.entrantsApplied = entrantsApplied;
    }

    /**
     * Sets max entrants limit. @param maxEntrants Maximum waiting list size, or null for unlimited.
     */
    public void setMaxEntrants(Long maxEntrants) {
        this.maxEntrants = maxEntrants;
    }

    /**
     * Sets event image reference. @param imageId Reference to the event poster document.
     */
    public void setImageId(DocumentReference imageId) {
        this.imageId = imageId;
    }

    /**
     * Sets organizer reference. @param organizerId Reference to the organizer's user document.
     */
    public void setOrganizerId(DocumentReference organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Sets event location. @param location Geographic coordinates of the event.
     */
    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    /**
     * Sets event tags. @param tags List of categories associated with the event.
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
  
   /* Sets the event ID.
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

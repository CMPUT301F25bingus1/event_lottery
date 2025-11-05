package com.example.eventlotto.events;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.Timestamp;

public class Event {
    private String eventTitle;
    private String description;
    private Timestamp createdAt;
    private Timestamp eventStartAt;
    private Timestamp eventEndAt;
    private Timestamp registrationOpensAt;
    private Timestamp registrationClosesAt;
    private Long capacity;
    private Boolean geoConsent;
    private Boolean notifyWhenNotSelected;
    private DocumentReference imageId;
    private DocumentReference organizerId;
    private GeoPoint location;
    private String eid;

    public Event() {} // Firestore requires empty constructor

    // Getters
    public String getEventTitle() { return eventTitle; }
    public String getDescription() { return description; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getEventStartAt() { return eventStartAt; }
    public Timestamp getEventEndAt() { return eventEndAt; }
    public Timestamp getRegistrationOpensAt() { return registrationOpensAt; }
    public Timestamp getRegistrationClosesAt() { return registrationClosesAt; }
    public Long getCapacity() { return capacity; }
    public Boolean getGeoConsent() { return geoConsent; }
    public Boolean getNotifyWhenNotSelected() { return notifyWhenNotSelected; }
    public DocumentReference getImageId() { return imageId; }
    public DocumentReference getOrganizerId() { return organizerId; }
    public GeoPoint getLocation() { return location; }

    public String getEid() { return eid; }
    public void setEid(String eid) { this.eid = eid; }
}

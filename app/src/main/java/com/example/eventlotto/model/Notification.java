package com.example.eventlotto.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Represents a notification entry sent to a user about an event.
 * Stored in Firestore to track message content and send timestamps.
 */
@IgnoreExtraProperties
public class Notification {
    /** Notification document ID. */
    private String nid;
    /** User ID the notification targets. */
    private String uid;
    /** Event ID this notification references. */
    private String eid;
    /** Timestamp of the most recent send. */
    private Timestamp lastSentAt;
    /** Timestamp of initial creation. */
    private Timestamp createdAt;
    /** Body text delivered to the user. */
    private String message;

    /** @return Notification body text. */
    public String getMessage() { return message; }
    /** @param message Notification body text. */
    public void setMessage(String message) { this.message = message; }

    public Notification() {}

    /** @return Notification document ID. */
    public String getNid() { return nid; }
    /** @param nid Notification document ID. */
    public void setNid(String nid) { this.nid = nid; }

    /** @return User ID that receives this notification. */
    public String getUid() { return uid; }
    /** @param uid User ID that receives this notification. */
    public void setUid(String uid) { this.uid = uid; }

    /** @return Event ID that triggered this notification. */
    public String getEid() { return eid; }
    /** @param eid Event ID that triggered this notification. */
    public void setEid(String eid) { this.eid = eid; }

    /** @return Timestamp when the notification was last sent. */
    public Timestamp getLastSentAt() { return lastSentAt; }
    /** @param lastSentAt Timestamp when the notification was last sent. */
    public void setLastSentAt(Timestamp lastSentAt) { this.lastSentAt = lastSentAt; }

    /** @return Timestamp when the notification record was created. */
    public Timestamp getCreatedAt() { return createdAt; }
    /** @param createdAt Timestamp when the notification record was created. */
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

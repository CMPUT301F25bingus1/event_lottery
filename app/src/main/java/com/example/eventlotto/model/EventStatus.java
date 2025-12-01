package com.example.eventlotto.model;

/**
 * Tracks a user's enrollment state for a given event.
 * Stored in Firestore to record whether a user is waiting, selected,
 * signed up, cancelled, or not chosen for an event.
 */
public class EventStatus {
    /** Status document ID in Firestore. */
    private String sid;
    /** Event ID the status is associated with. */
    private String eid;
    /** User/device ID the status is associated with. */
    private String uid;
    /**
     * Current enrollment status. Expected values:
     * waiting, selected, signed up, cancelled, not chosen.
     */
    private String status;

    public EventStatus() {}

    /** @return Status document ID. */
    public String getSid() { return sid; }
    /** @param sid Status document ID. */
    public void setSid(String sid) { this.sid = sid; }

    /** @return Event ID this status belongs to. */
    public String getEid() { return eid; }
    /** @param eid Event ID this status belongs to. */
    public void setEid(String eid) { this.eid = eid; }

    /** @return User/device ID this status belongs to. */
    public String getUid() { return uid; }
    /** @param uid User/device ID this status belongs to. */
    public void setUid(String uid) { this.uid = uid; }

    /** @return Current enrollment status value. */
    public String getStatus() { return status; }
    /** @param status Enrollment status (waiting, selected, signed up, cancelled, not chosen). */
    public void setStatus(String status) { this.status = status; }
}

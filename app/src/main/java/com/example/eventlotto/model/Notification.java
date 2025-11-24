package com.example.eventlotto.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Notification {
    private String nid;
    private String uid;
    private String eid;
    private Timestamp lastSentAt;
    private Timestamp createdAt;
    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Notification() {}

    public String getNid() { return nid; }
    public void setNid(String nid) { this.nid = nid; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEid() { return eid; }
    public void setEid(String eid) { this.eid = eid; }

    public Timestamp getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(Timestamp lastSentAt) { this.lastSentAt = lastSentAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}


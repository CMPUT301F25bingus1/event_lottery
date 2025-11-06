package com.example.eventlotto.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class EventStatus {
    private String sid; // status document id
    private String eid; // event id
    private String uid; // user id (device id for now)
    // one of: waiting, selected, signed up, cancelled, not chosen
    private String status;

    public EventStatus() {}

    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }

    public String getEid() { return eid; }
    public void setEid(String eid) { this.eid = eid; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}


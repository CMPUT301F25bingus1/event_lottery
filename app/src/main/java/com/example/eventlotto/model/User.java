package com.example.eventlotto.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {


    private String uid;
    private String deviceId;
    private String role;                    // entrant, organizer, admin
    private String fullName;
    private String email;
    private String phone;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
    private String deviceId;
=======
    private String avatarUrl;
>>>>>>> Stashed changes
=======
    private String avatarUrl;
>>>>>>> Stashed changes
=======
    private String avatarUrl;
>>>>>>> Stashed changes
=======
    private String avatarUrl;
>>>>>>> Stashed changes
    private Boolean notifyWhenNotSelected;
    private Boolean geoConsent;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;



    public User() {}


    public User(String deviceId, String fullName, String email, String phone) {
        this.uid = deviceId;
        this.deviceId = deviceId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = "entrant";
        this.notifyWhenNotSelected = true;
        this.geoConsent = false;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
        this.deletedAt = null;
        this.avatarUrl = null;
    }


    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }


    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Boolean getNotifyWhenNotSelected() { return notifyWhenNotSelected; }
    public void setNotifyWhenNotSelected(Boolean notifyWhenNotSelected) { this.notifyWhenNotSelected = notifyWhenNotSelected; }

    public Boolean getGeoConsent() { return geoConsent; }
    public void setGeoConsent(Boolean geoConsent) { this.geoConsent = geoConsent; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }



    public boolean wantsNotifications() {
        return notifyWhenNotSelected != null && notifyWhenNotSelected;
    }


    public void markDeleted() {
        this.deletedAt = Timestamp.now();
    }


    public void touch() {
        this.updatedAt = Timestamp.now();
    }
}

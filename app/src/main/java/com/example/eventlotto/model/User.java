package com.example.eventlotto.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Represents an application user profile stored in Firestore.
 * Includes contact details, role, notification preferences, and audit timestamps.
 */
@IgnoreExtraProperties
public class User {

    /** Unique user identifier (device ID for entrants). */
    private String uid;
    /** User role: entrant, organizer, or admin. */
    private String role; // entrant, organizer, admin
    /** Full display name of the user. */
    private String fullName;
    /** Contact email address. */
    private String email;
    /** Contact phone number. */
    private String phone;
    /** URL of the user's avatar image. */
    private String avatarUrl;
    /** Device identifier (used as UID for entrants). */
    private String deviceId;
    /** Whether to notify the user when they are not selected. */
    private Boolean notifyWhenNotSelected;
    /** Whether the user has granted location tracking consent. */
    private Boolean geoConsent;
    /** Timestamp when the profile was created. */
    private Timestamp createdAt;
    /** Timestamp when the profile was last updated. */
    private Timestamp updatedAt;
    /** Timestamp when the profile was soft-deleted; null if active. */
    private Timestamp deletedAt;

    public User() {}

    // Main constructor using deviceId
    /**
     * Creates a user with entrant defaults using the device ID as UID.
     * @param deviceId Device identifier (also used as UID).
     * @param fullName User's full name.
     * @param email User email.
     * @param phone User phone.
     */
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

    /** @return Unique user identifier. */
    public String getUid() { return uid; }
    /** @param uid Unique user identifier. */
    public void setUid(String uid) { this.uid = uid; }

    /** @return User role. */
    public String getRole() { return role; }
    /** @param role User role (entrant, organizer, admin). */
    public void setRole(String role) { this.role = role; }

    /** @return Full display name. */
    public String getFullName() { return fullName; }
    /** @param fullName Full display name. */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /** @return Email address. */
    public String getEmail() { return email; }
    /** @param email Email address. */
    public void setEmail(String email) { this.email = email; }

    /** @return Phone number. */
    public String getPhone() { return phone; }
    /** @param phone Phone number. */
    public void setPhone(String phone) { this.phone = phone; }

    /** @return Avatar image URL. */
    public String getAvatarUrl() { return avatarUrl; }
    /** @param avatarUrl Avatar image URL. */
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    /** @return Device identifier. */
    public String getDeviceId() { return deviceId; }
    /** @param deviceId Device identifier. */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    /** @return Whether the user wants notifications when not selected. */
    public Boolean getNotifyWhenNotSelected() { return notifyWhenNotSelected; }
    /** @param notifyWhenNotSelected Whether to notify when not selected. */
    public void setNotifyWhenNotSelected(Boolean notifyWhenNotSelected) { this.notifyWhenNotSelected = notifyWhenNotSelected; }

    /** @return Location tracking consent flag. */
    public Boolean getGeoConsent() { return geoConsent; }
    /** @param geoConsent Location tracking consent flag. */
    public void setGeoConsent(Boolean geoConsent) { this.geoConsent = geoConsent; }

    /** @return Creation timestamp. */
    public Timestamp getCreatedAt() { return createdAt; }
    /** @param createdAt Creation timestamp. */
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /** @return Last update timestamp. */
    public Timestamp getUpdatedAt() { return updatedAt; }
    /** @param updatedAt Last update timestamp. */
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    /** @return Soft-delete timestamp, or null if active. */
    public Timestamp getDeletedAt() { return deletedAt; }
    /** @param deletedAt Soft-delete timestamp, or null if active. */
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }

    /** Updates the {@code updatedAt} timestamp to now. */
    public void touch() { this.updatedAt = Timestamp.now(); }
}

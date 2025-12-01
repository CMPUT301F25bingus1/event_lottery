package com.example.eventlotto.model;

import androidx.annotation.Nullable;

/**
 * Represents an event the user has chosen to follow.
 * Stores both local image resources and optional remote image URLs,
 * along with organizer messages and notification preferences.
 */
public class FollowedEvent {
    /** Unique identifier for the followed event (event ID). */
    private final String id;
    /** Display name of the event. */
    private final String name;
    /** Short description shown in the follower's list. */
    private final String description;
    /** Local drawable resource used as a placeholder or default image. */
    private final int imageResId;
    /** Optional remote image URL for the event cover. */
    @Nullable private final String imageUrl;
    /** Whether the follower wants push notifications for this event. */
    private boolean notificationsEnabled;
    /** Optional organizer message shown to the follower. */
    private String message; // optional organizer message

    // Constructor with message
    /**
     * Creates a followed event entry.
     * @param id Event ID.
     * @param name Display name.
     * @param description Short description of the event.
     * @param imageResId Local drawable resource for fallback imagery.
     * @param imageUrl Remote image URL (nullable).
     * @param notificationsEnabled Whether notifications are enabled for this entry.
     * @param message Optional organizer message to display.
     */
    public FollowedEvent(String id, String name, String description, int imageResId,
                         @Nullable String imageUrl, boolean notificationsEnabled, String message) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageResId = imageResId;
        this.imageUrl = imageUrl;
        this.notificationsEnabled = notificationsEnabled;
        this.message = message;
    }

    // Existing getter/setters
    /** @return Organizer message or null/empty if none. */
    public String getMessage() { return message; }
    /** @param message Organizer message to show to the follower. */
    public void setMessage(String message) { this.message = message; }

    /** @return Event ID. */
    public String getId() { return id; }
    /** @return Event display name. */
    public String getName() { return name; }
    /** @return Event description. */
    public String getDescription() { return description; }
    /** @return Local drawable resource used for display. */
    public int getImageResId() { return imageResId; }
    /** @return Remote image URL if available. */
    @Nullable public String getImageUrl() { return imageUrl; }
    /** @return Whether notifications are enabled for this followed event. */
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    /** @param enabled Set notification preference for this followed event. */
    public void setNotificationsEnabled(boolean enabled) { this.notificationsEnabled = enabled; }
}

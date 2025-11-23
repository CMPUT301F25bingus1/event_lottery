package com.example.eventlotto.model;

public class FollowedEvent {
    private final String id;
    private final String name;
    private final String description;
    private final int imageResId;
    private boolean notificationsEnabled;
    private String message; // optional organizer message

    // Constructor with message
    public FollowedEvent(String id, String name, String description, int imageResId,
                         boolean notificationsEnabled, String message) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageResId = imageResId;
        this.notificationsEnabled = notificationsEnabled;
        this.message = message;
    }

    // Existing getter/setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean enabled) { this.notificationsEnabled = enabled; }
}

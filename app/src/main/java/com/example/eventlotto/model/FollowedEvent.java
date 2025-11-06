package com.example.eventlotto.functions.notifications;

public class FollowedEvent {


    private final String id;
    private final String name;
    private final String description;

    private final int imageResId;
    private boolean notificationsEnabled;


    public FollowedEvent(String id, String name, String description, int imageResId, boolean notificationsEnabled, boolean closed) {
        this.id = id;
        this.name = name;
        this.description = description;

        this.imageResId = imageResId;
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    public int getImageResId() { return imageResId; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean enabled) { this.notificationsEnabled = enabled; }
}


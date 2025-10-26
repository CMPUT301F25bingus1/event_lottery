package com.example.eventlotto.notifications;

public class FollowedEvent {
    public enum Status { ACCEPTED, WAITING, NOT_CHOSEN }

    private final String id;
    private final String name;
    private final String description;
    private final Status status;
    private final int imageResId;
    private boolean notificationsEnabled;
    private final boolean closed;

    public FollowedEvent(String id, String name, String description, Status status, int imageResId, boolean notificationsEnabled, boolean closed) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.imageResId = imageResId;
        this.notificationsEnabled = notificationsEnabled;
        this.closed = closed;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Status getStatus() { return status; }
    public int getImageResId() { return imageResId; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean enabled) { this.notificationsEnabled = enabled; }
    public boolean isClosed() { return closed; }
}


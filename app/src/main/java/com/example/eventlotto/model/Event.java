package com.example.eventlotto.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Event {
    private String eid;
    private String uid;
    private String title;
    private String description;
    private String venueName;
    private String venueAddress;
    private Double lat;
    private Double lng;
    private Timestamp registrationOpensAt;
    private Timestamp registrationClosesAt;
    private Long capacity;
    private Long waitlistLimit;
    private String posterUrl;
    private String qrToken;
    private String visibility;
    private Boolean geolocationRequired;
    private String status;
    private String lotteryStatus;
    private Long selectedQuota;
    private Long waitingCount;
    private Long selectedCount;
    private Long enrolledCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public Event() {}

    public String getEid() { return eid; }
    public void setEid(String eid) { this.eid = eid; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Timestamp getRegistrationOpensAt() { return registrationOpensAt; }
    public void setRegistrationOpensAt(Timestamp registrationOpensAt) { this.registrationOpensAt = registrationOpensAt; }

    public Timestamp getRegistrationClosesAt() { return registrationClosesAt; }
    public void setRegistrationClosesAt(Timestamp registrationClosesAt) { this.registrationClosesAt = registrationClosesAt; }

    public Long getCapacity() { return capacity; }
    public void setCapacity(Long capacity) { this.capacity = capacity; }

    public Long getWaitlistLimit() { return waitlistLimit; }
    public void setWaitlistLimit(Long waitlistLimit) { this.waitlistLimit = waitlistLimit; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public Boolean getGeolocationRequired() { return geolocationRequired; }
    public void setGeolocationRequired(Boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLotteryStatus() { return lotteryStatus; }
    public void setLotteryStatus(String lotteryStatus) { this.lotteryStatus = lotteryStatus; }

    public Long getSelectedQuota() { return selectedQuota; }
    public void setSelectedQuota(Long selectedQuota) { this.selectedQuota = selectedQuota; }

    public Long getWaitingCount() { return waitingCount; }
    public void setWaitingCount(Long waitingCount) { this.waitingCount = waitingCount; }

    public Long getSelectedCount() { return selectedCount; }
    public void setSelectedCount(Long selectedCount) { this.selectedCount = selectedCount; }

    public Long getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(Long enrolledCount) { this.enrolledCount = enrolledCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }
}


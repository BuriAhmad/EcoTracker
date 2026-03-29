package com.ecotrack.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.HashMap;
import java.util.Map;

/**
 * A single social-feed entry posted when a user logs an activity.
 * Stored at: socialFeed/{feedItemId}
 */
public class FeedItem {

    @DocumentId
    private String feedItemId;

    private String userId;          // Actual user ID (always stored for ownership)
    private String displayName;     // "Anonymous Hero" if anonymous
    private String department;
    private String avatarUrl;
    private boolean anonymous;

    private String activityType;    // Matches Constants.ACTIVITY_* keys
    private String activityDescription; // Human-readable, e.g. "Biked 5.2 km"
    private double quantity;
    private String unit;
    private double co2Saved;
    private int pointsEarned;

    /** Reaction counts — keys: "🌱", "💚", "🎉", "👏" */
    private Map<String, Long> reactions;

    private Timestamp timestamp;

    /** Required no-arg constructor for Firestore. */
    public FeedItem() {
        this.reactions = new HashMap<>();
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getFeedItemId() { return feedItemId; }
    public String getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getDepartment() { return department; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isAnonymous() { return anonymous; }
    public String getActivityType() { return activityType; }
    public String getActivityDescription() { return activityDescription; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public double getCo2Saved() { return co2Saved; }
    public int getPointsEarned() { return pointsEarned; }
    public Map<String, Long> getReactions() { return reactions; }
    public Timestamp getTimestamp() { return timestamp; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setFeedItemId(String feedItemId) { this.feedItemId = feedItemId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDepartment(String department) { this.department = department; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public void setActivityDescription(String activityDescription) { this.activityDescription = activityDescription; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setCo2Saved(double co2Saved) { this.co2Saved = co2Saved; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }
    public void setReactions(Map<String, Long> reactions) { this.reactions = reactions; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}

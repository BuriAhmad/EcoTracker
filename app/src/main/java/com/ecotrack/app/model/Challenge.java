package com.ecotrack.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * A time-boxed community challenge. Users join and log matching activities
 * to make progress towards the goal.
 * Stored at: challenges/{challengeId}
 */
public class Challenge {

    @DocumentId
    private String challengeId;

    private String title;
    private String description;
    private String activityType;        // Matches Constants.ACTIVITY_* keys
    private double goalQuantity;        // Target quantity (e.g. 50 km)
    private String unit;                // "km", "kg", etc.
    private Timestamp startDate;
    private Timestamp endDate;
    private int pointsReward;
    private String createdBy;           // userId of admin who created it
    private int participantCount;
    private String status;              // "active", "completed", "draft"

    /** Required no-arg constructor for Firestore. */
    public Challenge() {
        this.participantCount = 0;
        this.status = "active";
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getChallengeId() { return challengeId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getActivityType() { return activityType; }
    public double getGoalQuantity() { return goalQuantity; }
    public String getUnit() { return unit; }
    public Timestamp getStartDate() { return startDate; }
    public Timestamp getEndDate() { return endDate; }
    public int getPointsReward() { return pointsReward; }
    public String getCreatedBy() { return createdBy; }
    public int getParticipantCount() { return participantCount; }
    public String getStatus() { return status; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setChallengeId(String challengeId) { this.challengeId = challengeId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public void setGoalQuantity(double goalQuantity) { this.goalQuantity = goalQuantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }
    public void setPointsReward(int pointsReward) { this.pointsReward = pointsReward; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }
    public void setStatus(String status) { this.status = status; }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * @return days remaining until endDate, or 0 if ended.
     */
    public long getDaysRemaining() {
        if (endDate == null) return 0;
        long diff = endDate.toDate().getTime() - System.currentTimeMillis();
        return Math.max(0, diff / (1000L * 60 * 60 * 24));
    }

    public boolean isActive() {
        return "active".equals(status);
    }
}

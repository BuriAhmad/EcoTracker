package com.ecotrack.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * A user's participation in a challenge.
 * Stored at: challenges/{challengeId}/participants/{userId}
 */
public class ChallengeParticipant {

    @DocumentId
    private String userId;

    private String displayName;
    private String avatarUrl;
    private double currentProgress;     // Running total (e.g. 12.5 km so far)
    private double goalQuantity;        // Copied from the parent Challenge
    private boolean completed;

    @ServerTimestamp
    private Timestamp joinedAt;

    /** Required no-arg constructor for Firestore. */
    public ChallengeParticipant() {
        this.currentProgress = 0;
        this.completed = false;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public double getCurrentProgress() { return currentProgress; }
    public double getGoalQuantity() { return goalQuantity; }
    public boolean isCompleted() { return completed; }
    public Timestamp getJoinedAt() { return joinedAt; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setUserId(String userId) { this.userId = userId; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setCurrentProgress(double currentProgress) { this.currentProgress = currentProgress; }
    public void setGoalQuantity(double goalQuantity) { this.goalQuantity = goalQuantity; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setJoinedAt(Timestamp joinedAt) { this.joinedAt = joinedAt; }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * @return progress as a fraction (0.0 – 1.0).
     */
    public double getProgressFraction() {
        if (goalQuantity <= 0) return 0;
        return Math.min(1.0, currentProgress / goalQuantity);
    }
}

package com.ecotrack.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Represents a user profile.
 * Pure POJO — no business logic. Firestore serializable.
 */
public class User {

    @DocumentId
    private String userId;
    private String displayName;
    private String email;
    private String department;
    private String avatarUrl;
    private String role;           // "student" or "admin"
    private long totalPoints;
    private double ecoScore;       // 0–100
    private double totalCo2Saved;
    private double totalWaterSaved;
    private double totalWasteDiverted;
    private long totalActivitiesLogged;
    private double totalBikeKm;            // Cumulative km cycled (for bike_100km badge)
    private long totalChallengesCompleted; // Completed challenges (for challenges_5 badge)
    private int currentStreak;
    private Timestamp lastLogDate;
    @ServerTimestamp
    private Timestamp createdAt;
    private boolean anonymousOnFeed;
    private boolean showOnLeaderboard;

    /** Required no-arg constructor for Firestore deserialization. */
    public User() {
        this.role = "student";
        this.totalPoints = 0;
        this.ecoScore = 0;
        this.totalCo2Saved = 0;
        this.totalWaterSaved = 0;
        this.totalWasteDiverted = 0;
        this.totalActivitiesLogged = 0;
        this.currentStreak = 0;
        this.anonymousOnFeed = false;
        this.showOnLeaderboard = true;
    }

    /**
     * Convenience constructor for registration.
     */
    public User(String userId, String displayName, String email, String department) {
        this();
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.department = department;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRole() { return role; }
    public long getTotalPoints() { return totalPoints; }
    public double getEcoScore() { return ecoScore; }
    public double getTotalCo2Saved() { return totalCo2Saved; }
    public double getTotalWaterSaved() { return totalWaterSaved; }
    public double getTotalWasteDiverted() { return totalWasteDiverted; }
    public long getTotalActivitiesLogged() { return totalActivitiesLogged; }
    public double getTotalBikeKm() { return totalBikeKm; }
    public long getTotalChallengesCompleted() { return totalChallengesCompleted; }
    public int getCurrentStreak() { return currentStreak; }
    public Timestamp getLastLogDate() { return lastLogDate; }
    public Timestamp getCreatedAt() { return createdAt; }
    public boolean isAnonymousOnFeed() { return anonymousOnFeed; }
    public boolean isShowOnLeaderboard() { return showOnLeaderboard; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setUserId(String userId) { this.userId = userId; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setEmail(String email) { this.email = email; }
    public void setDepartment(String department) { this.department = department; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setRole(String role) { this.role = role; }
    public void setTotalPoints(long totalPoints) { this.totalPoints = totalPoints; }
    public void setEcoScore(double ecoScore) { this.ecoScore = ecoScore; }
    public void setTotalCo2Saved(double totalCo2Saved) { this.totalCo2Saved = totalCo2Saved; }
    public void setTotalWaterSaved(double totalWaterSaved) { this.totalWaterSaved = totalWaterSaved; }
    public void setTotalWasteDiverted(double totalWasteDiverted) { this.totalWasteDiverted = totalWasteDiverted; }
    public void setTotalActivitiesLogged(long totalActivitiesLogged) { this.totalActivitiesLogged = totalActivitiesLogged; }
    public void setTotalBikeKm(double totalBikeKm) { this.totalBikeKm = totalBikeKm; }
    public void setTotalChallengesCompleted(long totalChallengesCompleted) { this.totalChallengesCompleted = totalChallengesCompleted; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public void setLastLogDate(Timestamp lastLogDate) { this.lastLogDate = lastLogDate; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setAnonymousOnFeed(boolean anonymousOnFeed) { this.anonymousOnFeed = anonymousOnFeed; }
    public void setShowOnLeaderboard(boolean showOnLeaderboard) { this.showOnLeaderboard = showOnLeaderboard; }
}

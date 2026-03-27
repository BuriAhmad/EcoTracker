package com.ecotrack.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Represents a single logged eco-friendly activity.
 * Stored as subcollection under users/{userId}/activityLogs/{logId}.
 * Pure POJO — Firestore serializable.
 */
public class ActivityLog {

    @DocumentId
    private String logId;
    private String userId;
    private String activityType;
    private double quantity;
    private String unit;
    private double co2Saved;
    private double waterSaved;
    private double wasteDiverted;
    private int pointsEarned;
    private String photoProofUrl;
    private boolean verified;
    @ServerTimestamp
    private Timestamp timestamp;

    /** Required no-arg constructor for Firestore. */
    public ActivityLog() {
        this.verified = true;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getLogId() { return logId; }
    public String getUserId() { return userId; }
    public String getActivityType() { return activityType; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public double getCo2Saved() { return co2Saved; }
    public double getWaterSaved() { return waterSaved; }
    public double getWasteDiverted() { return wasteDiverted; }
    public int getPointsEarned() { return pointsEarned; }
    public String getPhotoProofUrl() { return photoProofUrl; }
    public boolean isVerified() { return verified; }
    public Timestamp getTimestamp() { return timestamp; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setLogId(String logId) { this.logId = logId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setCo2Saved(double co2Saved) { this.co2Saved = co2Saved; }
    public void setWaterSaved(double waterSaved) { this.waterSaved = waterSaved; }
    public void setWasteDiverted(double wasteDiverted) { this.wasteDiverted = wasteDiverted; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }
    public void setPhotoProofUrl(String photoProofUrl) { this.photoProofUrl = photoProofUrl; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}

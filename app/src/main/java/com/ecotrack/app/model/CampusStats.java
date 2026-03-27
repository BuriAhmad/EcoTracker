package com.ecotrack.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Campus-wide aggregate statistics.
 * Single document at campusStats/aggregate.
 * Pure POJO — Firestore serializable.
 */
public class CampusStats {

    private double totalCo2Saved;
    private double totalWaterSaved;
    private double totalWasteDiverted;
    private long totalActivitiesLogged;
    private long totalUsers;
    @ServerTimestamp
    private Timestamp lastUpdated;

    /** Required no-arg constructor for Firestore. */
    public CampusStats() {}

    // ── Getters ──────────────────────────────────────────────────────────

    public double getTotalCo2Saved() { return totalCo2Saved; }
    public double getTotalWaterSaved() { return totalWaterSaved; }
    public double getTotalWasteDiverted() { return totalWasteDiverted; }
    public long getTotalActivitiesLogged() { return totalActivitiesLogged; }
    public long getTotalUsers() { return totalUsers; }
    public Timestamp getLastUpdated() { return lastUpdated; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setTotalCo2Saved(double totalCo2Saved) { this.totalCo2Saved = totalCo2Saved; }
    public void setTotalWaterSaved(double totalWaterSaved) { this.totalWaterSaved = totalWaterSaved; }
    public void setTotalWasteDiverted(double totalWasteDiverted) { this.totalWasteDiverted = totalWasteDiverted; }
    public void setTotalActivitiesLogged(long totalActivitiesLogged) { this.totalActivitiesLogged = totalActivitiesLogged; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public void setLastUpdated(Timestamp lastUpdated) { this.lastUpdated = lastUpdated; }
}

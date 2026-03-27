package com.ecotrack.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

/**
 * Per-activity conversion rates for impact calculation.
 * Pure POJO — Firestore serializable.
 */
public class ConversionFactor {

    @DocumentId
    private String activityType;
    private double co2PerUnit;
    private double waterPerUnit;
    private double wastePerUnit;
    private int pointsPerUnit;
    private String unit;         // "km", "items", "L", "hrs", "day", "kg", "use", "meal"
    private String source;
    private Timestamp lastUpdated;

    /** Required no-arg constructor for Firestore. */
    public ConversionFactor() {}

    public ConversionFactor(String activityType, double co2PerUnit, double waterPerUnit,
                            double wastePerUnit, int pointsPerUnit, String unit) {
        this.activityType = activityType;
        this.co2PerUnit = co2PerUnit;
        this.waterPerUnit = waterPerUnit;
        this.wastePerUnit = wastePerUnit;
        this.pointsPerUnit = pointsPerUnit;
        this.unit = unit;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getActivityType() { return activityType; }
    public double getCo2PerUnit() { return co2PerUnit; }
    public double getWaterPerUnit() { return waterPerUnit; }
    public double getWastePerUnit() { return wastePerUnit; }
    public int getPointsPerUnit() { return pointsPerUnit; }
    public String getUnit() { return unit; }
    public String getSource() { return source; }
    public Timestamp getLastUpdated() { return lastUpdated; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setActivityType(String activityType) { this.activityType = activityType; }
    public void setCo2PerUnit(double co2PerUnit) { this.co2PerUnit = co2PerUnit; }
    public void setWaterPerUnit(double waterPerUnit) { this.waterPerUnit = waterPerUnit; }
    public void setWastePerUnit(double wastePerUnit) { this.wastePerUnit = wastePerUnit; }
    public void setPointsPerUnit(int pointsPerUnit) { this.pointsPerUnit = pointsPerUnit; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setSource(String source) { this.source = source; }
    public void setLastUpdated(Timestamp lastUpdated) { this.lastUpdated = lastUpdated; }
}

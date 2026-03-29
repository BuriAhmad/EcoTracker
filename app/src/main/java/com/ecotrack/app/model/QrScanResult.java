package com.ecotrack.app.model;

/**
 * Represents the result of scanning a QR code at a campus location.
 * The QR payload is a JSON object that maps to this POJO.
 *
 * Expected QR JSON format:
 * { "locationId": "...", "locationName": "...", "activityType": "...", "quantity": 1 }
 */
public class QrScanResult {

    private String locationId;
    private String locationName;
    private String activityType;
    private int quantity;
    private boolean success;

    public QrScanResult() {
        this.success = false;
        this.quantity = 1;
    }

    public QrScanResult(String locationId, String locationName,
                        String activityType, int quantity) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.activityType = activityType;
        this.quantity = quantity;
        this.success = true;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public String getActivityType() { return activityType; }
    public int getQuantity() { return quantity; }
    public boolean isSuccess() { return success; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setLocationId(String locationId) { this.locationId = locationId; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setSuccess(boolean success) { this.success = success; }
}

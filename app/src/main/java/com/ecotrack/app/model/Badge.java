package com.ecotrack.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * An earned badge instance, stored in users/{userId}/badges/{badgeId}.
 */
public class Badge {

    @DocumentId
    private String badgeId;
    private String badgeType;
    @ServerTimestamp
    private Timestamp unlockedAt;
    private boolean displayed;

    /** Required no-arg constructor for Firestore. */
    public Badge() {
        this.displayed = true;
    }

    public Badge(String badgeType) {
        this();
        this.badgeType = badgeType;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getBadgeId() { return badgeId; }
    public String getBadgeType() { return badgeType; }
    public Timestamp getUnlockedAt() { return unlockedAt; }
    public boolean isDisplayed() { return displayed; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }
    public void setUnlockedAt(Timestamp unlockedAt) { this.unlockedAt = unlockedAt; }
    public void setDisplayed(boolean displayed) { this.displayed = displayed; }
}

package com.ecotrack.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a team (club, department, or custom group).
 * Stored at: teams/{teamId}
 */
public class Team {

    @DocumentId
    private String teamId;
    private String name;
    private String type;               // "club", "department", "other"
    private String description;
    private List<String> memberIds;
    private long totalPoints;
    private int memberCount;
    private String createdBy;          // userId of creator
    private boolean isPublic;
    @ServerTimestamp
    private Timestamp createdAt;

    /** Required no-arg constructor for Firestore deserialization. */
    public Team() {
        this.memberIds = new ArrayList<>();
        this.totalPoints = 0;
        this.memberCount = 0;
        this.isPublic = true;
        this.type = "other";
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getTeamId() { return teamId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public List<String> getMemberIds() { return memberIds; }
    public long getTotalPoints() { return totalPoints; }
    public int getMemberCount() { return memberCount; }
    public String getCreatedBy() { return createdBy; }
    public boolean isPublic() { return isPublic; }
    public Timestamp getCreatedAt() { return createdAt; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setTeamId(String teamId) { this.teamId = teamId; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }
    public void setTotalPoints(long totalPoints) { this.totalPoints = totalPoints; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Returns the first letter of the team name (for avatar initials).
     */
    public String getInitial() {
        if (name == null || name.isEmpty()) return "?";
        return name.substring(0, 1).toUpperCase();
    }
}

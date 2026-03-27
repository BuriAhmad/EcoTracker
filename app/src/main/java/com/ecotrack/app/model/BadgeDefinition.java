package com.ecotrack.app.model;

/**
 * Badge template/definition, stored in badgeDefinitions/{badgeType}.
 * Defines what metric to check and the threshold to unlock.
 *
 * Note: badgeType is stored as a regular field inside the document body
 * (written by the seeder), so we do NOT use @DocumentId here — that would
 * conflict when Firestore finds the same key in both the doc ID and the body.
 */
public class BadgeDefinition {

    private String badgeType;
    private String name;
    private String description;
    private String iconUrl;
    private String metric;        // e.g. "totalCo2", "currentStreak", "totalPoints"
    private double threshold;
    private String rarity;        // "Common", "Rare", "Epic", "Legendary"
    private double multiplierBonus;

    /** Required no-arg constructor for Firestore. */
    public BadgeDefinition() {}

    public BadgeDefinition(String badgeType, String name, String description,
                           String metric, double threshold, String rarity) {
        this.badgeType = badgeType;
        this.name = name;
        this.description = description;
        this.metric = metric;
        this.threshold = threshold;
        this.rarity = rarity;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getBadgeType() { return badgeType; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public String getMetric() { return metric; }
    public double getThreshold() { return threshold; }
    public String getRarity() { return rarity; }
    public double getMultiplierBonus() { return multiplierBonus; }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setMetric(String metric) { this.metric = metric; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public void setMultiplierBonus(double multiplierBonus) { this.multiplierBonus = multiplierBonus; }
}

package com.ecotrack.app.util;

import com.ecotrack.app.model.Badge;
import com.ecotrack.app.model.BadgeDefinition;
import com.ecotrack.app.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Compares user metrics against badge thresholds and returns newly earned badges.
 */
public final class BadgeEvaluator {

    private BadgeEvaluator() {}

    /**
     * Evaluate which new badges a user qualifies for.
     *
     * @param user            the current user with updated totals
     * @param definitions     all badge definitions from Firestore
     * @param existingBadges  badges the user already owns
     * @return list of newly earned Badge objects (empty if none)
     */
    public static List<Badge> evaluateNewBadges(User user,
                                                 List<BadgeDefinition> definitions,
                                                 List<Badge> existingBadges) {
        // Build a set of already-earned badge types for O(1) lookup
        Set<String> earned = new HashSet<>();
        if (existingBadges != null) {
            for (Badge b : existingBadges) {
                earned.add(b.getBadgeType());
            }
        }

        List<Badge> newBadges = new ArrayList<>();

        for (BadgeDefinition def : definitions) {
            // Skip if already earned
            if (earned.contains(def.getBadgeType())) continue;

            double userValue = getMetricValue(user, def.getMetric());
            if (userValue >= def.getThreshold()) {
                newBadges.add(new Badge(def.getBadgeType()));
            }
        }

        return newBadges;
    }

    /**
     * Extract the relevant metric value from the User model.
     */
    public static double getMetricValue(User user, String metric) {
        if (metric == null) return 0;
        switch (metric) {
            case "totalActivities":
                return user.getTotalActivitiesLogged();
            case "currentStreak":
                return user.getCurrentStreak();
            case "totalCo2":
                return user.getTotalCo2Saved();
            case "totalWater":
                return user.getTotalWaterSaved();
            case "totalRecycled":
                return user.getTotalWasteDiverted();
            case "totalPoints":
                return user.getTotalPoints();
            // Metrics that need per-type tracking (not yet in User model)
            // Fallback to 0 — will work once more fields are added
            case "totalBikeKm":
            case "challengesCompleted":
                return 0;
            default:
                return 0;
        }
    }
}

package com.ecotrack.app.util;

import java.util.Date;

/**
 * Determines streak continuation, increment, or reset.
 * Pure stateless utility — no Android dependencies.
 */
public final class StreakManager {

    private StreakManager() { /* Prevent instantiation */ }

    /**
     * Result of a streak evaluation.
     */
    public static class StreakResult {
        private final int newStreak;
        private final double multiplier;
        private final boolean streakBroken;

        public StreakResult(int newStreak, double multiplier, boolean streakBroken) {
            this.newStreak = newStreak;
            this.multiplier = multiplier;
            this.streakBroken = streakBroken;
        }

        public int getNewStreak() { return newStreak; }
        public double getMultiplier() { return multiplier; }
        public boolean isStreakBroken() { return streakBroken; }
    }

    /**
     * Evaluate what happens to a user's streak when they log an activity.
     *
     * Logic:
     * - If lastLogDate is today → no change (already logged today)
     * - If lastLogDate is yesterday → streak increments
     * - Otherwise → streak resets to 1
     *
     * Multiplier:
     * - streak ≥ 30 → 2.0×
     * - streak ≥ 7  → 1.5×
     * - else         → 1.0×
     *
     * @param lastLogDate   the user's last log date (can be null for first-ever log)
     * @param currentDate   the current date
     * @param currentStreak the user's current streak count
     * @return StreakResult with updated values
     */
    public static StreakResult evaluateStreak(Date lastLogDate, Date currentDate, int currentStreak) {
        // First-ever log
        if (lastLogDate == null) {
            int newStreak = 1;
            return new StreakResult(newStreak, getMultiplier(newStreak), false);
        }

        // Already logged today — no change
        if (DateUtils.isToday(lastLogDate)) {
            return new StreakResult(currentStreak, getMultiplier(currentStreak), false);
        }

        // Last log was yesterday → increment streak
        if (DateUtils.isConsecutiveDay(lastLogDate, currentDate)) {
            int newStreak = currentStreak + 1;
            return new StreakResult(newStreak, getMultiplier(newStreak), false);
        }

        // Gap of 2+ days → streak broken, reset to 1
        return new StreakResult(1, 1.0, currentStreak > 1);
    }

    /**
     * Returns the point multiplier for the given streak length.
     */
    public static double getMultiplier(int streak) {
        if (streak >= Constants.STREAK_TIER_2_DAYS) {
            return Constants.STREAK_MULTIPLIER_30;
        }
        if (streak >= Constants.STREAK_TIER_1_DAYS) {
            return Constants.STREAK_MULTIPLIER_7;
        }
        return 1.0;
    }
}

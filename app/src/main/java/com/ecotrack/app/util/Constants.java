package com.ecotrack.app.util;

/**
 * App-wide constants — collection names, SharedPreferences keys, limits, multipliers.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // ── Activity Limits ──────────────────────────────────────────────────
    public static final int MAX_DAILY_LOGS = 20;
    public static final double ANOMALY_THRESHOLD_BIKING_KM = 100.0;

    // ── Streak Multipliers ───────────────────────────────────────────────
    public static final double STREAK_MULTIPLIER_7 = 1.5;
    public static final double STREAK_MULTIPLIER_30 = 2.0;
    public static final int STREAK_TIER_1_DAYS = 7;
    public static final int STREAK_TIER_2_DAYS = 30;

    // ── Eco-Score Weights ────────────────────────────────────────────────
    public static final double ECO_WEIGHT_CO2 = 0.40;
    public static final double ECO_WEIGHT_WASTE = 0.25;
    public static final double ECO_WEIGHT_WATER = 0.20;
    public static final double ECO_WEIGHT_STREAK = 0.15;

    // ── Firestore Collection Names ───────────────────────────────────────
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_ACTIVITY_LOGS = "activityLogs";
    public static final String COLLECTION_CONVERSION_FACTORS = "conversionFactors";
    public static final String COLLECTION_CHALLENGES = "challenges";
    public static final String COLLECTION_PARTICIPANTS = "participants";
    public static final String COLLECTION_TEAMS = "teams";
    public static final String COLLECTION_BADGES = "badges";
    public static final String COLLECTION_BADGE_DEFINITIONS = "badgeDefinitions";
    public static final String COLLECTION_CAMPUS_STATS = "campusStats";
    public static final String COLLECTION_SOCIAL_FEED = "socialFeed";

    // ── Firestore Document Names ─────────────────────────────────────────
    public static final String DOC_CAMPUS_AGGREGATE = "aggregate";

    // ── SharedPreferences Keys ───────────────────────────────────────────
    public static final String PREFS_NAME = "ecotrack_prefs";
    public static final String PREF_DAILY_REMINDER_ENABLED = "pref_daily_reminder_enabled";
    public static final String PREF_REMINDER_HOUR = "pref_reminder_hour";
    public static final String PREF_CHALLENGE_UPDATES = "pref_challenge_updates";
    public static final String PREF_STREAK_ALERTS = "pref_streak_alerts";
    public static final String PREF_CAMPUS_MILESTONES = "pref_campus_milestones";
    public static final String PREF_BADGE_UNLOCKS = "pref_badge_unlocks";
    public static final String PREF_USER_ROLE = "pref_user_role";
    public static final String PREF_ONBOARDING_COMPLETE = "pref_onboarding_complete";
    public static final String PREF_SEEDER_DONE = "pref_seeder_done";

    // ── User Roles ───────────────────────────────────────────────────────
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ADMIN = "admin";

    // ── Activity Types ───────────────────────────────────────────────────
    public static final String ACTIVITY_BIKING = "biking";
    public static final String ACTIVITY_WALKING = "walking";
    public static final String ACTIVITY_RECYCLING = "recycling";
    public static final String ACTIVITY_WATER_SAVE = "water_save";
    public static final String ACTIVITY_ENERGY_SAVING = "energy_saving";
    public static final String ACTIVITY_PLASTIC_FREE = "plastic_free";
    public static final String ACTIVITY_COMPOSTING = "composting";
    public static final String ACTIVITY_REUSE_CUP = "reuse_cup";
    public static final String ACTIVITY_MEATLESS_MEAL = "meatless_meal";
    public static final String ACTIVITY_PUBLIC_TRANSIT = "public_transit";

    // ── Firebase Storage Paths ───────────────────────────────────────────
    public static final String STORAGE_PROOFS = "proofs";
    public static final String STORAGE_AVATARS = "avatars";
    public static final String STORAGE_BUCKET  = "gs://saturn-events.firebasestorage.app";

    // ── Pagination ───────────────────────────────────────────────────────
    public static final int PAGE_SIZE_LEADERBOARD = 20;
    public static final int PAGE_SIZE_FEED = 15;
    public static final int PAGE_SIZE_ACTIVITY_LOGS = 10;

    // ── Time Periods (Leaderboard) ───────────────────────────────────────
    public static final String PERIOD_THIS_WEEK = "this_week";
    public static final String PERIOD_THIS_MONTH = "this_month";
    public static final String PERIOD_ALL_TIME = "all_time";
}

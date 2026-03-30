package com.ecotrack.app.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.ecotrack.app.util.Constants;

/**
 * Notification preferences model backed by SharedPreferences.
 * Provides typed accessors for all notification toggles and reminder settings.
 */
public class NotificationPreferences {

    private boolean dailyReminderEnabled;
    private int reminderHour;          // 0–23
    private boolean challengeUpdates;
    private boolean streakAlerts;
    private boolean campusMilestones;
    private boolean badgeUnlocks;

    /** Default constructor with sensible defaults. */
    public NotificationPreferences() {
        this.dailyReminderEnabled = true;
        this.reminderHour = 8;
        this.challengeUpdates = true;
        this.streakAlerts = true;
        this.campusMilestones = true;
        this.badgeUnlocks = true;
    }

    // ── Load from SharedPreferences ──────────────────────────────────────

    public static NotificationPreferences load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);

        NotificationPreferences np = new NotificationPreferences();
        np.dailyReminderEnabled = prefs.getBoolean(Constants.PREF_DAILY_REMINDER_ENABLED, true);
        np.reminderHour = prefs.getInt(Constants.PREF_REMINDER_HOUR, 8);
        np.challengeUpdates = prefs.getBoolean(Constants.PREF_CHALLENGE_UPDATES, true);
        np.streakAlerts = prefs.getBoolean(Constants.PREF_STREAK_ALERTS, true);
        np.campusMilestones = prefs.getBoolean(Constants.PREF_CAMPUS_MILESTONES, true);
        np.badgeUnlocks = prefs.getBoolean(Constants.PREF_BADGE_UNLOCKS, true);
        return np;
    }

    // ── Save to SharedPreferences ────────────────────────────────────────

    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(Constants.PREF_DAILY_REMINDER_ENABLED, dailyReminderEnabled)
                .putInt(Constants.PREF_REMINDER_HOUR, reminderHour)
                .putBoolean(Constants.PREF_CHALLENGE_UPDATES, challengeUpdates)
                .putBoolean(Constants.PREF_STREAK_ALERTS, streakAlerts)
                .putBoolean(Constants.PREF_CAMPUS_MILESTONES, campusMilestones)
                .putBoolean(Constants.PREF_BADGE_UNLOCKS, badgeUnlocks)
                .apply();
    }

    // ── Getters & Setters ────────────────────────────────────────────────

    public boolean isDailyReminderEnabled() { return dailyReminderEnabled; }
    public void setDailyReminderEnabled(boolean enabled) { this.dailyReminderEnabled = enabled; }

    public int getReminderHour() { return reminderHour; }
    public void setReminderHour(int hour) { this.reminderHour = Math.max(0, Math.min(23, hour)); }

    public boolean isChallengeUpdates() { return challengeUpdates; }
    public void setChallengeUpdates(boolean enabled) { this.challengeUpdates = enabled; }

    public boolean isStreakAlerts() { return streakAlerts; }
    public void setStreakAlerts(boolean enabled) { this.streakAlerts = enabled; }

    public boolean isCampusMilestones() { return campusMilestones; }
    public void setCampusMilestones(boolean enabled) { this.campusMilestones = enabled; }

    public boolean isBadgeUnlocks() { return badgeUnlocks; }
    public void setBadgeUnlocks(boolean enabled) { this.badgeUnlocks = enabled; }

    /** Formatted time string for display, e.g. "8:00 AM". */
    public String getFormattedReminderTime() {
        int displayHour = reminderHour % 12;
        if (displayHour == 0) displayHour = 12;
        String amPm = reminderHour < 12 ? "AM" : "PM";
        return displayHour + ":00 " + amPm;
    }
}

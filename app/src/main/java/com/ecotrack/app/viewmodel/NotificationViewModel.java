package com.ecotrack.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ecotrack.app.model.NotificationPreferences;

/**
 * UI state for the Notification Settings screen.
 * Loads / saves preferences from SharedPreferences.
 */
public class NotificationViewModel extends AndroidViewModel {

    private final MutableLiveData<NotificationPreferences> preferences = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>();

    public NotificationViewModel(@NonNull Application application) {
        super(application);
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<NotificationPreferences> getPreferences() { return preferences; }
    public LiveData<Boolean> getSaveResult() { return saveResult; }

    // ── Load ─────────────────────────────────────────────────────────────

    public void loadPreferences() {
        NotificationPreferences np = NotificationPreferences.load(getApplication());
        preferences.setValue(np);
    }

    // ── Save ─────────────────────────────────────────────────────────────

    public void savePreferences() {
        NotificationPreferences np = preferences.getValue();
        if (np == null) return;

        try {
            np.save(getApplication());
            saveResult.setValue(true);
        } catch (Exception e) {
            saveResult.setValue(false);
        }
    }

    // ── Toggle Methods (modify in-memory, caller saves explicitly) ───────

    public void setDailyReminderEnabled(boolean enabled) {
        NotificationPreferences np = getOrCreatePrefs();
        np.setDailyReminderEnabled(enabled);
        preferences.setValue(np);
    }

    public void setReminderHour(int hour) {
        NotificationPreferences np = getOrCreatePrefs();
        np.setReminderHour(hour);
        preferences.setValue(np);
    }

    public void incrementReminderHour() {
        NotificationPreferences np = getOrCreatePrefs();
        int h = np.getReminderHour();
        np.setReminderHour(h >= 23 ? 0 : h + 1);
        preferences.setValue(np);
    }

    public void decrementReminderHour() {
        NotificationPreferences np = getOrCreatePrefs();
        int h = np.getReminderHour();
        np.setReminderHour(h <= 0 ? 23 : h - 1);
        preferences.setValue(np);
    }

    public void setChallengeUpdates(boolean enabled) {
        NotificationPreferences np = getOrCreatePrefs();
        np.setChallengeUpdates(enabled);
        preferences.setValue(np);
    }

    public void setStreakAlerts(boolean enabled) {
        NotificationPreferences np = getOrCreatePrefs();
        np.setStreakAlerts(enabled);
        preferences.setValue(np);
    }

    public void setCampusMilestones(boolean enabled) {
        NotificationPreferences np = getOrCreatePrefs();
        np.setCampusMilestones(enabled);
        preferences.setValue(np);
    }

    public void setBadgeUnlocks(boolean enabled) {
        NotificationPreferences np = getOrCreatePrefs();
        np.setBadgeUnlocks(enabled);
        preferences.setValue(np);
    }

    private NotificationPreferences getOrCreatePrefs() {
        NotificationPreferences np = preferences.getValue();
        if (np == null) {
            np = NotificationPreferences.load(getApplication());
            preferences.setValue(np);
        }
        return np;
    }
}

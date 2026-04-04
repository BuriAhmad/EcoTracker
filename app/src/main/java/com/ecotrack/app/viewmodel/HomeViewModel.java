package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.model.ActivityLog;
import com.ecotrack.app.model.CampusStats;
import com.ecotrack.app.model.User;
import com.ecotrack.app.repository.ActivityRepository;
import com.ecotrack.app.repository.UserRepository;
import com.ecotrack.app.util.DateUtils;
import com.ecotrack.app.util.EcoScoreCalculator;

import java.util.Date;
import java.util.List;

/**
 * UI state for the Home/landing screen.
 * Campus stats, user greeting, eco-score, streak, weekly points, recent activity.
 */
public class HomeViewModel extends ViewModel {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<CampusStats> campusStats = new MutableLiveData<>();
    private final MutableLiveData<Integer> ecoScore = new MutableLiveData<>(0);
    private final MutableLiveData<String> ecoLevel = new MutableLiveData<>("Eco Newcomer");
    private final MutableLiveData<Integer> userStreak = new MutableLiveData<>(0);
    private final MutableLiveData<Long> totalPoints = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> activityCount = new MutableLiveData<>(0);
    private final MutableLiveData<Double> totalCo2 = new MutableLiveData<>(0.0);
    private final MutableLiveData<List<ActivityLog>> recentLogs = new MutableLiveData<>();
    private final MutableLiveData<float[]> weeklyData = new MutableLiveData<>();
    private boolean dataLoaded = false;

    public HomeViewModel() {
        activityRepository = new ActivityRepository();
        userRepository = new UserRepository();
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<CampusStats> getCampusStats() { return campusStats; }
    public LiveData<Integer> getEcoScore() { return ecoScore; }
    public LiveData<String> getEcoLevel() { return ecoLevel; }
    public LiveData<Integer> getUserStreak() { return userStreak; }
    public LiveData<Long> getTotalPoints() { return totalPoints; }
    public LiveData<Integer> getActivityCount() { return activityCount; }
    public LiveData<Double> getTotalCo2() { return totalCo2; }
    public LiveData<List<ActivityLog>> getRecentLogs() { return recentLogs; }
    public LiveData<float[]> getWeeklyData() { return weeklyData; }

    // ── Load ─────────────────────────────────────────────────────────────

    public void loadHome() {
        if (dataLoaded) return;
        dataLoaded = true;
        loadUserData();
        loadCampusStats();
        loadActivityLogs();
    }

    private void loadUserData() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        // Show cached data instantly, then refresh from server
        userRepository.getUserDocumentCached(userId)
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) processUser(user);
                    }
                });
        userRepository.getUserDocument(userId).addOnSuccessListener(doc -> {
            if (doc == null || !doc.exists()) return;
            User user = doc.toObject(User.class);
            if (user == null) return;
            processUser(user);
        });
    }

    private void processUser(User user) {
        currentUser.setValue(user);
        userStreak.setValue(user.getCurrentStreak());
        totalPoints.setValue(user.getTotalPoints());
        double co2 = user.getTotalCo2Saved();
        double water = user.getTotalWaterSaved();
        double waste = user.getTotalWasteDiverted();
        totalCo2.setValue(co2);
        activityCount.setValue((int) user.getTotalActivitiesLogged());
        int score = EcoScoreCalculator.calculateEcoScore(co2, waste, water, user.getCurrentStreak());
        ecoScore.setValue(score);
        ecoLevel.setValue(EcoScoreCalculator.getLevel(score));
    }

    private void loadCampusStats() {
        // Show cached data instantly, then refresh from server
        activityRepository.getCampusStatsCached()
                .addOnSuccessListener(stats -> {
                    if (stats != null) campusStats.setValue(stats);
                });
        activityRepository.getCampusStats()
                .addOnSuccessListener(stats -> {
                    if (stats != null) campusStats.setValue(stats);
                });
    }

    /**
     * Single query for the past 7 days — derives both the recent logs list and the
     * weekly bar chart data in-memory, replacing two separate Firestore round trips.
     */
    private void loadActivityLogs() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        Date weekAgo = DateUtils.daysAgo(7);
        Date now = new Date();

        // Show cached data instantly
        activityRepository.getActivityLogsCached(userId, weekAgo, now)
                .addOnSuccessListener(logs -> {
                    if (!logs.isEmpty()) processActivityLogs(logs);
                });

        // Refresh from server
        activityRepository.getActivityLogs(userId, weekAgo, now)
                .addOnSuccessListener(this::processActivityLogs);
    }

    private void processActivityLogs(List<ActivityLog> logs) {
        // Recent logs — top 5 (already ordered desc by timestamp)
        recentLogs.setValue(logs.size() > 5 ? logs.subList(0, 5) : logs);

        // Weekly bar chart — Mon=0 … Sun=6
        float[] daily = new float[7];
        for (ActivityLog log : logs) {
            if (log.getTimestamp() != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(log.getTimestamp().toDate());
                int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
                int index = (dow == java.util.Calendar.SUNDAY) ? 6 : (dow - java.util.Calendar.MONDAY);
                if (index >= 0 && index < 7) daily[index] += log.getPointsEarned();
            }
        }
        weeklyData.setValue(daily);
    }

    private String getCurrentUserId() {
        return userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
    }
}

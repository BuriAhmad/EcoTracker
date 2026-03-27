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
        loadUserData();
        loadCampusStats();
        loadWeeklyChart();
        loadRecentLogs();
    }

    private void loadUserData() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        userRepository.getUserDocument(userId).addOnSuccessListener(doc -> {
            if (doc == null || !doc.exists()) return;
            User user = doc.toObject(User.class);
            if (user == null) return;

            currentUser.setValue(user);
            userStreak.setValue(user.getCurrentStreak());
            totalPoints.setValue(user.getTotalPoints());

            // Read running totals directly from user doc (set by batch write)
            double co2 = user.getTotalCo2Saved();
            double water = user.getTotalWaterSaved();
            double waste = user.getTotalWasteDiverted();
            totalCo2.setValue(co2);
            activityCount.setValue((int) user.getTotalActivitiesLogged());

            // Compute eco-score from stored totals
            int score = EcoScoreCalculator.calculateEcoScore(
                    co2, waste, water, user.getCurrentStreak());
            ecoScore.setValue(score);
            ecoLevel.setValue(EcoScoreCalculator.getLevel(score));
        });
    }

    private void loadCampusStats() {
        activityRepository.getCampusStats()
                .addOnSuccessListener(campusStats::setValue);
    }

    private void loadWeeklyChart() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        Date startOfWeek = DateUtils.getStartOfWeek();
        activityRepository.getActivityLogs(userId, startOfWeek, new Date())
                .addOnSuccessListener(logs -> {
                    float[] daily = new float[7];
                    for (ActivityLog log : logs) {
                        if (log.getTimestamp() != null) {
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(log.getTimestamp().toDate());
                            int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
                            int index = (dow == java.util.Calendar.SUNDAY) ? 6 : (dow - java.util.Calendar.MONDAY);
                            if (index >= 0 && index < 7) {
                                daily[index] += log.getPointsEarned();
                            }
                        }
                    }
                    weeklyData.setValue(daily);
                });
    }

    private void loadRecentLogs() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        Date weekAgo = DateUtils.daysAgo(7);
        activityRepository.getActivityLogs(userId, weekAgo, new Date())
                .addOnSuccessListener(logs -> {
                    if (logs.size() > 5) {
                        recentLogs.setValue(logs.subList(0, 5));
                    } else {
                        recentLogs.setValue(logs);
                    }
                });
    }

    private String getCurrentUserId() {
        return userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
    }
}

package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.model.ActivityLog;
import com.ecotrack.app.model.User;
import com.ecotrack.app.repository.ActivityRepository;
import com.ecotrack.app.repository.UserRepository;
import com.ecotrack.app.util.DateUtils;
import com.ecotrack.app.util.EcoScoreCalculator;
import com.ecotrack.app.util.EquivalencyTranslator;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UI state for the personal Dashboard screen.
 * Eco-score, weekly data, impact totals, heatmap data, recent logs, equivalencies.
 */
public class DashboardViewModel extends ViewModel {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<Integer> ecoScore = new MutableLiveData<>(0);
    private final MutableLiveData<String> ecoLevel = new MutableLiveData<>("Eco Newcomer");
    private final MutableLiveData<Double> totalCo2 = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalWater = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalWaste = new MutableLiveData<>(0.0);
    private final MutableLiveData<List<ActivityLog>> recentLogs = new MutableLiveData<>();
    private final MutableLiveData<Map<LocalDate, Integer>> heatmapData = new MutableLiveData<>();
    private final MutableLiveData<List<EquivalencyTranslator.Equivalency>> equivalencies = new MutableLiveData<>();
    private final MutableLiveData<float[]> weeklyData = new MutableLiveData<>();
    private final MutableLiveData<Integer> userStreak = new MutableLiveData<>(0);

    public DashboardViewModel() {
        activityRepository = new ActivityRepository();
        userRepository = new UserRepository();
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<Integer> getEcoScore() { return ecoScore; }
    public LiveData<String> getEcoLevel() { return ecoLevel; }
    public LiveData<Double> getTotalCo2() { return totalCo2; }
    public LiveData<Double> getTotalWater() { return totalWater; }
    public LiveData<Double> getTotalWaste() { return totalWaste; }
    public LiveData<List<ActivityLog>> getRecentLogs() { return recentLogs; }
    public LiveData<Map<LocalDate, Integer>> getHeatmapData() { return heatmapData; }
    public LiveData<List<EquivalencyTranslator.Equivalency>> getEquivalencies() { return equivalencies; }
    public LiveData<float[]> getWeeklyData() { return weeklyData; }
    public LiveData<Integer> getUserStreak() { return userStreak; }

    // ── Load All Data ────────────────────────────────────────────────────

    public void loadDashboard() {
        loadUserData();
        loadRecentLogs();
        loadWeeklyChart();
        loadHeatmapData();
    }

    private void loadUserData() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        userRepository.getUserDocument(userId).addOnSuccessListener(doc -> {
            if (doc == null || !doc.exists()) return;
            User user = doc.toObject(User.class);
            if (user == null) return;

            userStreak.setValue(user.getCurrentStreak());

            // Read running totals directly from user doc (set by batch write)
            double co2 = user.getTotalCo2Saved();
            double water = user.getTotalWaterSaved();
            double waste = user.getTotalWasteDiverted();
            totalCo2.setValue(co2);
            totalWater.setValue(water);
            totalWaste.setValue(waste);

            // Compute eco-score from stored totals
            int score = EcoScoreCalculator.calculateEcoScore(
                    co2, waste, water, user.getCurrentStreak());
            ecoScore.setValue(score);
            ecoLevel.setValue(EcoScoreCalculator.getLevel(score));

            // Equivalencies
            equivalencies.setValue(EquivalencyTranslator.translate(co2));
        });
    }

    private void loadRecentLogs() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        Date weekAgo = DateUtils.daysAgo(7);
        activityRepository.getActivityLogs(userId, weekAgo, new Date())
                .addOnSuccessListener(logs -> {
                    // Take most recent 5
                    if (logs.size() > 5) {
                        recentLogs.setValue(logs.subList(0, 5));
                    } else {
                        recentLogs.setValue(logs);
                    }
                });
    }

    private void loadWeeklyChart() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        Date startOfWeek = DateUtils.getStartOfWeek();
        activityRepository.getActivityLogs(userId, startOfWeek, new Date())
                .addOnSuccessListener(logs -> {
                    float[] daily = new float[7]; // Mon=0, Tue=1, ... Sun=6
                    for (ActivityLog log : logs) {
                        if (log.getTimestamp() != null) {
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(log.getTimestamp().toDate());
                            int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
                            // Calendar: SUNDAY=1, MONDAY=2, ...
                            int index = (dow == java.util.Calendar.SUNDAY) ? 6 : (dow - java.util.Calendar.MONDAY);
                            if (index >= 0 && index < 7) {
                                daily[index] += log.getPointsEarned();
                            }
                        }
                    }
                    weeklyData.setValue(daily);
                });
    }

    private void loadHeatmapData() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        // Load last 35 days for 5-week heatmap
        Date start = DateUtils.daysAgo(35);
        activityRepository.getActivityLogs(userId, start, new Date())
                .addOnSuccessListener(logs -> {
                    Map<LocalDate, Integer> map = new HashMap<>();
                    for (ActivityLog log : logs) {
                        if (log.getTimestamp() != null) {
                            LocalDate date = log.getTimestamp().toDate().toInstant()
                                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                            map.merge(date, 1, Integer::sum);
                        }
                    }
                    heatmapData.setValue(map);
                });
    }

    private String getCurrentUserId() {
        return userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
    }
}

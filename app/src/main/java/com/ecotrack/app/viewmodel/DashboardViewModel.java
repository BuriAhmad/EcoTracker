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
    private boolean dataLoaded = false;

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
        if (dataLoaded) return;
        dataLoaded = true;
        loadUserData();
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
        userStreak.setValue(user.getCurrentStreak());
        double co2 = user.getTotalCo2Saved();
        double water = user.getTotalWaterSaved();
        double waste = user.getTotalWasteDiverted();
        totalCo2.setValue(co2);
        totalWater.setValue(water);
        totalWaste.setValue(waste);
        int score = EcoScoreCalculator.calculateEcoScore(co2, waste, water, user.getCurrentStreak());
        ecoScore.setValue(score);
        ecoLevel.setValue(EcoScoreCalculator.getLevel(score));
        equivalencies.setValue(EquivalencyTranslator.translate(co2));
    }

    /**
     * Single query for the past 35 days — derives recent logs, weekly chart data, and
     * heatmap data in-memory, replacing three separate Firestore round trips.
     */
    private void loadActivityLogs() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        Date start = DateUtils.daysAgo(35);
        Date now = new Date();

        // Show cached data instantly
        activityRepository.getActivityLogsCached(userId, start, now)
                .addOnSuccessListener(logs -> {
                    if (!logs.isEmpty()) processActivityLogs(logs);
                });

        // Refresh from server
        activityRepository.getActivityLogs(userId, start, now)
                .addOnSuccessListener(this::processActivityLogs);
    }

    private void processActivityLogs(List<ActivityLog> logs) {
        // 1. Recent logs — top 5 (already ordered desc by timestamp)
        recentLogs.setValue(logs.size() > 5 ? logs.subList(0, 5) : logs);

        // 2. Weekly chart — current week only (Mon=0 … Sun=6)
        Date startOfWeek = DateUtils.getStartOfWeek();
        float[] daily = new float[7];
        for (ActivityLog log : logs) {
            if (log.getTimestamp() != null) {
                Date logDate = log.getTimestamp().toDate();
                if (!logDate.before(startOfWeek)) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(logDate);
                    int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
                    int index = (dow == java.util.Calendar.SUNDAY) ? 6 : (dow - java.util.Calendar.MONDAY);
                    if (index >= 0 && index < 7) daily[index] += log.getPointsEarned();
                }
            }
        }
        weeklyData.setValue(daily);

        // 3. Heatmap — all 35 days
        Map<LocalDate, Integer> map = new HashMap<>();
        for (ActivityLog log : logs) {
            if (log.getTimestamp() != null) {
                LocalDate date = log.getTimestamp().toDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                map.merge(date, 1, Integer::sum);
            }
        }
        heatmapData.setValue(map);
    }

    private String getCurrentUserId() {
        return userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
    }
}

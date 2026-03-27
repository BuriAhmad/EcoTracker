package com.ecotrack.app.controller;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.ecotrack.app.model.ActivityLog;
import com.ecotrack.app.model.Badge;
import com.ecotrack.app.model.BadgeDefinition;
import com.ecotrack.app.model.ConversionFactor;
import com.ecotrack.app.model.User;
import com.ecotrack.app.repository.ActivityRepository;
import com.ecotrack.app.repository.UserRepository;
import com.ecotrack.app.util.BadgeEvaluator;
import com.ecotrack.app.util.Constants;
import com.ecotrack.app.util.ImpactCalculator;
import com.ecotrack.app.util.StreakManager;
import com.ecotrack.app.util.ValidationUtils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the full activity logging workflow:
 * validate → compute impact → batch write (log + user totals + campus stats) → callback.
 */
public class ActivityController {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityController() {
        this.activityRepository = new ActivityRepository();
        this.userRepository = new UserRepository();
    }

    // ── Callback Interfaces ──────────────────────────────────────────────

    public interface LogCallback {
        void onSuccess(ActivityLog log);
        void onError(String message);
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ── Activity Logging ─────────────────────────────────────────────────

    /**
     * Log an activity: validate → check daily limit → compute impact → batch write.
     */
    public void logActivity(String activityType, double quantity,
                            @Nullable Uri photoUri, LogCallback callback) {
        // Validate inputs
        if (activityType == null || activityType.isEmpty()) {
            callback.onError("Please select an activity type");
            return;
        }
        if (!ValidationUtils.isPositiveQuantity(quantity)) {
            callback.onError("Quantity must be greater than zero");
            return;
        }

        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onError("You must be signed in to log activities");
            return;
        }

        // Check anomaly threshold for biking
        if (Constants.ACTIVITY_BIKING.equals(activityType)
                && quantity > Constants.ANOMALY_THRESHOLD_BIKING_KM) {
            callback.onError("That seems unusually high. Please verify the quantity.");
            return;
        }

        // Check daily log limit
        activityRepository.getDailyLogCount(userId, new Date())
                .addOnSuccessListener(count -> {
                    if (count >= Constants.MAX_DAILY_LOGS) {
                        callback.onError("Daily limit reached (" + Constants.MAX_DAILY_LOGS
                                + " logs). Come back tomorrow!");
                        return;
                    }

                    // Fetch conversion factor then execute
                    activityRepository.getConversionFactor(activityType)
                            .addOnSuccessListener(factor -> {
                                if (factor == null) {
                                    callback.onError("Unknown activity type: " + activityType);
                                    return;
                                }
                                executeLog(userId, activityType, quantity, factor, photoUri, callback);
                            })
                            .addOnFailureListener(e ->
                                    callback.onError("Couldn't load activity data: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't check daily limit: " + e.getMessage()));
    }

    private void executeLog(String userId, String activityType, double quantity,
                            ConversionFactor factor, @Nullable Uri photoUri, LogCallback callback) {
        // Calculate impact
        ImpactCalculator.ImpactResult impact = ImpactCalculator.calculateImpact(quantity, factor);

        // Build ActivityLog object
        ActivityLog log = new ActivityLog();
        log.setUserId(userId);
        log.setActivityType(activityType);
        log.setQuantity(quantity);
        log.setUnit(factor.getUnit());
        log.setCo2Saved(impact.getCo2Saved());
        log.setWaterSaved(impact.getWaterSaved());
        log.setWasteDiverted(impact.getWasteDiverted());
        log.setPointsEarned(impact.getPointsEarned());
        log.setVerified(photoUri == null); // If no photo, auto-verified

        // Execute batch write: save log + increment user totals + increment campus stats
        activityRepository.executeLogBatch(
                userId, log,
                impact.getCo2Saved(), impact.getWaterSaved(),
                impact.getWasteDiverted(), impact.getPointsEarned()
        ).addOnSuccessListener(aVoid -> {
            // Return success to UI immediately
            callback.onSuccess(log);
            // Then evaluate streak + badges fire-and-forget
            try {
                evaluateAndUpdateStreak(userId, log);
                evaluateBadges(userId);
            } catch (Exception ignored) { }
        }).addOnFailureListener(e ->
                 callback.onError("Failed to save activity: " + e.getMessage()));
    }

    // ── Conversion Factors ───────────────────────────────────────────────

    /**
     * Fetch all conversion factors (for displaying the activity grid).
     */
    public void getConversionFactors(DataCallback<List<ConversionFactor>> callback) {
        activityRepository.getAllConversionFactors()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onError("Couldn't load activities: " + e.getMessage()));
    }

    /**
     * Get a real-time impact preview without saving anything.
     */
    public void getImpactPreview(String activityType, double quantity,
                                  DataCallback<ImpactCalculator.ImpactResult> callback) {
        if (activityType == null || quantity <= 0) {
            callback.onSuccess(new ImpactCalculator.ImpactResult(0, 0, 0, 0));
            return;
        }

        activityRepository.getConversionFactor(activityType)
                .addOnSuccessListener(factor -> {
                    if (factor == null) {
                        callback.onSuccess(new ImpactCalculator.ImpactResult(0, 0, 0, 0));
                        return;
                    }
                    ImpactCalculator.ImpactResult result =
                            ImpactCalculator.calculateImpact(quantity, factor);
                    callback.onSuccess(result);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Get today's log count for displaying "X of 20".
     */
    public void getTodayLogCount(DataCallback<Integer> callback) {
        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onSuccess(0);
            return;
        }

        activityRepository.getDailyLogCount(userId, new Date())
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── Streak Integration ───────────────────────────────────────────────

    /**
     * Fetch the user doc, evaluate streak, and write back updated streak + lastLogDate.
     * This runs fire-and-forget after the main log batch succeeds.
     */
    private void evaluateAndUpdateStreak(String userId, ActivityLog log) {
        userRepository.getUserDocument(userId)
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) return;

                    User user = doc.toObject(User.class);
                    if (user == null) return;

                    Date lastLog = user.getLastLogDate() != null
                            ? user.getLastLogDate().toDate() : null;
                    int currentStreak = user.getCurrentStreak();

                    StreakManager.StreakResult result =
                            StreakManager.evaluateStreak(lastLog, new Date(), currentStreak);

                    // Update user document with new streak info
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("currentStreak", result.getNewStreak());
                    updates.put("lastLogDate", Timestamp.now());
                    userRepository.updateUserFields(userId, updates);
                });
    }

    /**
     * Fetch user + badge definitions + existing badges, evaluate, and save any new badges.
     * Runs fire-and-forget after a successful log.
     */
    private void evaluateBadges(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Fetch fresh user doc (with latest totals)
        userRepository.getUserDocument(userId).addOnSuccessListener(userDoc -> {
            if (userDoc == null || !userDoc.exists()) return;
            User user = userDoc.toObject(User.class);
            if (user == null) return;

            // 2. Fetch all badge definitions
            db.collection(Constants.COLLECTION_BADGE_DEFINITIONS).get()
                    .addOnSuccessListener(defSnap -> {
                        List<BadgeDefinition> definitions = new ArrayList<>();
                        for (DocumentSnapshot d : defSnap) {
                            BadgeDefinition def = d.toObject(BadgeDefinition.class);
                            if (def != null) definitions.add(def);
                        }

                        // 3. Fetch user's existing badges
                        db.collection(Constants.COLLECTION_USERS).document(userId)
                                .collection(Constants.COLLECTION_BADGES).get()
                                .addOnSuccessListener(badgeSnap -> {
                                    List<Badge> existing = new ArrayList<>();
                                    for (DocumentSnapshot b : badgeSnap) {
                                        Badge badge = b.toObject(Badge.class);
                                        if (badge != null) existing.add(badge);
                                    }

                                    // 4. Evaluate
                                    List<Badge> newBadges = BadgeEvaluator.evaluateNewBadges(
                                            user, definitions, existing);

                                    // 5. Save new badges
                                    for (Badge badge : newBadges) {
                                        db.collection(Constants.COLLECTION_USERS).document(userId)
                                                .collection(Constants.COLLECTION_BADGES)
                                                .document(badge.getBadgeType())
                                                .set(badge);
                                    }
                                });
                    });
        });
    }
}

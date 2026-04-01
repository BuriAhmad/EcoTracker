package com.ecotrack.app.repository;

import com.ecotrack.app.model.ActivityLog;
import com.ecotrack.app.model.CampusStats;
import com.ecotrack.app.model.ConversionFactor;
import com.ecotrack.app.util.Constants;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore CRUD for activityLogs, conversionFactors, and campusStats.
 */
public class ActivityRepository {

    private final FirebaseFirestore db;

    public ActivityRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ── Conversion Factors ───────────────────────────────────────────────

    /**
     * Fetch a single conversion factor by activity type.
     */
    public Task<ConversionFactor> getConversionFactor(String activityType) {
        return db.collection(Constants.COLLECTION_CONVERSION_FACTORS)
                .document(activityType)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return null;
                    }
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        return doc.toObject(ConversionFactor.class);
                    }
                    return null;
                });
    }

    /**
     * Fetch all conversion factors.
     */
    public Task<List<ConversionFactor>> getAllConversionFactors() {
        return db.collection(Constants.COLLECTION_CONVERSION_FACTORS)
                .get()
                .continueWith(task -> {
                    List<ConversionFactor> factors = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return factors;
                    }
                    for (DocumentSnapshot doc : task.getResult()) {
                        ConversionFactor f = doc.toObject(ConversionFactor.class);
                        if (f != null) factors.add(f);
                    }
                    return factors;
                });
    }

    // ── Activity Logs ────────────────────────────────────────────────────

    /**
     * Save an activity log to the user's subcollection.
     */
    public Task<Void> saveActivityLog(String userId, ActivityLog log) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_ACTIVITY_LOGS)
                .document()
                .set(log);
    }

    /**
     * Fetch activity logs for a user within a date range.
     */
    public Task<List<ActivityLog>> getActivityLogs(String userId, Date startDate, Date endDate) {
        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_ACTIVITY_LOGS)
                .whereGreaterThanOrEqualTo("timestamp", new Timestamp(startDate))
                .whereLessThanOrEqualTo("timestamp", new Timestamp(endDate))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    List<ActivityLog> logs = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return logs; // Return empty list on failure instead of throwing
                    }
                    for (DocumentSnapshot doc : task.getResult()) {
                        ActivityLog log = doc.toObject(ActivityLog.class);
                        if (log != null) logs.add(log);
                    }
                    return logs;
                });
    }

    /**
     * Count how many logs the user has made today.
     */
    public Task<Integer> getDailyLogCount(String userId, Date today) {
        // Get start and end of today
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();

        return db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_ACTIVITY_LOGS)
                .whereGreaterThanOrEqualTo("timestamp", new Timestamp(startOfDay))
                .whereLessThan("timestamp", new Timestamp(endOfDay))
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return 0;
                    }
                    return task.getResult().size();
                });
    }

    // ── Batch Operations ─────────────────────────────────────────────────

    /**
     * Execute the full activity log batch: save log + increment user totals + increment campus stats.
     */
    public Task<Void> executeLogBatch(String userId, ActivityLog log,
                                       double co2, double water, double waste, int points) {
        WriteBatch batch = db.batch();

        // 1. Save the activity log
        var logRef = db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_ACTIVITY_LOGS)
                .document();
        batch.set(logRef, log);

        // 2. Increment user totals (set-merge so it works even if fields don't exist yet)
        var userRef = db.collection(Constants.COLLECTION_USERS).document(userId);
        Map<String, Object> userInc = new HashMap<>();
        userInc.put("totalPoints", FieldValue.increment(points));
        userInc.put("totalCo2Saved", FieldValue.increment(co2));
        userInc.put("totalWaterSaved", FieldValue.increment(water));
        userInc.put("totalWasteDiverted", FieldValue.increment(waste));
        userInc.put("totalActivitiesLogged", FieldValue.increment(1));
        // Track per-type km for badge evaluation
        if (Constants.ACTIVITY_BIKING.equals(log.getActivityType())) {
            userInc.put("totalBikeKm", FieldValue.increment(log.getQuantity()));
        }
        batch.set(userRef, userInc, SetOptions.merge());

        // 3. Increment campus stats (set-merge creates the doc if it doesn't exist)
        var campusRef = db.collection(Constants.COLLECTION_CAMPUS_STATS)
                .document(Constants.DOC_CAMPUS_AGGREGATE);
        Map<String, Object> campusInc = new HashMap<>();
        campusInc.put("totalCo2Saved", FieldValue.increment(co2));
        campusInc.put("totalWaterSaved", FieldValue.increment(water));
        campusInc.put("totalWasteDiverted", FieldValue.increment(waste));
        campusInc.put("totalActivitiesLogged", FieldValue.increment(1));
        batch.set(campusRef, campusInc, SetOptions.merge());

        return batch.commit();
    }

    // ── Campus Stats ─────────────────────────────────────────────────────

    /**
     * Fetch the campus aggregate document.
     */
    public Task<CampusStats> getCampusStats() {
        return db.collection(Constants.COLLECTION_CAMPUS_STATS)
                .document(Constants.DOC_CAMPUS_AGGREGATE)
                .get()
                .continueWith(task -> {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        return doc.toObject(CampusStats.class);
                    }
                    return new CampusStats();
                });
    }
}

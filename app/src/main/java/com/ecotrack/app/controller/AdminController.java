package com.ecotrack.app.controller;

import com.ecotrack.app.model.User;
import com.ecotrack.app.util.Constants;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates admin-level operations: campus analytics, top students, etc.
 */
public class AdminController {

    private final FirebaseFirestore db;

    public AdminController() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ── Callback ─────────────────────────────────────────────────────────

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ── Campus Stats ─────────────────────────────────────────────────────

    /**
     * Fetch campus aggregate stats (total users, total activities, total CO2, etc.)
     * from campusStats/aggregate document.
     */
    public void getCampusStats(DataCallback<Map<String, Object>> callback) {
        db.collection(Constants.COLLECTION_CAMPUS_STATS)
                .document(Constants.DOC_CAMPUS_AGGREGATE)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        callback.onSuccess(doc.getData());
                    } else {
                        // Return defaults if no aggregate doc yet
                        Map<String, Object> defaults = new HashMap<>();
                        defaults.put("totalUsers", 0L);
                        defaults.put("totalActivities", 0L);
                        defaults.put("totalCo2Saved", 0.0);
                        defaults.put("totalWaterSaved", 0.0);
                        defaults.put("totalWasteDiverted", 0.0);
                        defaults.put("totalPointsEarned", 0L);
                        callback.onSuccess(defaults);
                    }
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't load campus stats: " + e.getMessage()));
    }

    // ── Top Students ─────────────────────────────────────────────────────

    /**
     * Get top students by points (for admin dashboard).
     */
    public void getTopStudents(int limit, DataCallback<List<User>> callback) {
        db.collection(Constants.COLLECTION_USERS)
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null) users.add(u);
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't load top students: " + e.getMessage()));
    }

    // ── User Count ───────────────────────────────────────────────────────

    /**
     * Get the total number of registered users.
     */
    public void getUserCount(DataCallback<Integer> callback) {
        db.collection(Constants.COLLECTION_USERS)
                .get()
                .addOnSuccessListener(snapshot ->
                        callback.onSuccess(snapshot.size()))
                .addOnFailureListener(e ->
                        callback.onError("Couldn't count users: " + e.getMessage()));
    }

    // ── Active Challenge Count ───────────────────────────────────────────

    /**
     * Get the number of active challenges.
     */
    public void getActiveChallengeCount(DataCallback<Integer> callback) {
        db.collection(Constants.COLLECTION_CHALLENGES)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snapshot ->
                        callback.onSuccess(snapshot.size()))
                .addOnFailureListener(e ->
                        callback.onError("Couldn't count challenges: " + e.getMessage()));
    }

    // ── Team Count ───────────────────────────────────────────────────────

    /**
     * Get the total number of teams.
     */
    public void getTeamCount(DataCallback<Integer> callback) {
        db.collection(Constants.COLLECTION_TEAMS)
                .get()
                .addOnSuccessListener(snapshot ->
                        callback.onSuccess(snapshot.size()))
                .addOnFailureListener(e ->
                        callback.onError("Couldn't count teams: " + e.getMessage()));
    }
}

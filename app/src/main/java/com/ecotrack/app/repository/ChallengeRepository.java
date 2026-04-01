package com.ecotrack.app.repository;

import com.ecotrack.app.model.Challenge;
import com.ecotrack.app.model.ChallengeParticipant;
import com.ecotrack.app.util.Constants;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Firestore CRUD for challenges and their participants.
 * Collection: challenges/{challengeId}
 * Sub-collection: challenges/{challengeId}/participants/{userId}
 */
public class ChallengeRepository {

    private final FirebaseFirestore db;

    public ChallengeRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ── Challenge Documents ──────────────────────────────────────────────

    /**
     * Create a new challenge document.
     */
    public Task<DocumentReference> createChallenge(Challenge challenge) {
        return db.collection(Constants.COLLECTION_CHALLENGES).add(challenge);
    }

    /**
     * Get all active challenges (status == "active"), ordered by endDate ascending.
     */
    public Task<QuerySnapshot> getActiveChallenges() {
        return db.collection(Constants.COLLECTION_CHALLENGES)
                .whereEqualTo("status", "active")
                .orderBy("endDate", Query.Direction.ASCENDING)
                .get();
    }

    /**
     * Get all active challenges that match a specific activity type.
     * Used when updating challenge progress after an activity log.
     */
    public Task<QuerySnapshot> getActiveChallengesByType(String activityType) {
        return db.collection(Constants.COLLECTION_CHALLENGES)
                .whereEqualTo("status", "active")
                .whereEqualTo("activityType", activityType)
                .get();
    }

    /**
     * Get a single challenge by ID.
     */
    public Task<DocumentSnapshot> getChallengeById(String challengeId) {
        return db.collection(Constants.COLLECTION_CHALLENGES)
                .document(challengeId)
                .get();
    }

    // ── Participants ─────────────────────────────────────────────────────

    /**
     * Add a participant to a challenge. Document ID = userId.
     */
    public Task<Void> addParticipant(String challengeId, ChallengeParticipant participant) {
        return db.collection(Constants.COLLECTION_CHALLENGES)
                .document(challengeId)
                .collection(Constants.COLLECTION_PARTICIPANTS)
                .document(participant.getUserId())
                .set(participant);
    }

    /**
     * Remove a participant from a challenge.
     */
    public Task<Void> removeParticipant(String challengeId, String userId) {
        return db.collection(Constants.COLLECTION_CHALLENGES)
                .document(challengeId)
                .collection(Constants.COLLECTION_PARTICIPANTS)
                .document(userId)
                .delete();
    }

    /**
     * Get a specific participant document (to check if user has joined).
     */
    public Task<DocumentSnapshot> getParticipant(String challengeId, String userId) {
        return db.collection(Constants.COLLECTION_CHALLENGES)
                .document(challengeId)
                .collection(Constants.COLLECTION_PARTICIPANTS)
                .document(userId)
                .get();
    }

    /**
     * Get all participants for a challenge, ordered by progress descending.
     */
    public Task<QuerySnapshot> getParticipants(String challengeId) {
        return db.collection(Constants.COLLECTION_CHALLENGES)
                .document(challengeId)
                .collection(Constants.COLLECTION_PARTICIPANTS)
                .orderBy("currentProgress", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Atomically increment a participant's progress by `delta`.
     * Also marks completed if progress reaches goal.
     */
    public Task<Void> incrementParticipantProgress(String challengeId, String userId,
                                                    double delta, double goalQuantity) {
        DocumentReference ref = db.collection(Constants.COLLECTION_CHALLENGES)
                .document(challengeId)
                .collection(Constants.COLLECTION_PARTICIPANTS)
                .document(userId);

        return db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(ref);
            if (!snap.exists()) return null;

            double current = snap.getDouble("currentProgress") != null
                    ? snap.getDouble("currentProgress") : 0;
            double updated = current + delta;
            boolean wasCompleted = Boolean.TRUE.equals(snap.getBoolean("completed"));
            boolean nowCompleted = updated >= goalQuantity;

            Map<String, Object> updates = new HashMap<>();
            updates.put("currentProgress", updated);
            updates.put("completed", nowCompleted);
            transaction.update(ref, updates);

            // Increment the user's completed-challenges counter the first time they finish
            if (nowCompleted && !wasCompleted) {
                DocumentReference userRef = db.collection(Constants.COLLECTION_USERS)
                        .document(userId);
                transaction.update(userRef, "totalChallengesCompleted", FieldValue.increment(1));
            }
            return null;
        });
    }

    /**
     * Increment the participantCount field on the challenge document.
     */
    public Task<Void> incrementParticipantCount(String challengeId) {
        return db.collection(Constants.COLLECTION_CHALLENGES)
                .document(challengeId)
                .update("participantCount", FieldValue.increment(1));
    }

    /**
     * Decrement the participantCount field on the challenge document.
     */
    public Task<Void> decrementParticipantCount(String challengeId) {
        return db.collection(Constants.COLLECTION_CHALLENGES)
                .document(challengeId)
                .update("participantCount", FieldValue.increment(-1));
    }
}

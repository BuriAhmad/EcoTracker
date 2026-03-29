package com.ecotrack.app.controller;

import com.ecotrack.app.model.Challenge;
import com.ecotrack.app.model.ChallengeParticipant;
import com.ecotrack.app.model.User;
import com.ecotrack.app.repository.ChallengeRepository;
import com.ecotrack.app.repository.UserRepository;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates challenge operations:
 * listing, joining/leaving, progress updates, creation.
 */
public class ChallengeController {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public ChallengeController() {
        this.challengeRepository = new ChallengeRepository();
        this.userRepository = new UserRepository();
    }

    // ── Callback ─────────────────────────────────────────────────────────

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ── List Challenges ──────────────────────────────────────────────────

    /**
     * Get all active challenges.
     */
    public void getActiveChallenges(DataCallback<List<Challenge>> callback) {
        challengeRepository.getActiveChallenges()
                .addOnSuccessListener(snapshot -> {
                    List<Challenge> challenges = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Challenge c = doc.toObject(Challenge.class);
                        if (c != null) challenges.add(c);
                    }
                    callback.onSuccess(challenges);
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't load challenges: " + e.getMessage()));
    }

    // ── Challenge Detail ─────────────────────────────────────────────────

    /**
     * Get a single challenge by ID.
     */
    public void getChallengeDetail(String challengeId, DataCallback<Challenge> callback) {
        challengeRepository.getChallengeById(challengeId)
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        callback.onError("Challenge not found");
                        return;
                    }
                    Challenge c = doc.toObject(Challenge.class);
                    callback.onSuccess(c);
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't load challenge: " + e.getMessage()));
    }

    // ── Join / Leave ─────────────────────────────────────────────────────

    /**
     * Join a challenge. Fetches user info to populate the participant doc.
     */
    public void joinChallenge(String challengeId, double goalQuantity,
                              DataCallback<Void> callback) {
        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onError("You must be signed in");
            return;
        }

        userRepository.getUserDocument(userId).addOnSuccessListener(doc -> {
            if (doc == null || !doc.exists()) {
                callback.onError("User profile not found");
                return;
            }
            User user = doc.toObject(User.class);
            if (user == null) {
                callback.onError("User profile not found");
                return;
            }

            ChallengeParticipant participant = new ChallengeParticipant();
            participant.setUserId(userId);
            participant.setDisplayName(user.getDisplayName() != null ? user.getDisplayName() : "User");
            participant.setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
            participant.setGoalQuantity(goalQuantity);
            participant.setCurrentProgress(0);
            participant.setCompleted(false);

            challengeRepository.addParticipant(challengeId, participant)
                    .addOnSuccessListener(aVoid -> {
                        // Also increment the participant count on the challenge doc
                        challengeRepository.incrementParticipantCount(challengeId);
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e ->
                            callback.onError("Couldn't join: " + e.getMessage()));
        }).addOnFailureListener(e -> callback.onError("Couldn't load user: " + e.getMessage()));
    }

    /**
     * Leave a challenge.
     */
    public void leaveChallenge(String challengeId, DataCallback<Void> callback) {
        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onError("You must be signed in");
            return;
        }

        challengeRepository.removeParticipant(challengeId, userId)
                .addOnSuccessListener(aVoid -> {
                    challengeRepository.decrementParticipantCount(challengeId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't leave: " + e.getMessage()));
    }

    // ── Check Participation ──────────────────────────────────────────────

    /**
     * Check if the current user has joined a challenge.
     */
    public void isUserParticipant(String challengeId, DataCallback<Boolean> callback) {
        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onSuccess(false);
            return;
        }

        challengeRepository.getParticipant(challengeId, userId)
                .addOnSuccessListener(doc -> callback.onSuccess(doc != null && doc.exists()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Get the current user's participant record for a challenge (or null if not joined).
     */
    public void getUserParticipant(String challengeId,
                                    DataCallback<ChallengeParticipant> callback) {
        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onSuccess(null);
            return;
        }

        challengeRepository.getParticipant(challengeId, userId)
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        callback.onSuccess(doc.toObject(ChallengeParticipant.class));
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── Participants List ────────────────────────────────────────────────

    /**
     * Get participants for a challenge, ordered by progress.
     */
    public void getParticipants(String challengeId,
                                DataCallback<List<ChallengeParticipant>> callback) {
        challengeRepository.getParticipants(challengeId)
                .addOnSuccessListener(snapshot -> {
                    List<ChallengeParticipant> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ChallengeParticipant p = doc.toObject(ChallengeParticipant.class);
                        if (p != null) list.add(p);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't load participants: " + e.getMessage()));
    }

    // ── Create Challenge (Admin) ─────────────────────────────────────────

    /**
     * Create a new challenge.
     */
    public void createChallenge(Challenge challenge, DataCallback<String> callback) {
        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onError("You must be signed in");
            return;
        }

        challenge.setCreatedBy(userId);
        challenge.setParticipantCount(0);
        if (challenge.getStatus() == null) {
            challenge.setStatus("active");
        }

        challengeRepository.createChallenge(challenge)
                .addOnSuccessListener(ref -> callback.onSuccess(ref.getId()))
                .addOnFailureListener(e ->
                        callback.onError("Couldn't create challenge: " + e.getMessage()));
    }

    // ── Progress Update (called from ActivityController) ─────────────────

    /**
     * After a user logs an activity, find all active challenges with the same
     * activity type and increment the user's progress on each.
     * Fire-and-forget — no callback needed.
     */
    public void updateProgressForActivity(String userId, String activityType, double quantity) {
        challengeRepository.getActiveChallengesByType(activityType)
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Challenge challenge = doc.toObject(Challenge.class);
                        if (challenge == null) continue;
                        String challengeId = doc.getId();

                        // Check if user is a participant
                        challengeRepository.getParticipant(challengeId, userId)
                                .addOnSuccessListener(partDoc -> {
                                    if (partDoc != null && partDoc.exists()) {
                                        challengeRepository.incrementParticipantProgress(
                                                challengeId, userId,
                                                quantity, challenge.getGoalQuantity());
                                    }
                                });
                    }
                });
    }
}

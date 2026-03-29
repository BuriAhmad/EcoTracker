package com.ecotrack.app.repository;

import com.ecotrack.app.model.Team;
import com.ecotrack.app.util.Constants;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Firestore CRUD for the teams collection.
 * Collection: teams/{teamId}
 */
public class TeamRepository {

    private final FirebaseFirestore db;

    public TeamRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ── Create ───────────────────────────────────────────────────────────

    /**
     * Create a new team document. Returns a DocumentReference with the auto-generated ID.
     */
    public Task<DocumentReference> createTeam(Team team) {
        return db.collection(Constants.COLLECTION_TEAMS).add(team);
    }

    // ── Read ─────────────────────────────────────────────────────────────

    /**
     * Get a single team by ID.
     */
    public Task<DocumentSnapshot> getTeamById(String teamId) {
        return db.collection(Constants.COLLECTION_TEAMS)
                .document(teamId)
                .get();
    }

    /**
     * Get all public teams, ordered by totalPoints descending.
     */
    public Task<QuerySnapshot> getAllTeams() {
        return db.collection(Constants.COLLECTION_TEAMS)
                .whereEqualTo("public", true)
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get teams filtered by type (club, department, other).
     */
    public Task<QuerySnapshot> getTeamsByType(String type) {
        return db.collection(Constants.COLLECTION_TEAMS)
                .whereEqualTo("public", true)
                .whereEqualTo("type", type)
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get teams where a specific user is a member.
     */
    public Task<QuerySnapshot> getTeamsForUser(String userId) {
        return db.collection(Constants.COLLECTION_TEAMS)
                .whereArrayContains("memberIds", userId)
                .get();
    }

    /**
     * Search teams by name prefix (case-sensitive Firestore prefix query).
     */
    public Task<QuerySnapshot> searchTeamsByName(String prefix) {
        String end = prefix + "\uf8ff";
        return db.collection(Constants.COLLECTION_TEAMS)
                .whereGreaterThanOrEqualTo("name", prefix)
                .whereLessThanOrEqualTo("name", end)
                .limit(20)
                .get();
    }

    // ── Update (Membership) ──────────────────────────────────────────────

    /**
     * Add a user to a team's memberIds array and increment memberCount.
     */
    public Task<Void> addMember(String teamId, String userId) {
        return db.collection(Constants.COLLECTION_TEAMS)
                .document(teamId)
                .update(
                        "memberIds", FieldValue.arrayUnion(userId),
                        "memberCount", FieldValue.increment(1)
                );
    }

    /**
     * Remove a user from a team's memberIds array and decrement memberCount.
     */
    public Task<Void> removeMember(String teamId, String userId) {
        return db.collection(Constants.COLLECTION_TEAMS)
                .document(teamId)
                .update(
                        "memberIds", FieldValue.arrayRemove(userId),
                        "memberCount", FieldValue.increment(-1)
                );
    }

    // ── Update (Points) ──────────────────────────────────────────────────

    /**
     * Atomically increment a team's totalPoints.
     */
    public Task<Void> incrementTeamPoints(String teamId, long points) {
        return db.collection(Constants.COLLECTION_TEAMS)
                .document(teamId)
                .update("totalPoints", FieldValue.increment(points));
    }

    // ── Delete ───────────────────────────────────────────────────────────

    /**
     * Delete a team document.
     */
    public Task<Void> deleteTeam(String teamId) {
        return db.collection(Constants.COLLECTION_TEAMS)
                .document(teamId)
                .delete();
    }
}

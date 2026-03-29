package com.ecotrack.app.controller;

import com.ecotrack.app.model.Team;
import com.ecotrack.app.model.User;
import com.ecotrack.app.repository.TeamRepository;
import com.ecotrack.app.repository.UserRepository;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orchestrates team operations: create, join, leave, list, detail.
 */
public class TeamController {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamController() {
        this.teamRepository = new TeamRepository();
        this.userRepository = new UserRepository();
    }

    // ── Callback ─────────────────────────────────────────────────────────

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ── List Teams ───────────────────────────────────────────────────────

    /**
     * Get all public teams.
     */
    public void getAllTeams(DataCallback<List<Team>> callback) {
        teamRepository.getAllTeams()
                .addOnSuccessListener(snapshot -> {
                    List<Team> teams = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Team t = doc.toObject(Team.class);
                        if (t != null) teams.add(t);
                    }
                    callback.onSuccess(teams);
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't load teams: " + e.getMessage()));
    }

    /**
     * Get teams filtered by type.
     */
    public void getTeamsByType(String type, DataCallback<List<Team>> callback) {
        teamRepository.getTeamsByType(type)
                .addOnSuccessListener(snapshot -> {
                    List<Team> teams = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Team t = doc.toObject(Team.class);
                        if (t != null) teams.add(t);
                    }
                    callback.onSuccess(teams);
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't load teams: " + e.getMessage()));
    }

    // ── Team Detail ──────────────────────────────────────────────────────

    /**
     * Get a single team by ID.
     */
    public void getTeamDetail(String teamId, DataCallback<Team> callback) {
        teamRepository.getTeamById(teamId)
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        callback.onError("Team not found");
                        return;
                    }
                    Team t = doc.toObject(Team.class);
                    callback.onSuccess(t);
                })
                .addOnFailureListener(e ->
                        callback.onError("Couldn't load team: " + e.getMessage()));
    }

    // ── Team Members (User objects) ──────────────────────────────────────

    /**
     * Fetch User objects for all member IDs in a team.
     */
    public void getTeamMembers(Team team, DataCallback<List<User>> callback) {
        List<String> memberIds = team.getMemberIds();
        if (memberIds == null || memberIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<User> members = Collections.synchronizedList(new ArrayList<>());
        final int[] remaining = {memberIds.size()};

        for (String uid : memberIds) {
            userRepository.getUserDocument(uid)
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null) members.add(user);
                        }
                        remaining[0]--;
                        if (remaining[0] <= 0) {
                            // Sort by points descending
                            List<User> sorted = new ArrayList<>(members);
                            sorted.sort((a, b) -> Long.compare(b.getTotalPoints(), a.getTotalPoints()));
                            callback.onSuccess(sorted);
                        }
                    })
                    .addOnFailureListener(e -> {
                        remaining[0]--;
                        if (remaining[0] <= 0) {
                            List<User> sorted = new ArrayList<>(members);
                            sorted.sort((a, b) -> Long.compare(b.getTotalPoints(), a.getTotalPoints()));
                            callback.onSuccess(sorted);
                        }
                    });
        }
    }

    // ── Join / Leave ─────────────────────────────────────────────────────

    /**
     * Join a team. Adds current user to memberIds array.
     */
    public void joinTeam(String teamId, DataCallback<Void> callback) {
        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onError("You must be signed in");
            return;
        }

        teamRepository.addMember(teamId, userId)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e ->
                        callback.onError("Couldn't join team: " + e.getMessage()));
    }

    /**
     * Leave a team. Removes current user from memberIds array.
     */
    public void leaveTeam(String teamId, DataCallback<Void> callback) {
        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onError("You must be signed in");
            return;
        }

        teamRepository.removeMember(teamId, userId)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e ->
                        callback.onError("Couldn't leave team: " + e.getMessage()));
    }

    // ── Create Team ──────────────────────────────────────────────────────

    /**
     * Create a new team. The current user becomes the first member.
     */
    public void createTeam(Team team, DataCallback<String> callback) {
        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onError("You must be signed in");
            return;
        }

        if (team.getName() == null || team.getName().trim().isEmpty()) {
            callback.onError("Team name is required");
            return;
        }

        team.setCreatedBy(userId);
        team.setMemberIds(new ArrayList<>(Collections.singletonList(userId)));
        team.setMemberCount(1);
        team.setTotalPoints(0);

        teamRepository.createTeam(team)
                .addOnSuccessListener(ref -> callback.onSuccess(ref.getId()))
                .addOnFailureListener(e ->
                        callback.onError("Couldn't create team: " + e.getMessage()));
    }

    // ── Check Membership ─────────────────────────────────────────────────

    /**
     * Check if the current user is a member of a team.
     */
    public boolean isCurrentUserMember(Team team) {
        String userId = userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
        if (userId == null || team == null || team.getMemberIds() == null) return false;
        return team.getMemberIds().contains(userId);
    }

    // ── Search Teams ─────────────────────────────────────────────────────

    /**
     * Search teams by name prefix.
     */
    public void searchTeams(String query, DataCallback<List<Team>> callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Capitalize first letter for prefix match
        String capitalized = query.substring(0, 1).toUpperCase() + query.substring(1);

        teamRepository.searchTeamsByName(capitalized)
                .addOnSuccessListener(snapshot -> {
                    List<Team> teams = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Team t = doc.toObject(Team.class);
                        if (t != null) teams.add(t);
                    }
                    // Also try lowercase
                    teamRepository.searchTeamsByName(query.toLowerCase())
                            .addOnSuccessListener(snap2 -> {
                                for (DocumentSnapshot doc : snap2.getDocuments()) {
                                    Team t = doc.toObject(Team.class);
                                    if (t != null && !containsTeam(teams, t)) {
                                        teams.add(t);
                                    }
                                }
                                callback.onSuccess(teams);
                            })
                            .addOnFailureListener(e -> callback.onSuccess(teams));
                })
                .addOnFailureListener(e ->
                        callback.onError("Search failed: " + e.getMessage()));
    }

    private boolean containsTeam(List<Team> teams, Team target) {
        if (target.getTeamId() == null) return false;
        for (Team t : teams) {
            if (target.getTeamId().equals(t.getTeamId())) return true;
        }
        return false;
    }

    // ── Team Points (fire-and-forget after activity log) ─────────────────

    /**
     * After a user logs an activity, increment all their teams' points.
     * Called fire-and-forget from ActivityController.
     */
    public void updateTeamPointsForUser(String userId, long points) {
        teamRepository.getTeamsForUser(userId)
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        teamRepository.incrementTeamPoints(doc.getId(), points);
                    }
                });
    }
}

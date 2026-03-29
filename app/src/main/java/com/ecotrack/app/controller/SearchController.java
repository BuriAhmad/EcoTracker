package com.ecotrack.app.controller;

import com.ecotrack.app.model.Challenge;
import com.ecotrack.app.model.Team;
import com.ecotrack.app.repository.ChallengeRepository;
import com.ecotrack.app.repository.TeamRepository;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles search across challenges and teams.
 */
public class SearchController {

    private final ChallengeRepository challengeRepository;
    private final TeamController teamController;

    public SearchController() {
        this.challengeRepository = new ChallengeRepository();
        this.teamController = new TeamController();
    }

    // ── Callback ─────────────────────────────────────────────────────────

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ── Search Results Wrapper ────────────────────────────────────────────

    public static class SearchResults {
        private final List<Challenge> challenges;
        private final List<Team> teams;

        public SearchResults(List<Challenge> challenges, List<Team> teams) {
            this.challenges = challenges;
            this.teams = teams;
        }

        public List<Challenge> getChallenges() { return challenges; }
        public List<Team> getTeams() { return teams; }
    }

    // ── Search ───────────────────────────────────────────────────────────

    /**
     * Search across challenges and teams. Returns combined results.
     */
    public void search(String query, DataCallback<SearchResults> callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onSuccess(new SearchResults(new ArrayList<>(), new ArrayList<>()));
            return;
        }

        final List<Challenge> challengeResults = new ArrayList<>();
        final List<Team> teamResults = new ArrayList<>();
        final int[] completed = {0};

        // Search challenges (by title from active challenges)
        challengeRepository.getActiveChallenges()
                .addOnSuccessListener(snapshot -> {
                    String lowerQuery = query.toLowerCase();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Challenge c = doc.toObject(Challenge.class);
                        if (c != null && c.getTitle() != null
                                && c.getTitle().toLowerCase().contains(lowerQuery)) {
                            challengeResults.add(c);
                        }
                    }
                    completed[0]++;
                    if (completed[0] >= 2) {
                        callback.onSuccess(new SearchResults(challengeResults, teamResults));
                    }
                })
                .addOnFailureListener(e -> {
                    completed[0]++;
                    if (completed[0] >= 2) {
                        callback.onSuccess(new SearchResults(challengeResults, teamResults));
                    }
                });

        // Search teams
        teamController.searchTeams(query, new TeamController.DataCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> data) {
                if (data != null) teamResults.addAll(data);
                completed[0]++;
                if (completed[0] >= 2) {
                    callback.onSuccess(new SearchResults(challengeResults, teamResults));
                }
            }

            @Override
            public void onError(String message) {
                completed[0]++;
                if (completed[0] >= 2) {
                    callback.onSuccess(new SearchResults(challengeResults, teamResults));
                }
            }
        });
    }

    /**
     * Search only challenges.
     */
    public void searchChallenges(String query, DataCallback<List<Challenge>> callback) {
        challengeRepository.getActiveChallenges()
                .addOnSuccessListener(snapshot -> {
                    List<Challenge> results = new ArrayList<>();
                    String lowerQuery = query.toLowerCase();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Challenge c = doc.toObject(Challenge.class);
                        if (c != null && c.getTitle() != null
                                && c.getTitle().toLowerCase().contains(lowerQuery)) {
                            results.add(c);
                        }
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(e ->
                        callback.onError("Search failed: " + e.getMessage()));
    }

    /**
     * Search only teams.
     */
    public void searchTeams(String query, DataCallback<List<Team>> callback) {
        teamController.searchTeams(query, new TeamController.DataCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> data) {
                callback.onSuccess(data != null ? data : new ArrayList<>());
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}

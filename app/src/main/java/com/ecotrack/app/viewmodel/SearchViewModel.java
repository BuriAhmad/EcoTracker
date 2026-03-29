package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.controller.SearchController;
import com.ecotrack.app.model.Challenge;
import com.ecotrack.app.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * UI state for the Search screen.
 */
public class SearchViewModel extends ViewModel {

    private final SearchController searchController;

    private final MutableLiveData<List<Challenge>> challengeResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Team>> teamResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>("all");

    public SearchViewModel() {
        searchController = new SearchController();
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<List<Challenge>> getChallengeResults() { return challengeResults; }
    public LiveData<List<Team>> getTeamResults() { return teamResults; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getCurrentFilter() { return currentFilter; }

    // ── Search ───────────────────────────────────────────────────────────

    public void setFilter(String filter) {
        currentFilter.setValue(filter);
    }

    public void search(String query) {
        if (query == null || query.trim().length() < 2) {
            challengeResults.setValue(new ArrayList<>());
            teamResults.setValue(new ArrayList<>());
            return;
        }

        isLoading.setValue(true);
        String filter = currentFilter.getValue();

        if ("challenges".equals(filter)) {
            searchController.searchChallenges(query, new SearchController.DataCallback<List<Challenge>>() {
                @Override
                public void onSuccess(List<Challenge> data) {
                    challengeResults.setValue(data);
                    teamResults.setValue(new ArrayList<>());
                    isLoading.setValue(false);
                }

                @Override
                public void onError(String message) {
                    errorMessage.setValue(message);
                    isLoading.setValue(false);
                }
            });
        } else if ("teams".equals(filter)) {
            searchController.searchTeams(query, new SearchController.DataCallback<List<Team>>() {
                @Override
                public void onSuccess(List<Team> data) {
                    teamResults.setValue(data);
                    challengeResults.setValue(new ArrayList<>());
                    isLoading.setValue(false);
                }

                @Override
                public void onError(String message) {
                    errorMessage.setValue(message);
                    isLoading.setValue(false);
                }
            });
        } else {
            // Search all
            searchController.search(query, new SearchController.DataCallback<SearchController.SearchResults>() {
                @Override
                public void onSuccess(SearchController.SearchResults data) {
                    challengeResults.setValue(data.getChallenges());
                    teamResults.setValue(data.getTeams());
                    isLoading.setValue(false);
                }

                @Override
                public void onError(String message) {
                    errorMessage.setValue(message);
                    isLoading.setValue(false);
                }
            });
        }
    }
}

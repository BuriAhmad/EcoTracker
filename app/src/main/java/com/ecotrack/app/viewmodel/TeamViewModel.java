package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.controller.TeamController;
import com.ecotrack.app.model.Team;
import com.ecotrack.app.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * UI state for Team screens (list, detail, create).
 */
public class TeamViewModel extends ViewModel {

    private final TeamController teamController;

    // ── List screen ──────────────────────────────────────────────────────
    private final MutableLiveData<List<Team>> teams = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // ── Detail screen ────────────────────────────────────────────────────
    private final MutableLiveData<Team> currentTeam = new MutableLiveData<>();
    private final MutableLiveData<List<User>> teamMembers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isMember = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isDetailLoading = new MutableLiveData<>(false);

    // ── Create screen ────────────────────────────────────────────────────
    private final MutableLiveData<Boolean> isCreating = new MutableLiveData<>(false);
    private final MutableLiveData<String> createResult = new MutableLiveData<>();

    public TeamViewModel() {
        teamController = new TeamController();
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<List<Team>> getTeams() { return teams; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public LiveData<Team> getCurrentTeam() { return currentTeam; }
    public LiveData<List<User>> getTeamMembers() { return teamMembers; }
    public LiveData<Boolean> getIsMember() { return isMember; }
    public LiveData<Boolean> getIsDetailLoading() { return isDetailLoading; }

    public LiveData<Boolean> getIsCreating() { return isCreating; }
    public LiveData<String> getCreateResult() { return createResult; }

    // ── List ─────────────────────────────────────────────────────────────

    public void loadTeams() {
        isLoading.setValue(true);
        teamController.getAllTeams(new TeamController.DataCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> data) {
                teams.setValue(data);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void loadTeamsByType(String type) {
        isLoading.setValue(true);
        teamController.getTeamsByType(type, new TeamController.DataCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> data) {
                teams.setValue(data);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    // ── Detail ───────────────────────────────────────────────────────────

    public void loadTeamDetail(String teamId) {
        isDetailLoading.setValue(true);

        teamController.getTeamDetail(teamId, new TeamController.DataCallback<Team>() {
            @Override
            public void onSuccess(Team data) {
                currentTeam.setValue(data);
                isMember.setValue(teamController.isCurrentUserMember(data));
                isDetailLoading.setValue(false);

                // Load members
                loadMembers(data);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isDetailLoading.setValue(false);
            }
        });
    }

    private void loadMembers(Team team) {
        teamController.getTeamMembers(team, new TeamController.DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                teamMembers.setValue(data);
            }

            @Override
            public void onError(String message) {
                // Silently fail
            }
        });
    }

    // ── Join / Leave ─────────────────────────────────────────────────────

    public void joinTeam(String teamId) {
        teamController.joinTeam(teamId, new TeamController.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isMember.setValue(true);
                loadTeamDetail(teamId);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public void leaveTeam(String teamId) {
        teamController.leaveTeam(teamId, new TeamController.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isMember.setValue(false);
                loadTeamDetail(teamId);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    // ── Create ───────────────────────────────────────────────────────────

    public void createTeam(Team team) {
        isCreating.setValue(true);
        teamController.createTeam(team, new TeamController.DataCallback<String>() {
            @Override
            public void onSuccess(String teamId) {
                isCreating.setValue(false);
                createResult.setValue(teamId);
            }

            @Override
            public void onError(String message) {
                isCreating.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}

package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.controller.ChallengeController;
import com.ecotrack.app.model.Challenge;
import com.ecotrack.app.model.ChallengeParticipant;

import java.util.ArrayList;
import java.util.List;

/**
 * UI state for the Challenges screens (list + detail + create).
 */
public class ChallengeViewModel extends ViewModel {

    private final ChallengeController challengeController;

    // ── List screen ──────────────────────────────────────────────────────
    private final MutableLiveData<List<Challenge>> challenges = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // ── Detail screen ────────────────────────────────────────────────────
    private final MutableLiveData<Challenge> currentChallenge = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isJoined = new MutableLiveData<>(false);
    private final MutableLiveData<ChallengeParticipant> userParticipant = new MutableLiveData<>();
    private final MutableLiveData<List<ChallengeParticipant>> participants = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isDetailLoading = new MutableLiveData<>(false);

    // ── Create screen ────────────────────────────────────────────────────
    private final MutableLiveData<Boolean> isCreating = new MutableLiveData<>(false);
    private final MutableLiveData<String> createResult = new MutableLiveData<>();

    public ChallengeViewModel() {
        challengeController = new ChallengeController();
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<List<Challenge>> getChallenges() { return challenges; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public LiveData<Challenge> getCurrentChallenge() { return currentChallenge; }
    public LiveData<Boolean> getIsJoined() { return isJoined; }
    public LiveData<ChallengeParticipant> getUserParticipant() { return userParticipant; }
    public LiveData<List<ChallengeParticipant>> getParticipants() { return participants; }
    public LiveData<Boolean> getIsDetailLoading() { return isDetailLoading; }

    public LiveData<Boolean> getIsCreating() { return isCreating; }
    public LiveData<String> getCreateResult() { return createResult; }

    // ── List ─────────────────────────────────────────────────────────────

    public void loadChallenges() {
        isLoading.setValue(true);
        challengeController.getActiveChallenges(new ChallengeController.DataCallback<List<Challenge>>() {
            @Override
            public void onSuccess(List<Challenge> data) {
                challenges.setValue(data);
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

    public void loadChallengeDetail(String challengeId) {
        isDetailLoading.setValue(true);

        // Load challenge doc
        challengeController.getChallengeDetail(challengeId,
                new ChallengeController.DataCallback<Challenge>() {
            @Override
            public void onSuccess(Challenge data) {
                currentChallenge.setValue(data);
                isDetailLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isDetailLoading.setValue(false);
            }
        });

        // Check if user has joined
        challengeController.getUserParticipant(challengeId,
                new ChallengeController.DataCallback<ChallengeParticipant>() {
            @Override
            public void onSuccess(ChallengeParticipant data) {
                isJoined.setValue(data != null);
                userParticipant.setValue(data);
            }

            @Override
            public void onError(String message) {
                isJoined.setValue(false);
                userParticipant.setValue(null);
            }
        });

        // Load participants
        loadParticipants(challengeId);
    }

    public void loadParticipants(String challengeId) {
        challengeController.getParticipants(challengeId,
                new ChallengeController.DataCallback<List<ChallengeParticipant>>() {
            @Override
            public void onSuccess(List<ChallengeParticipant> data) {
                participants.setValue(data);
            }

            @Override
            public void onError(String message) {
                // Silently fail
            }
        });
    }

    // ── Join / Leave ─────────────────────────────────────────────────────

    public void joinChallenge(String challengeId) {
        Challenge c = currentChallenge.getValue();
        double goal = c != null ? c.getGoalQuantity() : 0;

        challengeController.joinChallenge(challengeId, goal,
                new ChallengeController.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isJoined.setValue(true);
                // Reload detail to refresh counts + participant status
                loadChallengeDetail(challengeId);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public void leaveChallenge(String challengeId) {
        challengeController.leaveChallenge(challengeId,
                new ChallengeController.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isJoined.setValue(false);
                userParticipant.setValue(null);
                loadChallengeDetail(challengeId);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    // ── Create ───────────────────────────────────────────────────────────

    public void createChallenge(Challenge challenge) {
        isCreating.setValue(true);
        challengeController.createChallenge(challenge,
                new ChallengeController.DataCallback<String>() {
            @Override
            public void onSuccess(String challengeId) {
                isCreating.setValue(false);
                createResult.setValue(challengeId);
            }

            @Override
            public void onError(String message) {
                isCreating.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}

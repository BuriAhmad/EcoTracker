package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.controller.AdminController;
import com.ecotrack.app.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UI state for the Admin Analytics screen.
 */
public class AdminViewModel extends ViewModel {

    private final AdminController adminController;

    private final MutableLiveData<Map<String, Object>> campusStats = new MutableLiveData<>();
    private final MutableLiveData<List<User>> topStudents = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> userCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> challengeCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> teamCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminViewModel() {
        adminController = new AdminController();
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<Map<String, Object>> getCampusStats() { return campusStats; }
    public LiveData<List<User>> getTopStudents() { return topStudents; }
    public LiveData<Integer> getUserCount() { return userCount; }
    public LiveData<Integer> getChallengeCount() { return challengeCount; }
    public LiveData<Integer> getTeamCount() { return teamCount; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // ── Load Data ────────────────────────────────────────────────────────

    public void loadDashboard() {
        isLoading.setValue(true);

        // Load campus stats
        adminController.getCampusStats(new AdminController.DataCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                campusStats.setValue(data);
                checkLoadingComplete();
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                checkLoadingComplete();
            }
        });

        // Load top students
        adminController.getTopStudents(10, new AdminController.DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                topStudents.setValue(data);
                checkLoadingComplete();
            }

            @Override
            public void onError(String message) {
                checkLoadingComplete();
            }
        });

        // Load counts
        adminController.getUserCount(new AdminController.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                userCount.setValue(data);
                checkLoadingComplete();
            }

            @Override
            public void onError(String message) {
                checkLoadingComplete();
            }
        });

        adminController.getActiveChallengeCount(new AdminController.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                challengeCount.setValue(data);
                checkLoadingComplete();
            }

            @Override
            public void onError(String message) {
                checkLoadingComplete();
            }
        });

        adminController.getTeamCount(new AdminController.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                teamCount.setValue(data);
                checkLoadingComplete();
            }

            @Override
            public void onError(String message) {
                checkLoadingComplete();
            }
        });
    }

    private int loadCounter = 0;
    private void checkLoadingComplete() {
        loadCounter++;
        if (loadCounter >= 5) {
            isLoading.setValue(false);
            loadCounter = 0;
        }
    }
}

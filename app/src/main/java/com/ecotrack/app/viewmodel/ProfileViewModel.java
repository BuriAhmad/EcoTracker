package com.ecotrack.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ecotrack.app.model.Badge;
import com.ecotrack.app.model.BadgeDefinition;
import com.ecotrack.app.model.User;
import com.ecotrack.app.controller.UserController;
import com.ecotrack.app.repository.UserRepository;
import com.ecotrack.app.util.Constants;
import com.ecotrack.app.util.EcoScoreCalculator;
import com.ecotrack.app.util.StreakManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * UI state for the Profile screen.
 * User data, badges (earned + definitions), streak info, impact breakdown.
 */
public class ProfileViewModel extends ViewModel {

    private final FirebaseFirestore db;
    private final UserRepository userRepository;
    private final UserController userController;

    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<List<BadgeDefinition>> badgeDefinitions = new MutableLiveData<>();
    private final MutableLiveData<List<Badge>> earnedBadges = new MutableLiveData<>();
    private final MutableLiveData<Integer> ecoScore = new MutableLiveData<>(0);
    private final MutableLiveData<String> ecoLevel = new MutableLiveData<>("Eco Newcomer");
    private final MutableLiveData<Double> streakMultiplier = new MutableLiveData<>(1.0);
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
    private boolean dataLoaded = false;

    public ProfileViewModel() {
        db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository();
        userController = new UserController();
    }

    // ── Exposed LiveData ─────────────────────────────────────────────────

    public LiveData<User> getUser() { return user; }
    public LiveData<List<BadgeDefinition>> getBadgeDefinitions() { return badgeDefinitions; }
    public LiveData<List<Badge>> getEarnedBadges() { return earnedBadges; }
    public LiveData<Integer> getEcoScore() { return ecoScore; }
    public LiveData<String> getEcoLevel() { return ecoLevel; }
    public LiveData<Double> getStreakMultiplier() { return streakMultiplier; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
    public LiveData<Boolean> getDeleteSuccess() { return deleteSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsUpdating() { return isUpdating; }

    // ── Load ─────────────────────────────────────────────────────────────

    public void loadProfile() {
        if (dataLoaded) return;
        dataLoaded = true;
        loadUserData();
        loadBadgeDefinitions();
        loadEarnedBadges();
    }

    private void loadUserData() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        // Show cached data instantly, then refresh from server
        userRepository.getUserDocumentCached(userId)
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        User u = doc.toObject(User.class);
                        if (u != null) processUser(u);
                    }
                });
        userRepository.getUserDocument(userId).addOnSuccessListener(doc -> {
            if (doc == null || !doc.exists()) return;
            User u = doc.toObject(User.class);
            if (u == null) return;
            processUser(u);
        });
    }

    private void processUser(User u) {
        user.setValue(u);
        int score = EcoScoreCalculator.calculateEcoScore(
                u.getTotalCo2Saved(), u.getTotalWasteDiverted(),
                u.getTotalWaterSaved(), u.getCurrentStreak());
        ecoScore.setValue(score);
        ecoLevel.setValue(EcoScoreCalculator.getLevel(score));
        streakMultiplier.setValue(StreakManager.getMultiplier(u.getCurrentStreak()));
    }

    private void loadBadgeDefinitions() {
        // Show cached data instantly
        db.collection(Constants.COLLECTION_BADGE_DEFINITIONS).get(Source.CACHE)
                .addOnSuccessListener(snapshot -> {
                    List<BadgeDefinition> defs = new ArrayList<>();
                    for (DocumentSnapshot d : snapshot) {
                        BadgeDefinition def = d.toObject(BadgeDefinition.class);
                        if (def != null) defs.add(def);
                    }
                    if (!defs.isEmpty()) badgeDefinitions.setValue(defs);
                });
        // Refresh from server
        db.collection(Constants.COLLECTION_BADGE_DEFINITIONS).get()
                .addOnSuccessListener(snapshot -> {
                    List<BadgeDefinition> defs = new ArrayList<>();
                    for (DocumentSnapshot d : snapshot) {
                        BadgeDefinition def = d.toObject(BadgeDefinition.class);
                        if (def != null) defs.add(def);
                    }
                    badgeDefinitions.setValue(defs);
                });
    }

    private void loadEarnedBadges() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        // Show cached data instantly
        db.collection(Constants.COLLECTION_USERS).document(userId)
                .collection(Constants.COLLECTION_BADGES).get(Source.CACHE)
                .addOnSuccessListener(snapshot -> {
                    List<Badge> badges = new ArrayList<>();
                    for (DocumentSnapshot d : snapshot) {
                        Badge b = d.toObject(Badge.class);
                        if (b != null) badges.add(b);
                    }
                    if (!badges.isEmpty()) earnedBadges.setValue(badges);
                });
        // Refresh from server
        db.collection(Constants.COLLECTION_USERS).document(userId)
                .collection(Constants.COLLECTION_BADGES).get()
                .addOnSuccessListener(snapshot -> {
                    List<Badge> badges = new ArrayList<>();
                    for (DocumentSnapshot d : snapshot) {
                        Badge b = d.toObject(Badge.class);
                        if (b != null) badges.add(b);
                    }
                    earnedBadges.setValue(badges);
                });
    }

    public void logout() {
        userRepository.signOut();
    }

    // ── Profile Edit ─────────────────────────────────────────────────────

    public void updateProfile(String displayName, String department,
                              boolean anonymousOnFeed, boolean showOnLeaderboard) {
        isUpdating.setValue(true);
        userController.updateProfile(displayName, department, anonymousOnFeed, showOnLeaderboard,
                new UserController.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        isUpdating.setValue(false);
                        updateSuccess.setValue(true);
                        loadUserData();  // Refresh profile data
                    }

                    @Override
                    public void onError(String message) {
                        isUpdating.setValue(false);
                        errorMessage.setValue(message);
                    }
                });
    }

    public void uploadAvatar(byte[] imageBytes) {
        isUpdating.setValue(true);
        userController.uploadAvatar(imageBytes, new UserController.SimpleCallback() {
            @Override
            public void onSuccess() {
                isUpdating.setValue(false);
                updateSuccess.setValue(true);
                loadUserData();
            }

            @Override
            public void onError(String message) {
                isUpdating.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void deleteAccount() {
        isUpdating.setValue(true);
        userController.deleteAccount(new UserController.SimpleCallback() {
            @Override
            public void onSuccess() {
                isUpdating.setValue(false);
                deleteSuccess.setValue(true);
            }

            @Override
            public void onError(String message) {
                isUpdating.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    private String getCurrentUserId() {
        return userRepository.getCurrentUser() != null
                ? userRepository.getCurrentUser().getUid() : null;
    }
}
